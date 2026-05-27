package minefarts.smarttube.utils

import minefarts.smarttube.google.common.converters.regexp.WithRegExp
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

@WithRegExp
internal interface UtilsApi {
    @GET("https://www.youtube.com/{oldChannelId}")
    fun canonicalChannelId(@Path("oldChannelId") altChannelId: String): Call<ChannelId>?
}