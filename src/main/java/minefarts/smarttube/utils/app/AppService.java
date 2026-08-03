package minefarts.smarttube.utils.app;

import android.content.Context;

import androidx.annotation.NonNull;

import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.prefs.GlobalPreferences;
import minefarts.smarttube.utils.app.models.AppInfo;
import minefarts.smarttube.utils.app.models.ClientData;
import minefarts.smarttube.utils.app.playerdata.PlayerDataExtractor;
import minefarts.smarttube.exoplayer.ExoMediaSourceFactory;
import minefarts.smarttube.google.common.helpers.RetrofitHelper;
import minefarts.smarttube.utils.service.internal.MediaServiceData;
import minefarts.smarttube.google.common.helpers.ServiceHelper;
import minefarts.smarttube.ContextManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlin.Pair;

import retrofit2.Call;
import retrofit2.Response;

public class AppService {

    private static AppService sInstance;
    
    public String mVisitorCookie = null;

    AppApi mAppApi;

    public static AppService instance() {
        if (sInstance == null) {
            sInstance = new AppService();
            sInstance.mAppApi = RetrofitHelper.create(AppApi.class);
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

    @NonNull
    public Context getContext() {

        if (GlobalPreferences.isInitialized())
            ContextManager.set(GlobalPreferences.sInstance.getContext());

        if (ContextManager.get() == null)
            throw new IllegalStateException("The Context isn't initialized yet");

        return ContextManager.get();
    }

    private ClientData getClientData() {
        
        AppInfo appInfo = getAppInfoData();
        if (appInfo == null) return null;

        Call<ClientData> wrapper = mAppApi.getClientData(
            ServiceHelper.tidyUrl(appInfo.mClientUrl)
        );
        
        return RetrofitHelper.get(wrapper);
    }

    public String getClientId() {
        ClientData clientData = getClientData();
        return clientData != null ? clientData.mClientId : null;
    }

    /**
     * Constant used in AuthApi
     */
    public String getClientSecret() {
        ClientData clientData = getClientData();
        return clientData != null ? clientData.mClientSecret : null;
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
        if (appInfo == null) return null;
        return ServiceHelper.tidyUrl(appInfo.mPlayerUrl);
    }

    private AppInfo getAppInfoData() {
        
        Call<AppInfo> wrapper = mAppApi.getAppInfo(
            ExoMediaSourceFactory.USER_AGENT_TV, 
            mVisitorCookie
        );

        Response<AppInfo> response = RetrofitHelper.getResponse(wrapper);

        if (response == null) return null;

        List<String> result = new ArrayList<>();
        for (String cookie : response.headers().values("Set-Cookie")) {
            result.add(cookie.split(";")[0]);
        }

        mVisitorCookie = result.isEmpty() ? null : Helpers.join("; ", result.toArray(new CharSequence[0]));

        return response.body();
    }

    public PlayerDataExtractor getPlayerDataExtractor() {
        return new PlayerDataExtractor(getPlayerUrl());
    }

    public void refreshCacheIfNeeded() {
        getAppInfoData();
        getClientData();
        getPlayerDataExtractor();
    }

}
