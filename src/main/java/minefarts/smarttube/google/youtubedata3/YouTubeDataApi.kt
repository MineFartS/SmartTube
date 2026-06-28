package minefarts.smarttube.google.youtubedata3

import minefarts.smarttube.utils.SignInService
import minefarts.smarttube.google.youtubedata3.data.SnippetResponse
import minefarts.smarttube.google.common.converters.gson.WithGson
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

@WithGson
public interface YouTubeDataApi {
    
    @GET("https://www.googleapis.com/youtube/v3/channels?part=snippet&key=")
    fun getChannelMetadata(@Query("id") ids: String): Call<SnippetResponse?>

    @GET("https://www.googleapis.com/youtube/v3/videos?part=snippet,contentDetails&key=")
    fun getVideoMetadata(@Query("id") ids: String): Call<SnippetResponse?>

    @GET("https://www.googleapis.com/youtube/v3/playlists?part=snippet,contentDetails&key=")
    fun getPlaylistMetadata(@Query("id") ids: String): Call<SnippetResponse?>
}