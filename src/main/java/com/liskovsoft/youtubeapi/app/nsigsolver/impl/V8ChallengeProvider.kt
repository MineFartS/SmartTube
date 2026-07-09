package com.liskovsoft.youtubeapi.app.nsigsolver.impl

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8ScriptExecutionException

import com.liskovsoft.youtubeapi.app.nsigsolver.common.YouTubeInfoExtractor
import com.liskovsoft.youtubeapi.app.nsigsolver.provider.ChallengeOutput
import com.liskovsoft.youtubeapi.app.nsigsolver.provider.JsChallengeProviderResponse
import com.liskovsoft.youtubeapi.app.nsigsolver.provider.JsChallengeRequest
import com.liskovsoft.youtubeapi.app.nsigsolver.provider.JsChallengeResponse
import com.liskovsoft.youtubeapi.app.nsigsolver.runtime.SolverOutput
import com.liskovsoft.youtubeapi.app.AppService

public object V8ChallengeProvider {
    
    @JvmField
    public val v8Runtime = ThreadLocal<V8>()

    private val sGson = Gson()

    private val assets
        get() = AppService.instance().context?.assets

    public fun warmup() {/* NOP */}

    @Synchronized
    private fun runV8(stdin: String): String {

        if (v8Runtime.get() == null) {
            
            val runtime = V8.createV8Runtime()

            for (name in listOf("lib", "core")) {

                val path = "nsigsolver/yt.solver.$name.js"

                assets!!.open(path).bufferedReader().use { 
                    runtime!!.executeStringScript(it.readText())
                }
                
            }

            v8Runtime.set(runtime)

        }

        return v8Runtime.get()!!.executeStringScript(stdin)
            ?: throw RuntimeException("V8 runtime error: empty response")

    }

    /**
     * Solve multiple JS challenges and return the results
     */
    fun bulkSolve(requests: List<JsChallengeRequest>): Sequence<JsChallengeProviderResponse> = sequence {
        
        val grouped: Map<String, List<JsChallengeRequest>> = requests.groupBy { it.input.playerUrl }

        for ((playerUrl, groupedRequests) in grouped) {

            val data = mapOf(
                "type" to "player",
                "player" to getPlayer(playerUrl),
                "output_preprocessed" to true,
                "requests" to groupedRequests.map { request -> mapOf(
                    "type" to request.type.value,
                    "challenges" to request.input.challenges
                )}
            )

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

    fun getPlayer(playerUrl: String?): String {

        return try {
            YouTubeInfoExtractor.loadPlayer(playerUrl ?: "")
        } catch (e: Exception) {
            throw RuntimeException("Failed to load player for JS challenge: $playerUrl")
        }

    }

}
