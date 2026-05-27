package minefarts.smarttube.utils.auth.V2;

import minefarts.smarttube.google.common.models.auth.AccessToken;
import minefarts.smarttube.google.common.models.auth.UserCode;
import minefarts.smarttube.google.common.models.auth.info.AccountsList;
import minefarts.smarttube.google.common.converters.jsonpath.WithJsonPath;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

@WithJsonPath
public interface AuthApi {

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/o/oauth2/device/code")
    Call<UserCode> getUserCode(@Body String authQuery);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/o/oauth2/token")
    Call<AccessToken> getAccessToken(@Body String authQuery);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/o/oauth2/token")
    Call<AccessToken> updateAccessToken(@Body String authQuery);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/account/accounts_list")
    Call<AccountsList> getAccountsList(@Body String authQuery);

}
