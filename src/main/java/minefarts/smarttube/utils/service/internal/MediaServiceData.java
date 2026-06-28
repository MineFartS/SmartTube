package minefarts.smarttube.utils.service.internal;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import minefarts.smarttube.utils.helpers.AppInfoHelpers;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.prefs.GlobalPreferences;
import minefarts.smarttube.utils.prefs.SharedPreferencesBase;
import minefarts.smarttube.utils.rx.RxHelper;
import minefarts.smarttube.utils.app.models.cached.AppInfoCached;
import minefarts.smarttube.utils.app.models.cached.ClientDataCached;
import minefarts.smarttube.utils.app.models.PlayerData;
import minefarts.smarttube.utils.app.playerdata.NSigData;
import minefarts.smarttube.utils.app.playerdata.PlayerExtractorCache;
import minefarts.smarttube.utils.app.potokencloud.PoTokenResponse;
import minefarts.smarttube.utils.MediaItemService;

import java.util.UUID;

import io.reactivex.disposables.Disposable;

import kotlin.Triple;

public class MediaServiceData {

    private static final String TAG = MediaServiceData.class.getSimpleName();
    
    public static final int FORMATS_NONE = 0;
    public static final int FORMATS_ALL = Integer.MAX_VALUE;
    public static final int FORMATS_DASH = 1;
    public static final int FORMATS_URL = 1 << 1;
    public static final int FORMATS_EXTENDED_HLS = 1 << 2;
    public static final int CONTENT_NONE = 0;
    public static final int CONTENT_MIXES = 1;
    public static final int CONTENT_WATCHED = 1 << 1;

    public static final int CONTENT_SHORTS = 1 << 3;

    public static final int CONTENT_UPCOMING = 1 << 8;

    public static final int CONTENT_STREAMS_SUBSCRIPTIONS = 1 << 12;

    private static MediaServiceData sInstance;
    private String mScreenId;
    private String mDeviceId;
    private String mOldAppVersion;
    private int mVideoInfoType;
    public String mVisitorCookie;
    private int mEnabledFormats;
    private int mHiddenContent;
    private MediaServiceCache mCachedPrefs;
    private GlobalPreferences mGlobalPrefs;
    public PoTokenResponse mPoToken;
    private AppInfoCached mAppInfo;
    private AppInfoCached mFailedAppInfo;
    private PlayerData mPlayerData;
    private PlayerExtractorCache mPlayerExtractorCache;
    private ClientDataCached mClientData;
    private NSigData mNSigData;
    private NSigData mSigData;
    private boolean mIsMoreSubtitlesUnlocked;

    private static class MediaServiceCache extends SharedPreferencesBase {
        private static final String PREF_NAME = MediaServiceCache.class.getSimpleName();
        private static final String MEDIA_SERVICE_CACHE = "media_service_cache";

        public MediaServiceCache(Context context) {
            super(context, PREF_NAME, true);
        }

        public String getMediaServiceCache() {
            return getString(MEDIA_SERVICE_CACHE, null);
        }

        public void setMediaServiceCache(String cache) {
            putString(MEDIA_SERVICE_CACHE, cache);
        }
    }

    private MediaServiceData() {

        mGlobalPrefs = GlobalPreferences.sInstance;
        mCachedPrefs = new MediaServiceCache(mGlobalPrefs.getContext());

        restoreState();

    }

    public static MediaServiceData instance() {
        if (sInstance == null) {
            if (GlobalPreferences.sInstance == null) {
                Log.e(TAG, "Can't init MediaServiceData. GlobalPreferences isn't initialized yet.");
                return null;
            }
            sInstance = new MediaServiceData();
        }

        return sInstance;
    }

    public String getScreenId() {
        return mScreenId;
    }

    public void setScreenId(String screenId) {
        mScreenId = screenId;
        persistState();
    }

    /**
     * Unique per app instance
     */
    public String getDeviceId() {
        if (mDeviceId == null) {
            mDeviceId = UUID.randomUUID().toString();
            persistState();
        }

        return mDeviceId;
    }

    public void setDeviceId(String deviceId) {
        mDeviceId = deviceId;
        persistState();
    }
    
    public int getVideoInfoType() {
        return mVideoInfoType;
    }

    public void setVideoInfoType(int videoInfoType) {
        mVideoInfoType = videoInfoType;
        persistState();
    }

    public Triple<NSigData, NSigData, PlayerData> getPlayerExtractorData() {
        return new Triple<>(mNSigData, mSigData, mPlayerData);
    }

    public void setPlayerExtractorData(NSigData nSigData, NSigData sigData, PlayerData playerData) {
        mNSigData = nSigData;
        mSigData = sigData;
        mPlayerData = playerData;
        persistState();
    }

    @Nullable
    public PlayerExtractorCache getPlayerExtractorCache() {
        return mPlayerExtractorCache;
    }

    public void setPlayerExtractorCache(PlayerExtractorCache playerCache) {
        mPlayerExtractorCache = playerCache;
        persistState();
    }

    public void setVisitorCookie(String visitorCookie) {
        mVisitorCookie = visitorCookie;
        persistState();
    }

    public boolean isFormatEnabled(int formats) {
        if (mEnabledFormats == FORMATS_NONE) {
            setFormatEnabled(FORMATS_DASH | FORMATS_URL, true);
        }

        return (mEnabledFormats & formats) == formats;
    }

    public void setFormatEnabled(int formats, boolean enable) {
        if (enable) {
            mEnabledFormats |= formats;
        } else {
            mEnabledFormats &= ~formats;
        }

        persistState();
    }

    public boolean isContentHidden(int content) {
        return (mHiddenContent & content) == content;
    }

    public void setContentHidden(int content, boolean hide) {
        if (hide) {
            mHiddenContent |= content;
        } else {
            mHiddenContent &= ~content;
        }

        persistState();
    }

    public PoTokenResponse getPoToken() {
        return mPoToken;
    }

    public void setPoToken(PoTokenResponse poToken) {
        mPoToken = poToken;

        persistState();
    }

    public AppInfoCached getAppInfo() {
        return mAppInfo;
    }

    public void setAppInfo(AppInfoCached appInfo) {
        if (appInfo != null) {
            mFailedAppInfo = null;
        }

        if (Helpers.equals(mAppInfo, appInfo)) return;

        mAppInfo = appInfo;

        persistState();
    }

    public AppInfoCached getFailedAppInfo() {
        return mFailedAppInfo;
    }

    public void setFailedAppInfo(AppInfoCached appInfo) {
        if (Helpers.equals(mFailedAppInfo, appInfo)) return;

        mFailedAppInfo = appInfo;

        persistState();
    }

    public ClientDataCached getClientData() {
        return mClientData;
    }

    public void setClientData(ClientDataCached clientData) {
        mClientData = clientData;

        persistState();
    }

    public boolean isMoreSubtitlesUnlocked() {
        return mIsMoreSubtitlesUnlocked;
    }

    public void setMoreSubtitlesUnlocked(boolean unlock) {
        mIsMoreSubtitlesUnlocked = unlock;
        persistState();
    }

    private void restoreState() {

        String data = mGlobalPrefs.getMediaServiceData();
        String[] split = Helpers.splitData(data);

        String appVersion = AppInfoHelpers.getAppVersionName(mGlobalPrefs.getContext());

        /* 00 */ mScreenId = Helpers.parseStr(split, 0);
        /* 01 */ mDeviceId = Helpers.parseStr(split, 1);
        /* 02 */ mOldAppVersion = Helpers.parseStr(split, 2);
        /* 03 */ mVideoInfoType = Helpers.parseInt(split, 3, -1);
        /* 04 */ mEnabledFormats = Helpers.parseInt(split, 4, FORMATS_DASH | FORMATS_URL);
        /* 05 */ mPoToken = Helpers.parseItem(split, 5, PoTokenResponse::fromString);
        /* 06 */ mAppInfo = Helpers.parseItem(split, 6, AppInfoCached::fromString);
        /* 07 */ mPlayerData = Helpers.parseItem(split, 7, PlayerData::fromString);
        /* 08 */ mClientData = Helpers.parseItem(split, 8, ClientDataCached::fromString);
        /* 09 */ mHiddenContent = Helpers.parseInt(split, 9, CONTENT_SHORTS | CONTENT_UPCOMING);
        /* 10 */ mIsMoreSubtitlesUnlocked = Helpers.parseBoolean(split, 10);
        /* 11 */ mVisitorCookie = Helpers.parseStr(split, 11);
        /* 12 */ mFailedAppInfo = Helpers.parseItem(split, 12, AppInfoCached::fromString);

        // Hide watched content by default
        setContentHidden(MediaServiceData.CONTENT_WATCHED, true);

        boolean isAppUpdated = mOldAppVersion != null && !Helpers.equals(mOldAppVersion, appVersion);

        if (isAppUpdated) {
            resetSensitiveData();
        }

        mOldAppVersion = appVersion;
    }

    public void persistState() {

        if (mGlobalPrefs == null) return;

        mGlobalPrefs.setMediaServiceData(Helpers.mergeData(
        /* 00 */ mScreenId, 
        /* 01 */ mDeviceId, 
        /* 02 */ mOldAppVersion, 
        /* 03 */ mVideoInfoType,
        /* 04 */ mEnabledFormats,
        /* 05 */ mPoToken,
        /* 06 */ mAppInfo, 
        /* 07 */ mPlayerData, 
        /* 08 */ mClientData, 
        /* 09 */ mHiddenContent,
        /* 10 */ mIsMoreSubtitlesUnlocked, 
        /* 11 */ mVisitorCookie, 
        /* 12 */ mFailedAppInfo
        ));
    
    }

    private void resetSensitiveData() {
        mVideoInfoType = -1;
        mFailedAppInfo = null;
    }

}
