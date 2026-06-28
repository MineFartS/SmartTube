package minefarts.smarttube.utils.comments

import minefarts.smarttube.utils.comments.gen.CommentsResult
import minefarts.smarttube.google.common.converters.gson.WithGson
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

@WithGson
public interface CommentsApi {

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/next")
    fun getComments(@Body commentsQuery: String): Call<CommentsResult?>

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/comment/perform_comment_action")
    fun commentAction(@Body actionQuery: String): Call<Void?>
    
}