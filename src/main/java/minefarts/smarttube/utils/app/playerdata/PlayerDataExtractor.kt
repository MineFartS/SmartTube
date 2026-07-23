package minefarts.smarttube.utils.app.playerdata

import com.eclipsesource.v8.V8ScriptExecutionException
import minefarts.smarttube.google.common.helpers.YouTubeHelper
import minefarts.smarttube.utils.helpers.Helpers
import minefarts.smarttube.utils.app.nsigsolver.impl.V8ChallengeProviderShim
import minefarts.smarttube.utils.app.nsigsolver.provider.ChallengeInput
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeRequest
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeType
import minefarts.smarttube.utils.service.internal.MediaServiceData

public class PlayerDataExtractor(val playerUrl: String?) {

    private val fixedPlayerUrl: String = playerUrl?.replace("-es6", "-ias").orEmpty()
    
    private val data
        get() = MediaServiceData.instance()
    
    private var nFuncCode: Boolean = false
    private var sFuncCode: Boolean = false
    
    var signatureTimestamp: String? = null    

    init {

        val playerCache = data.playerExtractorCache
        val param = "5cNpZqIJ7ixNqU68Y7S"

        if (playerCache?.playerUrl == playerUrl) {
            signatureTimestamp = playerCache?.signatureTimestamp
            nFuncCode = true
            sFuncCode = true
        }
        
        if (!nFuncCode || !sFuncCode) {
            try {
                                
                val result = V8ChallengeProviderShim.bulkSolve(
                    JsChallengeRequest(JsChallengeType.N, ChallengeInput(fixedPlayerUrl, listOf(param))),
                    JsChallengeRequest(JsChallengeType.SIG, ChallengeInput(fixedPlayerUrl, listOf(param)))
                )

                for (item in result) {
                    when (item.response?.type) {
                        JsChallengeType.N ->
                            if (item.response.output.results[param]?.let { it != param } ?: false)
                                nFuncCode = true
                        JsChallengeType.SIG ->
                            if (item.response.output.results[param]?.let { it != param } ?: false)
                                sFuncCode = true
                        else -> {}
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    fun extractNSig(nParam: String): String? {
        return bulkSigExtract(listOf(nParam), null).first?.firstOrNull()
    }

    fun extractSig(sParams: List<String?>): List<String?>? {
        return bulkSigExtract(null, sParams).second
    }

    fun bulkSigExtract(nParams: List<String?>?, sParams: List<String?>?): Pair<List<String?>?, List<String?>?> {
        if (Helpers.allNulls(nParams, sParams)) {
            return Pair(null, null)
        }

        val response = bulkSigExtractReal(nParams, sParams)

        return Pair(response.first, response.second)
    }

    fun validate(): Boolean {
        return nFuncCode && sFuncCode && signatureTimestamp != null
    }

    private fun buildRequest(
        params: List<String?>?,
        funcCode: Boolean,
        type: JsChallengeType
    ): JsChallengeRequest? {
        return params?.takeIf { funcCode }?.filterNotNull()?.takeIf { it.isNotEmpty() }?.distinct()?.let {
            JsChallengeRequest(type, ChallengeInput(fixedPlayerUrl, it))
        }
    }

    private fun bulkSigExtractReal(
        nParams: List<String?>?, 
        sParams: List<String?>?
    ): Pair<List<String?>?, List<String?>?> {

        if (Helpers.allNulls(nParams, sParams))
            return Pair(null, null)
    
        val requests = listOfNotNull(
            buildRequest(nParams, nFuncCode, JsChallengeType.N),
            buildRequest(sParams, sFuncCode, JsChallengeType.SIG)
        ).toTypedArray()
                
        val result = V8ChallengeProviderShim.bulkSolve(*requests)
                
        var nProcessed: List<String?>? = null
        var sProcessed: List<String?>? = null

        for (item in result) {
            when (item.response?.type) {
                JsChallengeType.N ->
                    nProcessed = nParams?.map { item.response.output.results[it] }
                JsChallengeType.SIG ->
                    sProcessed = sParams?.map { item.response.output.results[it] }
                else -> {}
            }
        }

        return Pair(nProcessed, sProcessed)
    }

}