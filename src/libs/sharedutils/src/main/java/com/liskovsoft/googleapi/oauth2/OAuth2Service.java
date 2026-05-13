package com.liskovsoft.googleapi.oauth2;

import com.liskovsoft.googlecommon.common.helpers.RetrofitHelper;
import com.liskovsoft.googlecommon.common.models.auth.AccessToken;
import com.liskovsoft.googlecommon.common.models.auth.UserCode;
import com.liskovsoft.sharedutils.mylogger.Log;

import retrofit2.Call;

public class OAuth2Service {

    public static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:device_code";
    public static final String GRANT_TYPE_REFRESH = "refresh_token";

    public static final String CLIENT_ID = "";
    public static final String CLIENT_SECRET = "";
    public static final String REFRESH_TOKEN = "";
    public static final String DEVICE_CODE = "";
    public static final String YOUTUBE_DATA_API_KEY = "";

    public static final String DRIVE_SCOPE = "https://www.googleapis.com/auth/drive.file";
    public static final String YOUTUBE_SCOPE = "https://www.googleapis.com/auth/youtube https://www.googleapis.com/auth/youtube.readonly";
    public static final String SIGN_IN_DATA_SCOPE = "email openid profile";

    private static final String TAG = OAuth2Service.class.getSimpleName();
    private static OAuth2Service sInstance;
    private final OAuth2Api mOAuth2Api;
    private static final int REFRESH_TOKEN_ATTEMPTS = 200;
    private static final long REFRESH_TOKEN_ATTEMPT_INTERVAL_MS = 5_000;

    private OAuth2Service() {
        mOAuth2Api = RetrofitHelper.create(OAuth2Api.class);
    }

    public static OAuth2Service instance() {
        if (sInstance == null) {
            sInstance = new OAuth2Service();
        }

        return sInstance;
    }

    /**
     * Returns user code that user should apply on the page<br/>
     * <a href=https://youtube.com/activate>https://youtube.com/activate</a>
     * @return response with user code and device code
     */
    public UserCode getUserCode() {
        Call<UserCode> wrapper = mOAuth2Api.getUserCode(
            CLIENT_ID, 
            DRIVE_SCOPE
        );
        return RetrofitHelper.get(wrapper);
    }

    /**
     * Note, before calling this method user should apply the 'user code' on the page<br/>
     * <a href=https://youtube.com/activate>https://youtube.com/activate</a>
     * @param deviceCode the code contained inside the response of the method {@link #getUserCode()}
     * @return refresh token that should be stored inside the app registry for future use
     */
    private AccessToken getAccessToken(String deviceCode) {
        Call<AccessToken> wrapper = mOAuth2Api.getAccessToken(CLIENT_ID, CLIENT_SECRET, deviceCode, GRANT_TYPE);
        return RetrofitHelper.get(wrapper);
    }

    /**
     * Returns temporal access token that should be refreshed after some period of time
     * @param refreshToken token obtained from previous method
     * @return temporal access token
     */
    public AccessToken updateAccessToken(String refreshToken) {
        
        Call<AccessToken> wrapper = mOAuth2Api.updateAccessToken(
            CLIENT_ID, 
            CLIENT_SECRET, 
            GRANT_TYPE_REFRESH, 
            refreshToken
        );

        return RetrofitHelper.getWithErrors(wrapper);
    }

    public AccessToken getAccessTokenWait(String deviceCode) throws InterruptedException {
        AccessToken tokenResult = null;

        for (int i = 0; i < REFRESH_TOKEN_ATTEMPTS; i++) {
            Thread.sleep(REFRESH_TOKEN_ATTEMPT_INTERVAL_MS);

            tokenResult = getAccessToken(deviceCode);

            if (tokenResult != null && tokenResult.getRefreshToken() != null) {
                break;
            }
        }

        if (tokenResult != null && tokenResult.getRefreshToken() != null) {
            return tokenResult;
        } else {
            String msg = String.format("Error. Refresh token is empty!\nDebug data: device code: %s, client id: %s, client secret: %s\nError msg: %s",
                    deviceCode,
                    null, // mAppService.getClientId()
                    null, // mAppService.getClientSecret()
                    tokenResult != null ? tokenResult.getError() : "");

            Log.e(TAG, msg);
            throw new IllegalStateException(msg);
        }
    }

}
