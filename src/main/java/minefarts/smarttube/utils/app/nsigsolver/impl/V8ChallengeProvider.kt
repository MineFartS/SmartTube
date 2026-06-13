package minefarts.smarttube.utils.app.nsigsolver.impl

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8ScriptExecutionException

import minefarts.smarttube.ContextManager
import minefarts.smarttube.utils.mylogger.Log
import minefarts.smarttube.utils.app.nsigsolver.common.withLock
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

val sGson = Gson()

internal object V8ChallengeProvider {

    private val tag = V8ChallengeProvider::class.simpleName
    
    private val v8Runtime = ThreadLocal<V8>()
    private val v8Lock = Any()

    private fun runV8(stdin: String): String {
        
        val runtime = v8Runtime.get() ?: throw JsChallengeProviderError("V8 runtime not initialized yet")
        
        try {
            return runtime.withLock {
                it.executeStringScript(stdin) ?: throw JsChallengeProviderError("V8 runtime error: empty response")
            }
        } catch (e: V8ScriptExecutionException) {
            
            if (e.message?.contains("Invalid or unexpected token") ?: false)
                YouTubeInfoExtractor.cache.clear("challenge-solver") // cached data broken?

            var msg: String = e.message ?: ""

            if (e.message?.contains("ReferenceError: jsc is not defined") ?: false) {
                throw JsChallengeProviderError("jsc is still loading")
            } else {
                throw JsChallengeProviderError("V8 runtime error: ${msg}", e)
            }
            
        }

    }

    private fun initRuntime() {
        if (v8Runtime.get() != null) return

        v8Runtime.set(V8.createV8Runtime())

        val assets = ContextManager.get()?.assets

        for (name in listOf("lib", "core")) {

            val path = "nsigsolver/yt.solver.$name.js"

            assets!!.open(path).bufferedReader().use { 
                runV8(it.readText())
            }
            
        }
    
    }

    private fun disposeRuntime() {

        val runtime = v8Runtime.get() ?: return
        
        // NOTE: getting lock fixes "Invalid V8 thread access" issues.
        runtime.withLock {
            it.release(false)
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

            val stdout = synchronized(v8Lock) {
                
                initRuntime()

                runV8("""
                    JSON.stringify(
                        jsc(${sGson.toJson(data)})
                    );
                """)

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

    /**
     * Solve multiple JS challenges and return the results
     */
    fun bulkSolve(requests: List<JsChallengeRequest>): Sequence<JsChallengeProviderResponse> = sequence {
        val validatedRequests: MutableList<JsChallengeRequest> = mutableListOf()
        for (request in requests) {
            try {
                // Validate request using built-in settings
                if (request.type !in listOf(JsChallengeType.N, JsChallengeType.SIG)) {
                    throw JsChallengeProviderRejectedRequest("JS Challenge type ${request.type} is not supported by the provider ${this::class.simpleName}")
                }
                validatedRequests.add(request)
            } catch (e: JsChallengeProviderRejectedRequest) {
                yield(JsChallengeProviderResponse(request=request, error=e))
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