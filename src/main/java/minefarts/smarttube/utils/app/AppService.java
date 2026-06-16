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
import minefarts.smarttube.ContextManager;

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

    AppApi mAppApi;
    MediaServiceData mData;

    public static AppService instance() {
 
        if (sInstance == null) {
            sInstance = new AppService();
            sInstance.mAppApi = RetrofitHelper.create(AppApi.class);
            sInstance.mData = MediaServiceData.instance();
        }

        return sInstance;
    }

    /**
     * Extracts signature used in music videos
     */
    public String extractSig(String sParam) {
        if (sParam == null) return null;

        return extractSig(Collections.singletonList(sParam)).get(0);
    }

    /**
     * Extracts signature used in music videos
     */
    public List<String> extractSig(List<String> sParams) {
        if (getPlayerDataExtractor() == null) return null;

        return getPlayerDataExtractor().extractSig(sParams);
    }

    public String extractNSig(String nParam) {
        if (nParam == null || getPlayerDataExtractor() == null) return null;

        return getPlayerDataExtractor().extractNSig(nParam);
    }

    public List<String> extractNSig(List<String> nParams) {
        if (Helpers.allNulls(nParams)) return null;

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

    @NonNull
    public Context getContext() {

        if (GlobalPreferences.isInitialized())
            ContextManager.set(GlobalPreferences.sInstance.getContext());

        if (ContextManager.get() == null)
            throw new IllegalStateException("The Context isn't initialized yet");

        return ContextManager.get();
    }

    protected ClientData getClientData(String clientUrl) {
        if (clientUrl == null) return null;

        Call<ClientData> wrapper = mAppApi.getClientData(clientUrl);
        ClientData clientData = RetrofitHelper.get(wrapper);

        String legacyClientUrl = clientUrl
            .replace("/dg=0/", "/exm=base/ed=1/")
            .replace("/m=base", "/m=main");

        // Seems that legacy script encountered.
        if (clientData == null) {
            clientData = RetrofitHelper.get(
                mAppApi.getClientData(legacyClientUrl)
            );
        }

        return clientData;
    }

    public String getClientId() {
        ClientData clientData = getClientData(getClientUrl());
        return clientData != null ? clientData.getClientId() : null;
    }

    /**
     * Constant used in AuthApi
     */
    public String getClientSecret() {
        ClientData clientData = getClientData(getClientUrl());
        return clientData != null ? clientData.getClientSecret() : null;
    }

    /**
     * Used with get_video_info, anonymous search and suggestions
     */
    public String getVisitorData() {
        AppInfo appInfo = getAppInfoData();
        return appInfo != null ? appInfo.mVisitorData : null;
    }

    public String getPlayerUrl() {
        AppInfo appInfo = getAppInfoData();
        return appInfo != null ? appInfo.getPlayerUrl() : null;
    }

    public String getClientUrl() {
        AppInfo appInfo = getAppInfoData();
        return appInfo != null ? appInfo.getClientUrl() : null;
    }

    private AppInfo getAppInfoData() {
        
        Call<AppInfo> wrapper = mAppApi.getAppInfo(
            ExoMediaSourceFactory.USER_AGENT_TV, 
            mData.mVisitorCookie
        );

        Response<AppInfo> response = RetrofitHelper.getResponse(wrapper);

        if (response != null) {
            mData.setVisitorCookie(
                RetrofitHelper.getCookies(response)
            );
            return response.body();
        }

        return null;
    }

    public PlayerDataExtractor getPlayerDataExtractor() {
        return new PlayerDataExtractor(getPlayerUrl());
    }

    public void refreshCacheIfNeeded() {
        getAppInfoData();
        getClientData(getClientUrl());
        getPlayerDataExtractor();
    }

}
