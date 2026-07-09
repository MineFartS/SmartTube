package minefarts.smarttube.utils.next.v2

import minefarts.smarttube.google.common.converters.gson.WithGson
import minefarts.smarttube.utils.next.v2.gen.UnlocalizedTitleResult
import minefarts.smarttube.utils.next.v2.gen.WatchNextResult
import minefarts.smarttube.utils.next.v2.gen.WatchNextResultContinuation

import retrofit2.Call
import retrofit2.http.*

import okhttp3.RequestBody

@WithGson
public interface WatchNextApi {

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/next")
    fun getWatchNextResult(@Body watchNextQuery: String): Call<WatchNextResult?>

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/next")
    fun getWatchNextResult(
        @Body watchNextQuery: RequestBody,
        @Header("X-Goog-Visitor-Id") visitorId: String
    ): Call<WatchNextResult?>

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/next")
    fun continueWatchNextResult(@Body watchNextQuery: String): Call<WatchNextResultContinuation?>

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/next")
    fun continueWatchNextResult(
        @Body watchNextQuery: String, 
        @Header("X-Goog-Visitor-Id") visitorId: String
    ): Call<WatchNextResultContinuation?>

    @GET("https://www.youtube.com/oembed")
    fun getUnlocalizedTitle(@Query("url") url: String): Call<UnlocalizedTitleResult?>

}