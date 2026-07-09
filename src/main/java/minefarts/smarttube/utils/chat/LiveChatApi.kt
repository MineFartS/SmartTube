package minefarts.smarttube.utils.chat

import minefarts.smarttube.utils.chat.gen.LiveChatResult
import minefarts.smarttube.google.common.converters.gson.WithGson
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

@WithGson
public interface LiveChatApi {

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/live_chat/get_live_chat")
    fun getLiveChat(@Body chatQuery: String?): Call<LiveChatResult?>

}