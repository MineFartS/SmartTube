package com.liskovsoft.sharedutils.actions;

import com.liskovsoft.sharedutils.actions.models.ActionResult;
import com.liskovsoft.googlecommon.common.converters.jsonpath.WithJsonPath;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

@WithJsonPath
public interface ActionsApi {

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/like/like")
    Call<ActionResult> setLike(@Body String actionQuery);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/like/removelike")
    Call<ActionResult> removeLike(@Body String actionQuery);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/like/dislike")
    Call<ActionResult> setDislike(@Body String actionQuery);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/like/removedislike")
    Call<ActionResult> removeDislike(@Body String actionQuery);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/subscription/subscribe")
    Call<ActionResult> subscribe(@Body String actionQuery);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/subscription/unsubscribe")
    Call<ActionResult> unsubscribe(@Body String actionQuery);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/history/clear_search_history")
    Call<Void> clearSearchHistory(@Body String historyQuery);
    
}
