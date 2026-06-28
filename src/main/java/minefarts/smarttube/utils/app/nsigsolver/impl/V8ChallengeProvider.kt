package minefarts.smarttube.utils.app.nsigsolver.impl

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8ScriptExecutionException

import minefarts.smarttube.ContextManager
import minefarts.smarttube.utils.mylogger.Log
import minefarts.smarttube.utils.app.nsigsolver.common.YouTubeInfoExtractor
import minefarts.smarttube.utils.app.nsigsolver.common.CachedData
import minefarts.smarttube.utils.app.nsigsolver.provider.ChallengeOutput
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeProviderError
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeProviderRejectedRequest
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeProviderResponse
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeRequest
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeResponse
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeType
import minefarts.smarttube.utils.app.nsigsolver.runtime.solverOutputType
import minefarts.smarttube.utils.app.nsigsolver.runtime.SolverOutput


public object V8ChallengeProvider {
    
    private val sGson = Gson()
    private val v8Runtime = ThreadLocal<V8>()
    private val v8Lock = Any()

    private val assets
        get() = ContextManager.get()?.assets

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
        // V8-specific lib scripts that uses Deno NPM imports
        
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

    private fun runJsRuntime(stdin: String): String {
        synchronized(v8Lock) {
            try {
                initRuntime()
                return runV8(stdin)
            } finally {
                disposeRuntime()
            }
        }
    }

    /**
     * Execute a JavaScript string in the V8 runtime
     */
    private fun runV8(stdin: String): String { synchronized(v8Lock) {
        if (v8Runtime.get() == null) {
            v8Runtime.set(V8.createV8Runtime())

            for (name in listOf("lib", "core")) {

                val path = "nsigsolver/yt.solver.$name.js"

                assets!!.open(path).bufferedReader().use { 
                    runV8(it.readText())
                }
                
            }
        }
    
    }

    private fun disposeRuntime() {

        val runtime = v8Runtime.get() ?: return

        // NOTE: getting lock fixes "Invalid V8 thread access: the locker has been released!"

        runtime.withLock {
            it.release(false)
        }
        
        val runtime = v8Runtime.get()

        return runtime!!.executeStringScript(stdin)
            ?: throw JsChallengeProviderError("V8 runtime error: empty response")

    }}

    /**
     * Solve multiple JS challenges and return the results
     */
    fun bulkSolve(vararg requests: JsChallengeRequest): Sequence<JsChallengeProviderResponse> = sequence {
        
        val grouped: Map<String, List<JsChallengeRequest>> = requests.groupBy { it.input.playerUrl }

        for ((playerUrl, groupedRequests) in grouped) {

            val player = YouTubeInfoExtractor.cache.load(
                "challenge-solver", 
                "player:$playerUrl"
            )?.code

            val data: MutableMap<String, Any> = if (player == null) {
                mutableMapOf(
                    "type" to "player",
                    "player" to getPlayer(playerUrl),
                    "output_preprocessed" to true
                )
            } else {
                mutableMapOf(
                    "type" to "preprocessed",
                    "preprocessed_player" to player,
                )
            }

            data["requests"] = groupedRequests.map { request ->
                mapOf(
                    "type" to request.type.value,
                    "challenges" to request.input.challenges
                )
            }

            val stdout = runV8("""
                JSON.stringify(
                    jsc(${sGson.toJson(data)})
                );
            """)

            val output: SolverOutput = try {
                sGson.fromJson(stdout, solverOutputType)
            } catch (e: JsonSyntaxException) {
                throw JsChallengeProviderError("Cannot parse solver output", e)
            }

            if (output.type == "error")
                throw JsChallengeProviderError(output.error ?: "Unknown solver output error")

            val preprocessed = output.preprocessed_player
            if (preprocessed != null)
                YouTubeInfoExtractor.cache.store("challenge-solver", "player:$playerUrl", CachedData(preprocessed))

            for ((request, responseData) in groupedRequests.zip(output.responses)) {
                if (responseData.type == "error") {
                    yield(JsChallengeProviderResponse(
                        request, null, JsChallengeProviderError(responseData.error ?: "Unknown solver output error")))
                } else {
                    yield(JsChallengeProviderResponse(
                        request, JsChallengeResponse(request.type, ChallengeOutput(responseData.data))
                    ))
                }
            }
        }

    }

    fun getPlayer(playerUrl: String?): String {

        return try {
            YouTubeInfoExtractor.loadPlayer(playerUrl ?: "")
        } catch (e: Exception) {
            throw JsChallengeProviderError("Failed to load player for JS challenge: $playerUrl", e)
        }

    }

}