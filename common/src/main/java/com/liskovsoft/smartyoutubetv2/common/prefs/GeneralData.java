package com.liskovsoft.smartyoutubetv2.common.prefs;

import android.annotation.SuppressLint;
import android.content.Context;

import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.prefs.GlobalPreferences;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.prefs.AppPrefs.ProfileChangeListener;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GeneralData implements ProfileChangeListener {

    private static final String GENERAL_DATA = "general_data";

    public static final int BACKGROUND_PLAYBACK_SHORTCUT_HOME = 0;
    public static final int BACKGROUND_PLAYBACK_SHORTCUT_HOME_BACK = 1;
    public static final int BACKGROUND_PLAYBACK_SHORTCUT_BACK = 2;

    @SuppressLint("StaticFieldLeak")
    private static GeneralData sInstance;
    private final Context mContext;
    private final AppPrefs mPrefs;

    private int mBackgroundShortcut;
    private boolean mIsHideShortsFromSubscriptionsEnabled;
    private boolean mIsHideUpcomingEnabled;
    private boolean mIsBridgeCheckEnabled;
    private String mLastPlaylistId;
    private String mLastPlaylistTitle;
    private boolean mIsHideShortsFromHomeEnabled;
    private boolean mIsHideShortsFromHistoryEnabled;
    private boolean mIsVPNEnabled;
    private boolean mIsAltAppIconEnabled;
    private int mVersionCode;
    private boolean mIsOldUpdateNotificationsEnabled;
    private boolean mIsRememberSubscriptionsPositionEnabled;
    private boolean mIsHideWatchedFromNotificationsEnabled;
    private List<String> mChangelog;
    private Map<String, Integer> mPlaylistOrder;
    private List<Video> mPendingStreams;
    private Map<Integer, Video> mSelectedItems;
    private boolean mIsFirstUseTooltipEnabled;
    private boolean mIsDeviceSpecificBackupEnabled;
    private List<Video> mOldPinnedItems;

    private GeneralData(Context context) {
        mContext = context;
        mPrefs = AppPrefs.instance(context);
        mPrefs.addListener(this);
        restoreState();
    }

    public static GeneralData instance(Context context) {
        if (sInstance == null) {
            sInstance = new GeneralData(context.getApplicationContext());
        }

        return sInstance;
    }

    public int getBackgroundPlaybackShortcut() {
        return mBackgroundShortcut;
    }

    public void setBackgroundPlaybackShortcut(int type) {
        mBackgroundShortcut = type;
        persistState();
    }

    public List<Video> getOldPinnedItems() {
        return mOldPinnedItems;
    }

    public boolean isRememberSubscriptionsPositionEnabled() {
        return mIsRememberSubscriptionsPositionEnabled;
    }

    public void setRememberSubscriptionsPositionEnabled(boolean enable) {
        mIsRememberSubscriptionsPositionEnabled = enable;
        persistState();
    }

    public boolean isHideWatchedFromNotificationsEnabled() {
        return mIsHideWatchedFromNotificationsEnabled;
    }

    public void setHideWatchedFromNotificationsEnabled(boolean enable) {
        mIsHideWatchedFromNotificationsEnabled = enable;
        persistState();
    }

    public boolean is24HourLocaleEnabled() {
        return GlobalPreferences.sInstance.is24HourLocaleEnabled();
    }

    public void set24HourLocaleEnabled(boolean enable) {
        GlobalPreferences.sInstance.set24HourLocaleEnabled(enable);
    }

    public boolean isVPNEnabled() {
        return mIsVPNEnabled;
    }

    public void setVPNEnabled(boolean enable) {
        mIsVPNEnabled = enable;
        persistState();
    }

    public boolean isBridgeCheckEnabled() {
        return mIsBridgeCheckEnabled;
    }

    public void setBridgeCheckEnabled(boolean enable) {
        mIsBridgeCheckEnabled = enable;
        persistState();
    }

    public String getLastPlaylistId() {
        return mLastPlaylistId;
    }

    public void setLastPlaylistId(String playlistId) {
        mLastPlaylistId = playlistId;
        persistState();
    }

    public String getLastPlaylistTitle() {
        return mLastPlaylistTitle;
    }

    public void setLastPlaylistTitle(String playlistTitle) {
        mLastPlaylistTitle = playlistTitle;
        persistState();
    }

    public int getPlaylistOrder(String playlistId) {
        Integer order = mPlaylistOrder.get(playlistId);
        return order != null ? order : -1; // default order unpredictable (depends on site prefs)
    }

    public void setPlaylistOrder(String playlistId, int playlistOrder) {
        if (playlistOrder == -1) {
            mPlaylistOrder.remove(playlistId);
        } else {
            mPlaylistOrder.put(playlistId, playlistOrder);
        }
        persistState();
    }

    public List<Video> getPendingStreams() {
        return Collections.unmodifiableList(mPendingStreams);
    }

    public boolean containsPendingStream(Video video) {
        if (video == null || video.videoId == null) {
            return false;
        }

        return Helpers.containsIf(mPendingStreams, item -> video.videoId.equals(item.videoId));
    }

    public void addPendingStream(Video video) {
        if (video == null || video.videoId == null || containsPendingStream(video)) {
            return;
        }

        mPendingStreams.add(video);
        persistState();
    }

    public void removePendingStream(Video video) {
        if (video == null || video.videoId == null || !containsPendingStream(video)) {
            return;
        }

        Helpers.removeIf(mPendingStreams, item -> video.videoId.equals(item.videoId));
        persistState();
    }

    public boolean isAltAppIconEnabled() {
        return mIsAltAppIconEnabled;
    }

    public void setAltAppIconEnabled(boolean enable) {
        mIsAltAppIconEnabled = enable;

        persistState();
    }

    public int getVersionCode() {
        return mVersionCode;
    }

    public void setVersionCode(int code) {
        mVersionCode = code;

        persistState();
    }

    public boolean isOldUpdateNotificationsEnabled() {
        return mIsOldUpdateNotificationsEnabled;
    }

    public void setOldUpdateNotificationsEnabled(boolean enable) {
        mIsOldUpdateNotificationsEnabled = enable;
        persistState();
    }

    public Video getSelectedItem(int sectionId) {
        return mSelectedItems.get(sectionId);
    }

    public void setSelectedItem(int sectionId, Video item) {
        if (item == null) {
            return;
        }

        mSelectedItems.put(sectionId, item);

        persistState();
    }

    public void removeSelectedItem(int sectionId) {
        mSelectedItems.remove(sectionId);
    }

    public List<String> getChangelog() {
        return mChangelog;
    }

    public void setChangelog(List<String> changelog) {
        mChangelog = changelog;
        persistState();
    }

    public boolean isFirstUseTooltipEnabled() {
        return mIsFirstUseTooltipEnabled;
    }

    public void setFirstUseTooltipEnabled(boolean enable) {
        mIsFirstUseTooltipEnabled = enable;
        persistState();
    }

    public boolean isDeviceSpecificBackupEnabled() {
        return mIsDeviceSpecificBackupEnabled;
    }

    public void setDeviceSpecificBackupEnabled(boolean enable) {
        mIsDeviceSpecificBackupEnabled = enable;
        persistState();
    }

    private synchronized void restoreState() {
        
        String data = mPrefs.getProfileData(GENERAL_DATA);
        String[] split = Helpers.splitData(data);

        /* 00 */ mBackgroundShortcut = Helpers.parseInt(split, 0, BACKGROUND_PLAYBACK_SHORTCUT_HOME_BACK);
        /* 01 */ mOldPinnedItems = Helpers.parseList(split, 1, Video::fromString);
        /* 02 */ mIsHideShortsFromSubscriptionsEnabled = Helpers.parseBoolean(split, 2, false);
        /* 03 */ mIsBridgeCheckEnabled = Helpers.parseBoolean(split, 3, true);
        /* 04 */ mLastPlaylistId = Helpers.parseStr(split, 4);
        /* 05 */ mIsHideUpcomingEnabled = Helpers.parseBoolean(split, 5, false);
        /* 06 */ mIsHideShortsFromHomeEnabled = Helpers.parseBoolean(split, 6, false);
        /* 07 */ mIsHideShortsFromHistoryEnabled = Helpers.parseBoolean(split, 7, false);
        /* 08 */ mIsVPNEnabled = Helpers.parseBoolean(split, 8, false);
        /* 09 */ mLastPlaylistTitle = Helpers.parseStr(split, 9);
        /* 10 */ mPlaylistOrder = Helpers.parseMap(split, 10, Helpers::parseStr, Helpers::parseInt);
        /* 11 */ mPendingStreams = Helpers.parseList(split, 11, Video::fromString);
        /* 12 */ mIsAltAppIconEnabled = Helpers.parseBoolean(split, 12, false);
        /* 13 */ mVersionCode = Helpers.parseInt(split, 13, -1);
        /* 14 */ mIsOldUpdateNotificationsEnabled = Helpers.parseBoolean(split, 14, false);
        /* 15 */ mIsRememberSubscriptionsPositionEnabled = Helpers.parseBoolean(split, 15, false);
        /* 16 */ mIsHideWatchedFromNotificationsEnabled = Helpers.parseBoolean(split, 16, false);
        /* 17 */ mChangelog = Helpers.parseStrList(split, 17);
        /* 18 */ mSelectedItems = Helpers.parseMap(split, 18, Helpers::parseInt, Video::fromString);
        /* 19 */ mIsFirstUseTooltipEnabled = Helpers.parseBoolean(split, 19, true);
        /* 20 */ mIsDeviceSpecificBackupEnabled = Helpers.parseBoolean(split, 20, false);

    }

    public void persistState() {
        // Zero index is skipped. Selected sections were there.
        mPrefs.setProfileData(
            GENERAL_DATA, 
            Helpers.mergeData(
            /* 00 */ mBackgroundShortcut, 
            /* 01 */ mOldPinnedItems, 
            /* 02 */ mIsHideShortsFromSubscriptionsEnabled,
            /* 03 */ mIsBridgeCheckEnabled, 
            /* 04 */ mLastPlaylistId,
            /* 05 */ mIsHideUpcomingEnabled, 
            /* 06 */ mIsHideShortsFromHomeEnabled, 
            /* 07 */ mIsHideShortsFromHistoryEnabled, 
            /* 08 */ mIsVPNEnabled,
            /* 09 */ mLastPlaylistTitle,
            /* 10 */ mPlaylistOrder, 
            /* 11 */ mPendingStreams, 
            /* 12 */ mIsAltAppIconEnabled, 
            /* 13 */ mVersionCode, 
            /* 14 */ mIsOldUpdateNotificationsEnabled,
            /* 15 */ mIsRememberSubscriptionsPositionEnabled, 
            /* 16 */ mIsHideWatchedFromNotificationsEnabled, 
            /* 17 */ mChangelog,
            /* 18 */ mSelectedItems, 
            /* 19 */ mIsFirstUseTooltipEnabled, 
            /* 20 */ mIsDeviceSpecificBackupEnabled
            )
        );
    }

    @Override
    public void onProfileChanged() {
        persistState();
        restoreState();
    }
}
