package minefarts.smarttube.utils.app.nsigsolver.impl

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8ScriptExecutionException

import com.liskovsoft.youtubeapi.app.nsigsolver.impl.V8ChallengeProvider

import minefarts.smarttube.ContextManager
import minefarts.smarttube.utils.mylogger.Log
import minefarts.smarttube.utils.app.nsigsolver.provider.ChallengeOutput
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeProviderResponse
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeRequest
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeResponse
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeType
import minefarts.smarttube.utils.app.nsigsolver.runtime.SolverOutput

public object V8ChallengeProviderShim {

    private val sGson = Gson()

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
            val stdin = "JSON.stringify( jsc(${sGson.toJson(data)}) )"

            val output: SolverOutput = sGson.fromJson(
                V8ChallengeProvider.runV8(stdin),
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