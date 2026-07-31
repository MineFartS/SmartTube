package minefarts.smarttube.utils.block;

import minefarts.smarttube.utils.block.data.SegmentList;
import com.liskovsoft.googlecommon.common.converters.jsonpath.WithJsonPath;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

@WithJsonPath
public interface SponsorBlockApi {
 
    @GET("https://sponsor.ajay.app/api/skipSegments")
    Call<SegmentList> getSegments(
        @Query("videoID") String videoId, 
        @Query(value = "categories", encoded = true) String categoriesJsonArray
    );

}
