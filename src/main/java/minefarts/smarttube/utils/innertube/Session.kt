package minefarts.smarttube.utils.innertube

import minefarts.smarttube.google.common.converters.gson.WithGson
import minefarts.smarttube.google.common.converters.jsonpath.WithJsonPathSkip
import minefarts.smarttube.google.common.helpers.RetrofitHelper
import minefarts.smarttube.utils.innertube.helpers.ApiHelpers
import minefarts.smarttube.utils.innertube.helpers.URLS
import minefarts.smarttube.utils.innertube.models.InnertubeConfigResult
import minefarts.smarttube.utils.innertube.models.InnertubeContext
import minefarts.smarttube.utils.innertube.models.SessionArgs
import minefarts.smarttube.utils.innertube.models.SessionData
import minefarts.smarttube.utils.innertube.models.SessionDataResult

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST

@WithJsonPathSkip
private interface SessionApi {
    @Headers(
        "Accept: */*",
        "Referer: ${URLS.YT_BASE}/sw.js"
    )
    @GET("${URLS.YT_BASE}/sw.js_data")
    fun getSessionData(@HeaderMap headers: Map<String, String>): Call<SessionDataResult?>
}

@WithGson
private interface InnertubeConfigApi {

    @Headers(
        "Content-Type: application/json",
        "Accept: */*",
        "Referer: ${URLS.YT_BASE}",
        "X-Origin: ${URLS.YT_BASE}"
    )
    @POST("${URLS.API.PRODUCTION_1}v1/config")
    fun retrieveInnertubeConfig(
        @HeaderMap headers: Map<String, String>, 
        @Body jsonConfig: String
    ): Call<InnertubeConfigResult?>

}

public class Session private constructor(
    val context: InnertubeContext,
    val apiKey: String,
    val apiVersion: String,
    val accountIndex: Int,
    val configData: String?,
    val userAgent: String,
    val player: Player? = null,
    val cookie: String? = null,
    //cache?: ICache,
    val poToken: String? = null
) {
    companion object {

        private val sessionApi = RetrofitHelper.create(SessionApi::class.java)

        private val innertubeConfigApi = RetrofitHelper.create(InnertubeConfigApi::class.java)

        fun create(options: SessionOptions? = null): Session {

            val (apiKey, apiVersion, configData, context, userAgent, accountIndex) = getSessionData()

            return Session(context, apiKey, apiVersion, accountIndex, configData, userAgent, Player.create(options?.playerId))
        }

        fun getSessionData(): SessionData {

            val sessionData = getSessionDataResult()
            val deviceInfo = sessionData!!.deviceInfo

            val options = SessionArgs()
            val context = InnertubeContext(options, deviceInfo!!) // builds the context!

            val innertubeConfig = retrieveInnertubeConfig(sessionData, context)
            val coldConfigData = innertubeConfig?.responseContext?.globalConfigGroup?.rawColdConfigGroup?.configData
            val coldHashData = innertubeConfig?.responseContext?.globalConfigGroup?.coldHashData
            val hotHashData = innertubeConfig?.responseContext?.globalConfigGroup?.hotHashData
            val configData = innertubeConfig?.configData

            return SessionData(
                sessionData.apiKey!!,
                "v1", // Constants.CLIENTS.WEB.API_VERSION
                configData!!,
                context.apply {
                    client.configInfo!!.coldConfigData = coldConfigData!!
                    client.configInfo!!.coldHashData = coldHashData!!
                    client.configInfo!!.hotHashData = hotHashData!!
                }
            )

        }

        fun getSessionDataResult(): SessionDataResult? {
            val sessionDataResult = sessionApi.getSessionData(ApiHelpers.createSessionDataHeaders())
            return RetrofitHelper.get(sessionDataResult)
        }

        fun retrieveInnertubeConfig(sessionData: SessionDataResult, context: InnertubeContext): InnertubeConfigResult? {
            val innertubeConfigResult =
                innertubeConfigApi.retrieveInnertubeConfig(
                    ApiHelpers.createInnertubeConfigHeaders(sessionData),
                    ApiHelpers.createInnertubeJsonConfig(context)
                )
            return RetrofitHelper.get(innertubeConfigResult)
        }
    }
}

public data class SessionOptions(
    val lang: String?,
    val location: String?,
    val userAgent: String?,
    val poToken: String?,
    val playerId: String?,
    val retrieveInnertubeConfig: Boolean = true
    // .....
)