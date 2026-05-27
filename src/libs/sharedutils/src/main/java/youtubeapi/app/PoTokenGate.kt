package minefarts.sharedutils.app

import android.os.Build.VERSION
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi

import minefarts.sharedutils.app.potokencloud.PoTokenCloudService
import minefarts.sharedutils.app.potokennp2.misc.PoTokenResult
import minefarts.sharedutils.common.helpers.AppClient
import minefarts.sharedutils.app.potokennp2.misc.PoTokenProvider
import minefarts.sharedutils.helpers.DeviceHelpers
import minefarts.sharedutils.mylogger.Log
import minefarts.sharedutils.app.AppService
import minefarts.sharedutils.app.potokennp2.visitor.VisitorService
import minefarts.sharedutils.app.potokennp2.PoTokenGenerator
import minefarts.sharedutils.app.potokennp2.PoTokenWebView

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private enum class PoTokenType {
    /**
     * A poToken generated from videoId.
     *
     * Used in player requests.
      */
    CONTENT,

    /**
     * A generic poToken.
     *
     * Used in SABR requests.
     */
    SESSION
}

internal object PoTokenGate {

    val TAG = PoTokenGate::class.simpleName
    private val webViewSupported by lazy { DeviceHelpers.isWebViewSupported() }
    private var webViewBadImpl = false // whether the system has a bad WebView implementation

    private object WebPoTokenGenLock
    private var webPoTokenVisitorData: String? = null
    private var webPoTokenStreamingPot: String? = null
    private var webPoTokenGenerator: PoTokenGenerator? = null

    private var mWebPoToken: PoTokenResult? = null
    private var mCacheResetTimeMs: Long = -1

    @RequiresApi(19)
    private fun getWebClientPoToken(
        videoId: String, 
        forceRecreate: Boolean
    ): PoTokenResult {

        // just a helper class since Kotlin does not have builtin support for 4-tuples
        data class Quadruple<T1, T2, T3, T4>(val t1: T1, val t2: T2, val t3: T3, val t4: T4)

        val (poTokenGenerator, visitorData, streamingPot, hasBeenRecreated) =
            synchronized(WebPoTokenGenLock) {
                val shouldRecreate = webPoTokenGenerator == null || webPoTokenVisitorData == null || webPoTokenStreamingPot == null ||
                   forceRecreate || webPoTokenGenerator!!.isExpired()

                if (shouldRecreate) {
                    // IMPORTANT: VisitorService performs network I/O.
                    // getWebClientPoToken() can be called from UI thread,
                    // so we must fetch visitorData off-main.
                    webPoTokenVisitorData = try {
                        val latch = CountDownLatch(1)
                        var visitor: String? = null
                        Thread {
                            try {
                                visitor = VisitorService.getVisitorData()
                            } finally {
                                latch.countDown()
                            }
                        }.start()
                        latch.await(5, TimeUnit.SECONDS)
                        visitor
                    } catch (_: Throwable) {
                        null
                    }


                    val latch = if (webPoTokenGenerator != null) CountDownLatch(1) else null

                    // close the current webPoTokenGenerator on the main thread
                    webPoTokenGenerator?.let {
                        Handler(Looper.getMainLooper()).post {
                            try {
                                it.close()
                            } finally {
                                latch?.countDown()
                            }
                        }
                    }

                    latch?.await(3, TimeUnit.SECONDS)

                    // create a new webPoTokenGenerator
                    webPoTokenGenerator = PoTokenWebView
                        .newPoTokenGenerator(AppService.instance().context)

                    // The streaming poToken needs to be generated exactly once before generating
                    // any other (player) tokens.
                    webPoTokenStreamingPot = webPoTokenGenerator!!
                        .generatePoToken(webPoTokenVisitorData!!)
                }

                return@synchronized Quadruple(
                    webPoTokenGenerator!!,
                    webPoTokenVisitorData!!,
                    webPoTokenStreamingPot!!,
                    shouldRecreate
                )
            }

        val playerPot = try {
            // Not using synchronized here, since poTokenGenerator would be able to generate
            // multiple poTokens in parallel if needed. The only important thing is for exactly one
            // visitorData/streaming poToken to be generated before anything else.
            if (videoId.isEmpty()) "" else poTokenGenerator.generatePoToken(videoId)
        } catch (throwable: Throwable) {
            if (hasBeenRecreated) {
                // the poTokenGenerator has just been recreated (and possibly this is already the
                // second time we try), so there is likely nothing we can do
                throw throwable
            } else {
                // retry, this time recreating the [webPoTokenGenerator] from scratch;
                // this might happen for example if NewPipe goes in the background and the WebView
                // content is lost
                Log.e(TAG, "Failed to obtain poToken, retrying", throwable)
                return getWebClientPoToken(videoId = videoId, forceRecreate = true)
            }
        }

        Log.d(
            TAG,
            "poToken for $videoId: playerPot=$playerPot, " +
                    "streamingPot=$streamingPot, visitor_data=$visitorData"
        )

        return PoTokenResult(videoId, visitorData, playerPot, streamingPot)
    }

    @JvmStatic
    public fun isWebPotExpired() = isWebPotSupported() && webPoTokenGenerator?.isExpired() ?: true

    @JvmStatic
    public fun isWebPotSupported() = VERSION.SDK_INT >= 19 && webViewSupported && !webViewBadImpl

    private fun resetCache() {
        webPoTokenVisitorData = null
        webPoTokenStreamingPot = null
    }

    private fun getWebContentPoToken(videoId: String): String? {
        if (mWebPoToken?.videoId == videoId && !isWebPotExpired()) {
            return mWebPoToken?.playerRequestPoToken
        }

        mWebPoToken = if (isWebPotSupported())
            getWebClientPoToken(videoId, false)
        else null

        return mWebPoToken?.playerRequestPoToken
    }

    private fun getWebSessionPoToken(): String? {
        return if (isWebPotSupported()) {
            if (mWebPoToken == null)
                mWebPoToken = getWebClientPoToken("", false)
            mWebPoToken?.streamingDataPoToken
        } else PoTokenCloudService.getPoToken()
    }
    
    private fun updatePoToken() {
        if (isWebPotSupported()) {
            //mNpPoToken = null // only refresh
            mWebPoToken = getWebClientPoToken("", false) // refresh and preload
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

    fun getWebVisitorData(): String? {
        return mWebPoToken?.visitorData
    }

    private fun resetWebCache(): Boolean {
        val currentTimeMs = System.currentTimeMillis()
        if (currentTimeMs < mCacheResetTimeMs)
            return false

        if (isWebPotSupported()) {
            mWebPoToken = null
            resetCache()
        } else
            PoTokenCloudService.resetCache()

        mCacheResetTimeMs = currentTimeMs + 60_000

        return true
    }

}