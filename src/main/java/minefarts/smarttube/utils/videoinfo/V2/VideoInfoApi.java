package minefarts.smarttube.utils.videoinfo.V2;

import minefarts.smarttube.google.common.converters.jsonpath.WithJsonPath;
import minefarts.smarttube.utils.videoinfo.models.VideoInfo;
import minefarts.smarttube.utils.videoinfo.models.VideoInfoHls;
import minefarts.smarttube.utils.videoinfo.models.VideoInfoReel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

@WithJsonPath
public interface VideoInfoApi {
    
    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/player")
    Call<VideoInfo> getVideoInfo(
        @Body String videoQuery, 
        @Header("x-goog-visitor-id") String visitorId, 
        @Header("User-Agent") String userAgent
    );

    @Headers("Content-Type: application/json")
    @POST("https://youtubei.googleapis.com/youtubei/v1/reel/reel_item_watch")
    Call<VideoInfoReel> getVideoInfoReel(
        @Body String videoQuery, 
        @Header("x-goog-visitor-id") String visitorId, 
        @Header("User-Agent") String userAgent
    );

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/player")
    Call<VideoInfoHls> getVideoInfoHls(
        @Body String videoQuery, 
        @Header("x-goog-visitor-id") String visitorId
    );

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/player")
    Call<VideoInfo> getVideoInfo(
        @Body String videoQuery
    );

    @Headers({
            "Content-Type: application/json",
            "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    })
    @POST("https://www.youtube.com/youtubei/v1/player")
    Call<VideoInfo> getVideoInfoWeb(
        @Body String videoQuery, 
        @Header("x-goog-visitor-id") String visitorId
    );
    
}
