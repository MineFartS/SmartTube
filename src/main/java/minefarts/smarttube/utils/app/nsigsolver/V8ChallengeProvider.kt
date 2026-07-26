package minefarts.smarttube.utils.app.nsigsolver

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

import java.util.concurrent.Executors

import com.caoccao.javet.interop.V8Host
import com.caoccao.javet.interop.V8Runtime

import minefarts.smarttube.ContextManager
import minefarts.smarttube.utils.mylogger.Log

import com.liskovsoft.youtubeapi.app.nsigsolver.provider.ChallengeOutput
import com.liskovsoft.youtubeapi.app.nsigsolver.provider.JsChallengeProviderResponse
import com.liskovsoft.youtubeapi.app.nsigsolver.provider.JsChallengeRequest
import com.liskovsoft.youtubeapi.app.nsigsolver.provider.JsChallengeResponse
import com.liskovsoft.youtubeapi.app.nsigsolver.provider.JsChallengeType
import com.liskovsoft.youtubeapi.app.nsigsolver.runtime.SolverOutput

typealias V8ChallengeProvider2 = com.liskovsoft.youtubeapi.app.nsigsolver.impl.V8ChallengeProvider

inline fun <reified T> Gson.fromJson(json: String): T {
    return this.fromJson(json, object : TypeToken<T>() {}.type)
}

public object V8ChallengeProvider {
    
    private val sGson = Gson()

    private val assets
        get() = ContextManager.get()?.assets!!
    
    private val v8Executor = Executors.newSingleThreadExecutor() 

    private var v8Runtime: V8Runtime? = null

    private fun jsc(data: Map<String, Any>): SolverOutput = v8Executor.submit<SolverOutput> {

        if (v8Runtime == null) {

            v8Runtime = V8Host.getV8Instance().createV8Runtime()

            assets.open("yt.solver.js").bufferedReader().use { 
                v8Runtime!!.getExecutor( it.readText() ).executeVoid()
            }

        }
        
        val script: String = """
        (function() {
            var data = ${sGson.toJson(data)};
            var resp = jsc.default(data);
            return JSON.stringify(resp);
        })()
        """

        val resp: String = v8Runtime!!.getExecutor(script).executeString()

        sGson.fromJson(resp)

    }.get()

    public fun bulkSolve(vararg requests: JsChallengeRequest): Sequence<JsChallengeProviderResponse> = sequence {

        for ((playerUrl, groupedRequests) in requests.groupBy{it.input.playerUrl}) {

            val data = mapOf<String, Any>(
                "type" to "player",
                "player" to V8ChallengeProvider2.getPlayer(playerUrl),
                "output_preprocessed" to true,
                "requests" to groupedRequests.map { request -> mapOf(
                    "type" to request.type.value,
                    "challenges" to request.input.challenges
                )}
            )

            val output: SolverOutput = jsc(data)

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