package minefarts.smarttube.utils.app.playerdata

import com.eclipsesource.v8.V8ScriptExecutionException
import minefarts.smarttube.google.common.helpers.YouTubeHelper
import minefarts.smarttube.utils.helpers.Helpers
import minefarts.smarttube.utils.app.nsigsolver.common.YouTubeInfoExtractor
import minefarts.smarttube.utils.app.nsigsolver.impl.V8ChallengeProvider
import minefarts.smarttube.utils.app.nsigsolver.provider.ChallengeInput
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeRequest
import minefarts.smarttube.utils.app.nsigsolver.provider.JsChallengeType
import minefarts.smarttube.utils.service.internal.MediaServiceData

internal class PlayerDataExtractor(val playerUrl: String?) {

    private val fixedPlayerUrl: String = playerUrl?.replace("-es6", "-ias").orEmpty()
    
    private val tag = PlayerDataExtractor::class.java.simpleName
    
    private val data
        get() = MediaServiceData.instance()
    
    private var nFuncCode: Boolean = false
    private var sFuncCode: Boolean = false
    
    private var signatureTimestamp: String? = null    

    init {

        val playerCache = data.playerExtractorCache
        val param = "5cNpZqIJ7ixNqU68Y7S"

        if (playerCache?.playerUrl == playerUrl) {
            signatureTimestamp = playerCache?.signatureTimestamp
            nFuncCode = true
            sFuncCode = true
        }
        
        if (nFuncCode && sFuncCode) {

            V8ChallengeProvider.warmup() // enable hot start

        } else {
            try {
                                
                val result = V8ChallengeProvider.bulkSolve(
                    listOf(
                        JsChallengeRequest(JsChallengeType.N, ChallengeInput(fixedPlayerUrl, listOf(param))),
                        JsChallengeRequest(JsChallengeType.SIG, ChallengeInput(fixedPlayerUrl, listOf(param))),
                    )
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

    fun getSignatureTimestamp(): String? {
        return signatureTimestamp
    }

    fun validate(): Boolean {
        return nFuncCode && sFuncCode && signatureTimestamp != null
    }

    private fun extractNSigReal(nParam: String): String? {
        return bulkSigExtractReal(listOf(nParam), null).first?.firstOrNull()
    }

    private fun extractSigReal(sParam: List<String>): List<String?>? {
        return bulkSigExtractReal(null, sParam).second
    }

    private fun bulkSigExtractReal(nParams: List<String?>?, sParams: List<String?>?): Pair<List<String?>?, List<String?>?> {
        if (Helpers.allNulls(nParams, sParams)) {
            return Pair(null, null)
        }

        var nProcessed: List<String?>? = null
        var sProcessed: List<String?>? = null

        val nRequest = nParams?.takeIf { nFuncCode }?.filterNotNull()?.takeIf { it.isNotEmpty() }?.distinct()?.let {
            JsChallengeRequest(JsChallengeType.N, ChallengeInput(fixedPlayerUrl, it))
        }

        val sRequest = sParams?.takeIf { sFuncCode }?.filterNotNull()?.takeIf { it.isNotEmpty() }?.distinct()?.let {
            JsChallengeRequest(JsChallengeType.SIG, ChallengeInput(fixedPlayerUrl, it))
        }

        val result = V8ChallengeProvider.bulkSolve(listOfNotNull(nRequest, sRequest))

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

    private fun loadPlayer(): String? {
        return YouTubeInfoExtractor.loadPlayerSilent(fixedPlayerUrl)
    }

    private fun fetchAllData() {
        val jsCode = loadPlayer()

        signatureTimestamp = jsCode?.let { CommonExtractor.extractSignatureTimestamp(it) }
    }

    private fun persistAllData() {
        if (validate()) {
            data.playerExtractorCache = PlayerExtractorCache(playerUrl, signatureTimestamp)
        }
    }

}