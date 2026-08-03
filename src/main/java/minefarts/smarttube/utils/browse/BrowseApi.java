package minefarts.smarttube.utils.browse;

import minefarts.smarttube.utils.browse.models.grid.GridTabContinuation;
import minefarts.smarttube.utils.browse.models.guide.Guide;
import minefarts.smarttube.utils.browse.models.sections.SectionContinuation;
import minefarts.smarttube.utils.browse.models.grid.GridTabList;
import minefarts.smarttube.utils.browse.models.sections.SectionList;
import minefarts.smarttube.utils.browse.models.sections.SectionTabContinuation;
import minefarts.smarttube.utils.browse.models.sections.SectionTabList;
import minefarts.smarttube.google.common.converters.jsonpath.WithJsonPath;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * For signed users!
 */
@WithJsonPath
public interface BrowseApi {
    
    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/browse")
    Call<GridTabList> getGridTabList(@Body String browseQuery);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/browse")
    Call<GridTabContinuation> continueGridTab(@Body String browseQuery);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/browse")
    Call<SectionTabList> getSectionTabList(@Body String browseQuery);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/browse")
    Call<SectionTabList> getSectionTabList(
        @Body String browseQuery, 
        @Header("X-Goog-Visitor-Id") String visitorId
    );

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/browse")
    Call<SectionTabContinuation> continueSectionTab(@Body String browseQuery);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/browse")
    Call<SectionTabContinuation> continueSectionTab(
        @Body String browseQuery, 
        @Header("X-Goog-Visitor-Id") String visitorId
    );

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/browse")
    Call<SectionContinuation> continueSection(@Body String browseQuery);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/browse")
    Call<SectionContinuation> continueSection(
        @Body String browseQuery, 
        @Header("X-Goog-Visitor-Id") String visitorId
    );

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/browse")
    Call<SectionList> getSectionList(@Body String browseQuery);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/browse")
    Call<SectionList> getSectionList(
        @Body String browseQuery, 
        @Header("X-Goog-Visitor-Id") String visitorId
    );

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/guide")
    Call<Guide> getGuide(@Body String browseQuery);

}
