package minefarts.smarttube.google.common.helpers

import minefarts.smarttube.utils.okhttp.OkHttpManager
import minefarts.smarttube.utils.common.helpers.AppConstants
import minefarts.smarttube.utils.app.AppService
import minefarts.smarttube.exoplayer.ExoMediaSourceFactory

import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

object RetrofitOkHttpHelper {

    private val authSkipList = mutableListOf<Request>()

    @JvmStatic
    val authHeaders = mutableMapOf<String, String>()

    @JvmStatic
    fun addAuthSkip(request: Request) {
        if (!authSkipList.contains(request))
            authSkipList.add(request)
    }

    @JvmStatic
    val client: OkHttpClient by lazy {
        
        val builder = OkHttpManager.instance().client.newBuilder()
        
        builder.addInterceptor {chain ->

            val request = chain.request()
            val headers = request.headers()
            val requestBuilder = request.newBuilder()

            applyHeaders(
                this.commonHeaders, 
                headers, 
                requestBuilder
            )

            val url = request.url().toString()

            if (apiPrefixes.any { url.startsWith(it) }) {
                
                val doSkipAuth = authSkipList.remove(request)

                // Empty Home fix (anonymous user) and improve Recommendations for everyone
                if (visitorApiSuffixes.any { url.contains(it) })
                    
                headers["X-Goog-Visitor-Id"] ?: AppService.instance().visitorData?.let { 
                    requestBuilder.header("X-Goog-Visitor-Id", it) 
                }

                applyHeaders(
                    this.apiHeaders, 
                    headers, 
                    requestBuilder
                )

                val tParam = if (tParamSuffixes.any { url.contains(it) }) YouTubeHelper.generateTParameter() else null

                if (authHeaders.isEmpty() || doSkipAuth) {
                    applyQueryKeys(
                        mapOf(
                            "key" to AppConstants.API_KEY, 
                            "prettyPrint" to "false", 
                            "t" to tParam
                        ),
                        request, 
                        requestBuilder
                    )
                } else {
                    
                    applyQueryKeys(
                        mapOf(
                            "prettyPrint" to "false", 
                            "t" to tParam
                        ), 
                        request, 
                        requestBuilder
                    )
                    
                    applyHeaders(
                        authHeaders, 
                        headers, 
                        requestBuilder
                    )

                }
            }

            chain.proceed(requestBuilder.build())
        }

        builder.build()
    }

    private val commonHeaders = mapOf(
        // Enable compression in production
        "Accept-Encoding" to "gzip, deflate, br",
    )

    private val apiHeaders = mapOf(
        "User-Agent" to ExoMediaSourceFactory.USER_AGENT_TV,
        "Referer" to "https://www.youtube.com/tv"
    )

    private val apiPrefixes = arrayOf(
        "https://www.googleapis.com/upload/drive/v3",
        "https://www.googleapis.com/drive/v3",
        "https://m.youtube.com/youtubei/v1/",
        "https://www.youtube.com/youtubei/v1/",
        "https://youtubei.googleapis.com/youtubei/v1",
        "https://www.youtube.com/api/stats/",
        "https://clients1.google.com/complete/"
    )

    // NOTE: visitor header could broke many apis. E.g. VisitorService
    private val visitorApiSuffixes = arrayOf(
        "/youtubei/v1/browse",
        "/youtubei/v1/search",
        "/youtubei/v1/player",
        "/youtubei/v1/reel/",
        "/youtubei/v1/next",
        "/api/stats/",
    )

    private val tParamSuffixes = listOf("/browse", "/next", "/reel", "/playlist")

    private fun applyHeaders(
        newHeaders: Map<String, String?>, 
        oldHeaders: Headers, 
        builder: Request.Builder
    ) {
        for (header in newHeaders) {
            // Don't override existing headers
            oldHeaders[header.key] ?: header.value?.let { builder.header(header.key, it) } // NOTE: don't remove null check
        }
    }

    private fun applyQueryKeys(keys: Map<String, String?>, request: Request, builder: Request.Builder) {
        val originUrl = request.url()

        var newUrlBuilder: HttpUrl.Builder? = null

        for (entry in keys) {
            // Don't override existing keys
            originUrl.queryParameter(entry.key) ?: run {
                if (entry.value == null)
                    return@run

                if (newUrlBuilder == null) {
                    newUrlBuilder = originUrl.newBuilder()
                }

                newUrlBuilder?.addQueryParameter(entry.key, entry.value)
            }
        }

        newUrlBuilder?.run {
            builder.url(build())
        }
    }

}