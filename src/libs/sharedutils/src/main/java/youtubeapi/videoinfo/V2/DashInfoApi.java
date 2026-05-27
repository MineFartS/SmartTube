package minefarts.sharedutils.videoinfo.V2;

import minefarts.googlecommon.common.converters.regexp.WithRegExp;
import minefarts.sharedutils.videoinfo.models.DashInfoContent;
import minefarts.sharedutils.videoinfo.models.DashInfoUrl;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

@WithRegExp
public interface DashInfoApi {
    @GET
    Call<DashInfoUrl> getDashInfoUrl(@Url String url);

    @GET
    Call<DashInfoContent> getDashInfoContent(@Url String url);
}
