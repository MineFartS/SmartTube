package com.liskovsoft.googleapi.youtubedata3

import com.liskovsoft.googleapi.oauth2.OAuth2Service
import com.liskovsoft.googleapi.youtubedata3.data.SnippetResponse
import com.liskovsoft.googlecommon.common.converters.gson.WithGson
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

@WithGson
internal interface YouTubeDataApi {
    
    @GET("https://www.googleapis.com/youtube/v3/channels?part=snippet&key=${OAuth2Service.YOUTUBE_DATA_API_KEY}")
    fun getChannelMetadata(@Query("id") ids: String): Call<SnippetResponse?>

    @GET("https://www.googleapis.com/youtube/v3/videos?part=snippet,contentDetails&key=${OAuth2Service.YOUTUBE_DATA_API_KEY}")
    fun getVideoMetadata(@Query("id") ids: String): Call<SnippetResponse?>

    @GET("https://www.googleapis.com/youtube/v3/playlists?part=snippet,contentDetails&key=${OAuth2Service.YOUTUBE_DATA_API_KEY}")
    fun getPlaylistMetadata(@Query("id") ids: String): Call<SnippetResponse?>
}