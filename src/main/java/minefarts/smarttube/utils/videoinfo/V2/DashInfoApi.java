package minefarts.smarttube.utils.videoinfo.V2;

import minefarts.smarttube.google.common.converters.regexp.WithRegExp;
import minefarts.smarttube.utils.videoinfo.models.DashInfoContent;
import minefarts.smarttube.utils.videoinfo.models.DashInfoUrl;

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
