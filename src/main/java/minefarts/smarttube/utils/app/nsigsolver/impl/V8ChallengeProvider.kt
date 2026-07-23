package minefarts.smarttube.utils.app.nsigsolver.impl

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8ScriptExecutionException

import minefarts.smarttube.ContextManager
import minefarts.smarttube.utils.mylogger.Log
import minefarts.smarttube.utils.app.nsigsolver.provider.ChallengeOutput
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeProviderResponse
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeRequest
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeResponse
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeType
import minefarts.smarttube.utils.app.nsigsolver.runtime.SolverOutput

typealias V8ChallengeProvider2 = com.liskovsoft.youtubeapi.app.nsigsolver.impl.V8ChallengeProvider

public object V8ChallengeProvider {
    
    @JvmField
    public val v8Runtime = ThreadLocal<V8>()

    private val sGson = Gson()

    private val assets
        get() = ContextManager.get()?.assets

    @Synchronized
    private fun runV8(stdin: String): String {
        
        if (v8Runtime.get() == null) {
            
            val runtime = V8.createV8Runtime()
            v8Runtime.set(runtime)

            for (name in listOf("lib", "core")) {

                val path = "nsigsolver/yt.solver.$name.js"

                assets!!.open(path).bufferedReader().use { 
                    runtime!!.executeStringScript(it.readText())
                }
                
            }
            
        }

        return v8Runtime.get()!!.executeStringScript(stdin)
            ?: throw RuntimeException("V8 runtime error: empty response")

    }

    /**
     * Solve multiple JS challenges and return the results
     */
    fun bulkSolve(vararg requests: JsChallengeRequest): Sequence<JsChallengeProviderResponse> = sequence {
        
        val grouped: Map<String, List<JsChallengeRequest>> = requests.groupBy { it.input.playerUrl }

        for ((playerUrl, groupedRequests) in grouped) {

            val data = mutableMapOf<String, Any?>(
                "type" to "player",
                "player" to V8ChallengeProvider2.getPlayer(playerUrl),
                "output_preprocessed" to true,
            )

            data["requests"] = groupedRequests.map { request -> mapOf(
                "type" to request.type.value,
                "challenges" to request.input.challenges
            )}

            val output: SolverOutput = sGson.fromJson(
                runV8("JSON.stringify( jsc(${sGson.toJson(data)}) )"), 
                object : TypeToken<SolverOutput>() {}.type
            )

            if (output.type == "error")
                throw RuntimeException(output.error ?: "")

            for ((request, responseData) in groupedRequests.zip(output.responses)) {
                if (responseData.type == "error") {
                    yield(JsChallengeProviderResponse(
                        request, null, 
                        RuntimeException(responseData.error ?: "Unknown solver output error")
                    ))
                } else {
                    yield(JsChallengeProviderResponse(
                        request, 
                        JsChallengeResponse(request.type, ChallengeOutput(responseData.data))
                    ))
                }
            }
        }

    }

}