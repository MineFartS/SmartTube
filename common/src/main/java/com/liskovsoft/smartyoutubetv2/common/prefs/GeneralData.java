package com.liskovsoft.smartyoutubetv2.common.prefs;

import android.annotation.SuppressLint;
import android.content.Context;

import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.prefs.GlobalPreferences;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.prefs.AppPrefs.ProfileChangeListener;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils;
import com.liskovsoft.smartyoutubetv2.common.utils.DataStore;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GeneralData implements ProfileChangeListener {

    public static final int BACKGROUND_PLAYBACK_SHORTCUT_HOME = 0;
    public static final int BACKGROUND_PLAYBACK_SHORTCUT_HOME_BACK = 1;
    public static final int BACKGROUND_PLAYBACK_SHORTCUT_BACK = 2;

    @SuppressLint("StaticFieldLeak")
    private static GeneralData sInstance;

    private final Context mContext;
    private final AppPrefs mPrefs;
    private final DataStore mDataStore;

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

        mDataStore = new DataStore("general_data");
        
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

        /* 00 */ mBackgroundShortcut = mDataStore.get(0, BACKGROUND_PLAYBACK_SHORTCUT_HOME_BACK);
        /* 01 */ mOldPinnedItems = mDataStore.get(1, Video::fromString);
        /* 02 */ mIsHideShortsFromSubscriptionsEnabled = mDataStore.get(2, false);
        /* 03 */ mIsBridgeCheckEnabled = mDataStore.get(3, true);
        /* 04 */ mLastPlaylistId = mDataStore.get(4);
        /* 05 */ mIsHideUpcomingEnabled = mDataStore.get(5, false);
        /* 06 */ mIsHideShortsFromHomeEnabled = mDataStore.get(6, false);
        /* 07 */ mIsHideShortsFromHistoryEnabled = mDataStore.get(7, false);
        /* 08 */ mIsVPNEnabled = mDataStore.get(8, false);
        /* 09 */ mLastPlaylistTitle = mDataStore.get(9);
        /* 10 */ mPlaylistOrder = mDataStore.get(10, Helpers::parseStr, Helpers::parseInt);
        /* 11 */ mPendingStreams = mDataStore.get(11, Video::fromString);
        /* 12 */ mIsAltAppIconEnabled = mDataStore.get(12, false);
        /* 13 */ mVersionCode = Helpers.mDataStore.get(13, -1);
        /* 14 */ mIsOldUpdateNotificationsEnabled = mDataStore.get(14, false);
        /* 15 */ mIsRememberSubscriptionsPositionEnabled = mDataStore.get(15, false);
        /* 16 */ mIsHideWatchedFromNotificationsEnabled = mDataStore.get(16, false);
        /* 17 */ mChangelog = mDataStore.get(17);
        /* 18 */ mSelectedItems = mDataStore.get(18, Helpers::parseInt, Video::fromString);
        /* 19 */ mIsFirstUseTooltipEnabled = mDataStore.get(19, true);
        /* 20 */ mIsDeviceSpecificBackupEnabled = mDataStore.get(20, false);

    }

    private void persistState() {

        /* 00 */ mDataStore.put(0, mBackgroundShortcut); 
        /* 01 */ mDataStore.put(1, mOldPinnedItems);
        /* 02 */ mDataStore.put(2, mIsHideShortsFromSubscriptionsEnabled);
        /* 03 */ mDataStore.put(3, mIsBridgeCheckEnabled);
        /* 04 */ mDataStore.put(4, mLastPlaylistId);
        /* 05 */ mDataStore.put(5, mIsHideUpcomingEnabled); 
        /* 06 */ mDataStore.put(6, mIsHideShortsFromHomeEnabled); 
        /* 07 */ mDataStore.put(7, mIsHideShortsFromHistoryEnabled); 
        /* 08 */ mDataStore.put(8, mIsVPNEnabled);
        /* 09 */ mDataStore.put(9, mLastPlaylistTitle);
        /* 10 */ mDataStore.put(10, mPlaylistOrder);
        /* 11 */ mDataStore.put(11, mPendingStreams); 
        /* 12 */ mDataStore.put(12, mIsAltAppIconEnabled); 
        /* 13 */ mDataStore.put(13, mVersionCode);
        /* 14 */ mDataStore.put(14, mIsOldUpdateNotificationsEnabled);
        /* 15 */ mDataStore.put(15, mIsRememberSubscriptionsPositionEnabled); 
        /* 16 */ mDataStore.put(16, mIsHideWatchedFromNotificationsEnabled);
        /* 17 */ mDataStore.put(17, mChangelog);
        /* 18 */ mDataStore.put(18, mSelectedItems); 
        /* 19 */ mDataStore.put(19, mIsFirstUseTooltipEnabled); 
        /* 20 */ mDataStore.put(20, mIsDeviceSpecificBackupEnabled);

    }

    @Override
    public void onProfileChanged() {
        persistState();
        restoreState();
    }

}
