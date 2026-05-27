package minefarts.sharedutils.comments

import minefarts.sharedutils.comments.gen.CommentsResult
import minefarts.googlecommon.common.converters.gson.WithGson
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

@WithGson
internal interface CommentsApi {

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/next")
    fun getComments(@Body commentsQuery: String): Call<CommentsResult?>

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/comment/perform_comment_action")
    fun commentAction(@Body actionQuery: String): Call<Void?>
    
}