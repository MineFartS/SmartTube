package minefarts.smarttube.utils.app.nsigsolver.impl

import com.caoccao.javet.exceptions.JavetExecutionException
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.interop.V8Host

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

import minefarts.smarttube.utils.mylogger.Log
import minefarts.smarttube.utils.app.nsigsolver.common.CachedData
import minefarts.smarttube.utils.app.nsigsolver.common.YouTubeInfoExtractor
import minefarts.smarttube.utils.app.nsigsolver.common.loadScript

import minefarts.smarttube.utils.app.nsigsolver.provider.ChallengeOutput
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeProviderError
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeProviderRejectedRequest
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeProviderResponse
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeRequest
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeResponse
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeType
import minefarts.smarttube.utils.app.nsigsolver.runtime.Script
import minefarts.smarttube.utils.app.nsigsolver.runtime.ScriptSource
import minefarts.smarttube.utils.app.nsigsolver.runtime.ScriptType
import minefarts.smarttube.utils.app.nsigsolver.runtime.ScriptVariant
import minefarts.smarttube.utils.app.nsigsolver.runtime.SolverOutput
import minefarts.smarttube.utils.app.nsigsolver.runtime.solverOutputType

val sGson = Gson()

internal object V8ChallengeProvider {

    private val tag = V8ChallengeProvider::class.simpleName

    private val v8NpmLibFilename = listOf(
        "nsigsolver/polyfill.js",
        "nsigsolver/meriyah-6.1.4.min.js",
        "nsigsolver/astring-1.9.0.min.js"
    )

    val cacheSection = "challenge-solver"

    private val repository = "yt-dlp/ejs"
    private val supportedTypes = listOf(JsChallengeType.N, JsChallengeType.SIG)
    val scriptVersion = "0.0.1"

    private val scriptFilenames = mapOf(
        ScriptType.LIB to "nsigsolver/yt.solver.lib.js",
        ScriptType.CORE to "nsigsolver/yt.solver.core.js"
    )

    private val minScriptFilenames = mapOf(
        ScriptType.LIB to "yt.solver.lib.min.js",
        ScriptType.CORE to "yt.solver.core.min.js"
    )

    private val v8Runtime = ThreadLocal<V8Runtime>()
    private val v8Lock = Any()

    fun iterScriptSources(): Sequence<Pair<ScriptSource, (ScriptType) -> Script?>> = sequence {
        for ((source, func) in iterScriptSources2()) {
            if (source == ScriptSource.WEB || source == ScriptSource.BUILTIN)
                yield(Pair(ScriptSource.BUILTIN, ::v8NpmSource))
            yield(Pair(source, func))
        }
    }

    private fun v8NpmSource(scriptType: ScriptType): Script? {
        if (scriptType != ScriptType.LIB)
            return null

        val code = loadScript(
            v8NpmLibFilename,
            "Failed to read v8 challenge solver lib script"
        )

        return Script(
            scriptType,
            ScriptVariant.V8_NPM,
            ScriptSource.BUILTIN,
            scriptVersion,
            code
        )
    }

    private fun runV8(stdin: String): String {

        val runtime = v8Runtime.get() ?: throw JsChallengeProviderError("V8 runtime not initialized yet")

        val value = runtime.getExecutor(stdin).executeString()
            
        try {
            return value?.toString() ?: throw JsChallengeProviderError("V8 runtime error: empty response")

        } catch (e: JavetExecutionException) {
        
            if (e.message?.contains("Invalid or unexpected token") ?: false)
                YouTubeInfoExtractor.cache.clear(cacheSection)
                
            throw JsChallengeProviderError("V8 runtime error: ${e.message}", e)
        
        }

    }

    private fun initRuntime() {
        if (v8Runtime.get() != null)
            return
        v8Runtime.set(V8Host.getV8Instance().createV8Runtime())

        runV8("""
            ${libScript.code}
            ${coreScript.code}
            """ + ";\n\n"
        ) // warm up
    }

    private fun disposeRuntime() {
        val runtime = v8Runtime.get() ?: return
        // NOTE: getting lock fixes "Invalid V8 thread access" issues.
        runtime.v8Locker.use {
            runtime.close()
        }
        v8Runtime.remove()
    }

    fun warmup() {
        synchronized(v8Lock) {
            initRuntime()
        }
    }

    fun shutdown() {
        synchronized(v8Lock) {
            disposeRuntime()
        }
    }

    fun forceRecreate() {
        synchronized(v8Lock) {
            disposeRuntime()
            initRuntime()
        }
    }

    fun realBulkSolve(requests: List<JsChallengeRequest>): Sequence<JsChallengeProviderResponse> = sequence {
        val grouped: Map<String, List<JsChallengeRequest>> = requests.groupBy { it.input.playerUrl }

        for ((playerUrl, groupedRequests) in grouped) {

            val data = YouTubeInfoExtractor.cache.load(cacheSection, "player:$playerUrl")
            var player = data?.code

            val cached = if (player != null) {
                true
            } else {
                player = getPlayer(playerUrl)
                false
            }

            val jsonRequests = groupedRequests.map { request ->
                mapOf(
                    "type" to request.type.value,
                    "challenges" to request.input.challenges
                )
            }

            val data2 = if (cached) {
                mapOf(
                    "type" to "preprocessed",
                    "preprocessed_player" to player,
                    "requests" to jsonRequests
                )
            } else {
                mapOf(
                    "type" to "player",
                    "player" to player,
                    "requests" to jsonRequests,
                    "output_preprocessed" to true
                )
            }

            val jsonData = sGson.toJson(data2)

            val stdout = synchronized(v8Lock) {
                try {
                    initRuntime()
                    runV8("JSON.stringify(jsc($jsonData));")
                } finally {
                    disposeRuntime()
                }
            }

            val output: SolverOutput = try {
                sGson.fromJson(stdout, solverOutputType)
            } catch (e: JsonSyntaxException) {
                throw JsChallengeProviderError("Cannot parse solver output", e)
            }

            if (output.type == "error")
                throw JsChallengeProviderError(output.error ?: "Unknown solver output error")

            val preprocessed = output.preprocessed_player
            if (preprocessed != null)
                YouTubeInfoExtractor.cache.store(cacheSection, "player:$playerUrl", CachedData(preprocessed))

            for ((request, responseData) in groupedRequests.zip(output.responses)) {
                if (responseData.type == "error") {
                    yield(
                        JsChallengeProviderResponse(
                            request,
                            null,
                            JsChallengeProviderError(responseData.error ?: "Unknown solver output error")
                        )
                    )
                } else {
                    yield(
                        JsChallengeProviderResponse(
                            request,
                            JsChallengeResponse(request.type, ChallengeOutput(responseData.data)),
                            null
                        )
                    )
                }
            }
        }
    }

    // region: challenge solver script

    private val libScript: Script by lazy { getScript(ScriptType.LIB) }
    private val coreScript: Script by lazy { getScript(ScriptType.CORE) }

    private fun getScript(scriptType: ScriptType): Script {
        for ((_, fromSource) in iterScriptSources2()) {
            val script = fromSource(scriptType) ?: continue
            if (script.version != scriptVersion) {
                Log.w(
                    tag,
                    "Challenge solver ${scriptType.value} script version ${script.version} is not supported (source: ${script.source.value}, supported version: $scriptVersion)"
                )
            }

            Log.d(
                tag,
                "Using challenge solver ${script.type.value} script v${script.version} (source: ${script.source.value}, variant: ${script.variant.value})"
            )
            return script
        }

        throw JsChallengeProviderRejectedRequest("No usable challenge solver ${scriptType.value} script available")
    }

    private fun iterScriptSources2(): Sequence<Pair<ScriptSource, (scriptType: ScriptType) -> Script?>> = sequence {
        yieldAll(
            listOf(
                Pair(ScriptSource.CACHE, ::cachedSource),
                Pair(ScriptSource.BUILTIN, ::builtinSource),
                Pair(ScriptSource.WEB, ::webReleaseSource)
            )
        )
    }

    private fun cachedSource(scriptType: ScriptType): Script? {
        val data = YouTubeInfoExtractor.cache.load(cacheSection, scriptType.value) ?: return null
        return Script(
            scriptType,
            ScriptVariant.valueOf(data.variant ?: "unknown"),
            ScriptSource.CACHE,
            data.version ?: "unknown",
            data.code
        )
    }

    private fun builtinSource(scriptType: ScriptType): Script? {
        val fileName = scriptFilenames[scriptType] ?: return null

        return Script(
            scriptType,
            ScriptVariant.UNMINIFIED,
            ScriptSource.BUILTIN,
            scriptVersion,
            loadScript(fileName, "Failed to read builtin challenge solver ${scriptType.value}")
        )
    }

    private fun webReleaseSource(scriptType: ScriptType): Script? {
        val fileName = minScriptFilenames[scriptType] ?: return null

        val code = YouTubeInfoExtractor.downloadWebpageWithRetries(
            "https://github.com/$repository/releases/download/$scriptVersion/$fileName",
            "[${tag}] Failed to download challenge solver ${scriptType.value} script"
        )

        YouTubeInfoExtractor.cache.store(
            cacheSection,
            scriptType.value,
            CachedData(code)
        )

        return Script(
            scriptType,
            ScriptVariant.MINIFIED,
            ScriptSource.WEB,
            scriptVersion,
            code
        )
    }

    // endregion: challenge solver script

    private fun validateRequest(request: JsChallengeRequest) {
        if (request.type !in supportedTypes) {
            throw JsChallengeProviderRejectedRequest(
                "JS Challenge type ${request.type} is not supported by the provider ${this::class.simpleName}"
            )
        }
    }

    fun bulkSolve(requests: List<JsChallengeRequest>): Sequence<JsChallengeProviderResponse> = sequence {
        val validatedRequests: MutableList<JsChallengeRequest> = mutableListOf()
        for (request in requests) {
            try {
                validateRequest(request)
                validatedRequests.add(request)
            } catch (e: JsChallengeProviderRejectedRequest) {
                yield(JsChallengeProviderResponse(request = request, error = e))
            }
        }
        yieldAll(realBulkSolve(validatedRequests))
    }

    fun getPlayer(playerUrl: String?): String {
        var mPlayerUrl = playerUrl
        if (mPlayerUrl == null)
            mPlayerUrl = ""

        return try {
            YouTubeInfoExtractor.loadPlayer(mPlayerUrl)
        } catch (e: Exception) {
            throw JsChallengeProviderError("Failed to load player for JS challenge: $playerUrl", e)
        }
    }

}

