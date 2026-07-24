package minefarts.smarttube.utils.app.nsigsolver.impl

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8ScriptExecutionException

import com.liskovsoft.youtubeapi.app.nsigsolver.impl.V8ChallengeProvider
import com.liskovsoft.youtubeapi.app.nsigsolver.provider.ChallengeOutput
import com.liskovsoft.youtubeapi.app.nsigsolver.provider.JsChallengeProviderResponse
import com.liskovsoft.youtubeapi.app.nsigsolver.provider.JsChallengeRequest
import com.liskovsoft.youtubeapi.app.nsigsolver.provider.JsChallengeResponse
import com.liskovsoft.youtubeapi.app.nsigsolver.provider.JsChallengeType
import com.liskovsoft.youtubeapi.app.nsigsolver.runtime.SolverOutput

import minefarts.smarttube.ContextManager
import minefarts.smarttube.utils.mylogger.Log

public object V8ChallengeProviderShim {

    private val sGson = Gson()

    private val assets
        get() = ContextManager.get()?.assets

    fun bulkSolve(vararg requests: JsChallengeRequest): Sequence<JsChallengeProviderResponse> = sequence {

        for ((playerUrl, groupedRequests) in requests.groupBy{it.input.playerUrl}) {

            val data = mutableMapOf<String, Any?>(
                "type" to "player",
                "player" to V8ChallengeProvider.getPlayer(playerUrl),
                "output_preprocessed" to true,
            )

            data["requests"] = groupedRequests.map { request -> mapOf(
                "type" to request.type.value,
                "challenges" to request.input.challenges
            )}

            V8ChallengeProvider.initRuntime()
            
            for (name in listOf("lib", "core")) {
                assets!!.open("nsigsolver/yt.solver.$name.js").bufferedReader().use { 
                    V8ChallengeProvider.runV8(it.readText())
                }
            }

            val output: SolverOutput = sGson.fromJson(
                V8ChallengeProvider.runV8("JSON.stringify( jsc(${sGson.toJson(data)}) )"),
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