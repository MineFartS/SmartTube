package minefarts.sharedutils.chat

import minefarts.sharedutils.chat.gen.LiveChatResult
import minefarts.googlecommon.common.converters.gson.WithGson
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

@WithGson
internal interface LiveChatApi {

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/live_chat/get_live_chat")
    fun getLiveChat(@Body chatQuery: String?): Call<LiveChatResult?>

}