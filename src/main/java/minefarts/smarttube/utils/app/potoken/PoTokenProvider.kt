package minefarts.smarttube.utils.app.potoken

import android.os.Handler
import android.os.Looper

import minefarts.smarttube.utils.app.potoken.visitor.VisitorService
import minefarts.smarttube.utils.helpers.DeviceHelpers
import minefarts.smarttube.utils.mylogger.Log
import minefarts.smarttube.utils.app.AppService

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

public object PoTokenProvider {

    val TAG = PoTokenProvider::class.simpleName
    
    private val webViewSupported by lazy { DeviceHelpers.isWebViewSupported() }
    private var webViewBadImpl = false // whether the system has a bad WebView implementation

    private object WebPoTokenGenLock
    private var poTokenGenerator: PoTokenGenerator? = null
    private var visitorData: String? = null
    private var streamingPot: String? = null
    
    var poTokenFactory: PoTokenGenerator.Factory? = null
    
    fun getWebClientPoToken(videoId: String): PoTokenResult? {
        if (!isWebPotSupported()) {
            return null
        }

        try {
            return getWebClientPoToken2(videoId = videoId)
        } catch (e: RuntimeException) {
            // RxJava's Single wraps exceptions into RuntimeErrors, so we need to unwrap them here
            when (val cause = e.cause) {
                is BadWebViewException -> {
                    Log.e(TAG, "Could not obtain poToken because WebView is broken", e)
                    webViewBadImpl = true
                    return null
                }
                null -> throw e
                else -> throw cause // includes PoTokenException
            }
        }
    }

    /**
     * @param forceRecreate whether to force the recreation of [poTokenGenerator], to be used in
     * case the current [poTokenGenerator] threw an error last time
     * [PoTokenGenerator.generatePoToken] was called
     */
    private fun getWebClientPoToken2(videoId: String): PoTokenResult = synchronized(WebPoTokenGenLock) {
                        
            val shouldRecreate = poTokenGenerator == null 
                    || visitorData == null 
                    || streamingPot == null 
                    || poTokenGenerator!!.isExpired()

            if (shouldRecreate) {
                // MOD: my visitor data
                //visitorData = AppService.instance().visitorData
                visitorData = VisitorService.getVisitorData()

                val latch = if (poTokenGenerator != null) CountDownLatch(1) else null

                // close the current poTokenGenerator on the main thread
                poTokenGenerator?.let {
                    Handler(Looper.getMainLooper()).post {
                        try {
                            it.close()
                        } catch (_: Exception) {
                            // NullPointerException: android.webkit.WebViewClassic.clearHistory (WebViewClassic.java:3670)
                        } finally {
                            latch?.countDown()
                        }
                    }
                }

                latch?.await(3, TimeUnit.SECONDS)

                // create a new poTokenGenerator
                val context = AppService.instance().context
                poTokenGenerator = try {
                    (poTokenFactory ?: PoTokenWebView)
                        .newPoTokenGenerator(context)
                } catch (e: Exception) {
                    when (e) {
                        is BadWebViewException, is PoTokenException -> {
                            // BadWebViewException: Error invoking onRunBotguardResult
                            // PoTokenException: mintCallback is not defined
                            // PoTokenWebView2/3 may fail due to too many requests. Switching to the default variant.
                            if (poTokenFactory != null && poTokenFactory != PoTokenWebView)
                                PoTokenWebView.newPoTokenGenerator(context)
                            else throw e
                        }
                        else -> throw e
                    }
                }

                // The streaming poToken needs to be generated exactly once before generating
                // any other (player) tokens.
                streamingPot = poTokenGenerator!!
                    .generatePoToken(visitorData!!)
            }

        val playerPot = if (videoId.isEmpty()) "" else poTokenGenerator!!.generatePoToken(videoId)

        Log.d(
            TAG,
            "poToken for $videoId:\nplayerPot=$playerPot\nstreamingPot=$streamingPot\nvisitor_data=$visitorData"
        )

        return PoTokenResult(videoId, visitorData!!, playerPot, streamingPot)
    }

    fun isWebPotExpired() = isWebPotSupported() && poTokenGenerator?.isExpired() ?: true

    @JvmStatic
    fun isWebPotSupported() = webViewSupported && !webViewBadImpl

    fun resetCache() {
        visitorData = null
        streamingPot = null
    }
}