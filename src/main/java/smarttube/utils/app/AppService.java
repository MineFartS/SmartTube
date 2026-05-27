package minefarts.smarttube.utils.app;

import android.content.Context;
import android.util.Base64;

import androidx.annotation.NonNull;

import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.prefs.GlobalPreferences;
import minefarts.smarttube.utils.app.models.AppInfo;
import minefarts.smarttube.utils.app.models.ClientData;
import minefarts.smarttube.utils.app.playerdata.PlayerDataExtractor;
import minefarts.smarttube.exoplayer.ExoMediaSourceFactory;
import minefarts.smarttube.google.common.helpers.RetrofitHelper;
import minefarts.smarttube.utils.service.internal.MediaServiceData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.security.SecureRandom;

import kotlin.Pair;

import retrofit2.Call;
import retrofit2.Response;

public class AppService {

    private static final SecureRandom secureRandom = new SecureRandom();

    private static AppService sInstance;
    
    private String mClientPlaybackNonce;

    private final AppApi mAppApi;

    private AppService() {
        mAppApi = RetrofitHelper.create(AppApi.class);
    }

    public static AppService instance() {
        if (sInstance == null)
            sInstance = new AppService();

        return sInstance;
    }

    /**
     * Extracts signature used in music videos
     */
    public String extractSig(String sParam) {
        if (sParam == null) {
            return null;
        }

        return extractSig(Collections.singletonList(sParam)).get(0);
    }

    /**
     * Extracts signature used in music videos
     */
    public List<String> extractSig(List<String> sParams) {
        if (getPlayerDataExtractor() == null) {
            return null;
        }

        return getPlayerDataExtractor().extractSig(sParams);
    }

    public String extractNSig(String nParam) {
        if (nParam == null || getPlayerDataExtractor() == null) {
            return null;
        }

        return getPlayerDataExtractor().extractNSig(nParam);
    }

    public List<String> extractNSig(List<String> nParams) {
        if (Helpers.allNulls(nParams)) {
            return null;
        }

        List<String> result = new ArrayList<>();

        String previousNParam = null;
        String previousNSig = null;

        for (String nParam : nParams) {
            if (Helpers.equals(nParam, previousNParam)) {
                result.add(previousNSig);
                continue;
            }

            String nSig = extractNSig(nParam);

            result.add(nSig);

            previousNParam = nParam;
            previousNSig = nSig;
        }

        return result;
    }

    public void resetClientPlaybackNonce() {

        byte[] randomBytes = new byte[32];
        
        secureRandom.nextBytes(randomBytes);

        mClientPlaybackNonce = Base64.encodeToString(
            randomBytes, 
            Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP
        );

    }

    /**
     * NOTE: Unique per video info instance<br/>
     * A nonce is a unique value chosen by an entity in a protocol, and it is used to protect that entity against attacks which fall under the very large umbrella of "replay".
     */
    public String getClientPlaybackNonce() {
        
        if (mClientPlaybackNonce == null)
            resetClientPlaybackNonce();

        return mClientPlaybackNonce;
    }

    /**
     * Used in get_video_info
     */
    public String getSignatureTimestamp() {
        if (getPlayerDataExtractor() == null) {
            return null;
        }

        return getPlayerDataExtractor().getSignatureTimestamp();
    }

    @NonNull
    public Context getContext() {
        Context context = GlobalPreferences.isInitialized() ? GlobalPreferences.sInstance.getContext() : null;

        if (context == null) {
            throw new IllegalStateException("The Context isn't initialized yet");
        }

        return context;
    }

    /**
     * Obtains info with respect of anonymous browsing data (visitor cookie)
     */
    protected AppInfo getAppInfo(String userAgent) {
        String visitorCookie = getData().getVisitorCookie();
        Call<AppInfo> wrapper = mAppApi.getAppInfo(userAgent, visitorCookie);
        AppInfo result = null;

        Response<AppInfo> response = RetrofitHelper.getResponse(wrapper);

        if (response != null) {
            //String visitorInfoCookie = RetrofitHelper.getCookie(response, AppConstants.VISITOR_INFO_COOKIE);
            //String visitorPrivacyCookie = RetrofitHelper.getCookie(response, AppConstants.VISITOR_PRIVACY_COOKIE);
            //getData().setVisitorCookie(Helpers.join("; ", visitorInfoCookie, visitorPrivacyCookie));
            getData().setVisitorCookie(RetrofitHelper.getCookies(response));
            result = response.body();
        }

        return result;
    }

    protected ClientData getClientData(String clientUrl) {
        if (clientUrl == null) {
            return null;
        }

        Call<ClientData> wrapper = mAppApi.getClientData(clientUrl);
        ClientData clientData = RetrofitHelper.get(wrapper);

        // Seems that legacy script encountered.
        if (clientData == null) {
            clientData = RetrofitHelper.get(mAppApi.getClientData(getLegacyClientUrl(clientUrl)));
        }

        return clientData;
    }
    
    private static String getLegacyClientUrl(String clientUrl) {
        if (clientUrl == null) {
            return null;
        }

        return clientUrl
                .replace("/dg=0/", "/exm=base/ed=1/")
                .replace("/m=base", "/m=main");
    }

    public void invalidateVisitorData() {
        getData().setVisitorCookie(null);
    }

    public void invalidateCache() {
        // NOP
    }

    public boolean isPlayerCacheActual() {
        return false;
    }

    public String getClientId() {
        // TODO: NPE 1.6K!!!
        ClientData clientData = getClientData();
        return clientData != null ? clientData.getClientId() : null;
    }

    /**
     * Constant used in AuthApi
     */
    public String getClientSecret() {
        return getClientData() != null ? getClientData().getClientSecret() : null;
    }

    /**
     * Used with get_video_info, anonymous search and suggestions
     */
    public String getVisitorData() {
        // TODO: NPE 300!!!
        return getAppInfoData() != null ? getAppInfoData().getVisitorData() : null;
    }

    public String getPlayerUrl() {
        // NOTE: NPE 2.5K
        //return getData().getPlayerUrl() != null ? getData().getPlayerUrl() : mCachedAppInfo != null ? mCachedAppInfo.getPlayerUrl() : null;
        return getAppInfoData() != null ? getAppInfoData().getPlayerUrl() : null;
    }

    public String getClientUrl() {
        // NOTE: NPE 143K!!!
        return getAppInfoData() != null ? getAppInfoData().getClientUrl() : null;
    }

    private AppInfo getAppInfoData() {
        return getAppInfo(ExoMediaSourceFactory.USER_AGENT_TV);
    }

    private ClientData getClientData() {
        return getClientData(getClientUrl());
    }

    public PlayerDataExtractor getPlayerDataExtractor() {
        return new PlayerDataExtractor(getPlayerUrl());
    }

    public void refreshCacheIfNeeded() {
        getAppInfoData();
        getClientData();
        getPlayerDataExtractor();
    }

    protected MediaServiceData getData() {
        return MediaServiceData.instance();
    }

}
