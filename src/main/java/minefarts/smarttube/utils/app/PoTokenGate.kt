package minefarts.smarttube.utils.app

import minefarts.smarttube.utils.app.potoken.PoTokenService
import minefarts.smarttube.utils.app.potokencloud.PoTokenCloudService
import minefarts.smarttube.utils.app.potokennp2.PoTokenProvider
import minefarts.smarttube.utils.app.potokennp2.PoTokenResult
import minefarts.smarttube.utils.app.potokennp2.PoTokenGenerator
import minefarts.smarttube.utils.app.potokennp2.PoTokenWebView
import minefarts.smarttube.utils.common.helpers.AppClient

/**
 * PoTokenType
 *
 * `CONTENT` A poToken generated from videoId.
 * Used in DASH/SABR requests (e.g. `pot` param).
 * Previously used in player requests.
 *
 * `SESSION` A poToken generated from visitorData.
 * Usage is unknown. Previously used in DASH/SABR requests (e.g. `pot` param).
 */
public object PoTokenGate {
    private var mWebPoToken: PoTokenResult? = null
    private var mCacheResetTimeMs: Long = -1

    init {
        PoTokenProvider.poTokenFactory = PoTokenWebView
    }

    private fun getWebContentPoToken(videoId: String): String? {
        if (mWebPoToken?.videoId == videoId && !PoTokenProvider.isWebPotExpired()) {
            return mWebPoToken?.playerRequestPoToken
        }

        mWebPoToken = if (PoTokenProvider.isWebPotSupported())
            PoTokenProvider.getWebClientPoToken(videoId)
        else null

        return mWebPoToken?.playerRequestPoToken
    }

    private fun getWebSessionPoToken(): String? {
        return if (PoTokenProvider.isWebPotSupported()) {
            if (mWebPoToken == null)
                mWebPoToken = PoTokenProvider.getWebClientPoToken("")
            mWebPoToken?.streamingDataPoToken
        } else PoTokenCloudService.getPoToken()
    }
    
    private fun updatePoToken() {
        if (PoTokenProvider.isWebPotSupported()) {
            //mNpPoToken = null // only refresh
            mWebPoToken = PoTokenProvider.getWebClientPoToken("") // refresh and preload
        } else {
            PoTokenCloudService.updatePoToken()
        }
    }

    @JvmStatic
    @JvmOverloads
    fun getPoToken(client: AppClient, videoId: String? = null): String? {
        return when {
            client.isWebPotRequired -> if (videoId != null) getWebContentPoToken(videoId) else getWebSessionPoToken()
            else -> null
        }
    }

    @JvmStatic
    fun getColdStartPoToken(client: AppClient, videoId: String): String? =
        if (client.isWebPotRequired) PoTokenService.generateColdStartToken(videoId) else null

    @JvmStatic
    fun getVisitorData(client: AppClient): String? {
        return when {
            client.isWebPotRequired -> getWebVisitorData()
            else -> null
        }
    }

    @JvmStatic
    fun resetCache(client: AppClient): Boolean {
        return when {
            client.isWebPotRequired -> resetWebCache()
            else -> false
        }
    }

    @JvmStatic
    fun resetCache() {
        resetWebCache()
    }

    fun getWebVisitorData(): String? {
        return mWebPoToken?.visitorData
    }

    private fun resetWebCache(): Boolean {
        val currentTimeMs = System.currentTimeMillis()
        if (currentTimeMs < mCacheResetTimeMs)
            return false

        if (PoTokenProvider.isWebPotSupported()) {
            mWebPoToken = null
            PoTokenProvider.resetCache()
        } else
            PoTokenCloudService.resetCache()

        mCacheResetTimeMs = currentTimeMs + 60_000

        return true
    }
}