package minefarts.smarttube.prefs;

import android.annotation.SuppressLint;
import android.content.Context;

import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.prefs.GlobalPreferences;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.prefs.AppPrefs.ProfileChangeListener;
import minefarts.smarttube.utils.Utils;
import minefarts.smarttube.utils.DataStore;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class GeneralData implements ProfileChangeListener {

    public static final int BACKGROUND_PLAYBACK_SHORTCUT_HOME = 0;
    public static final int BACKGROUND_PLAYBACK_SHORTCUT_HOME_BACK = 1;
    public static final int BACKGROUND_PLAYBACK_SHORTCUT_BACK = 2;

    @SuppressLint("StaticFieldLeak")
    private static GeneralData sInstance;

    private final Context mContext;
    private final AppPrefs mPrefs;
    private final DataStore mDataStore;

    private String mLastPlaylistId;
    private String mLastPlaylistTitle;
    private int mVersionCode;
    private boolean mIsOldUpdateNotificationsEnabled;
    private boolean mIsRememberSubscriptionsPositionEnabled;
    private ArrayList<String> mChangelog;
    private HashMap<String, Integer> mPlaylistOrder;
    private ArrayList<Video> mPendingStreams;
    private HashMap<Integer, Video> mSelectedItems;
    private boolean mIsFirstUseTooltipEnabled;

    private GeneralData(Context context) {

        mContext = context;
        
        mPrefs = AppPrefs.instance(context);
        mPrefs.addListener(this);

        mDataStore = new DataStore(context, "general_data");
        
        restoreState();
    
    }

    public static GeneralData instance(Context context) {
        if (sInstance == null) {
            sInstance = new GeneralData(context.getApplicationContext());
        }

        return sInstance;
    }

    public boolean isRememberSubscriptionsPositionEnabled() {
        return mIsRememberSubscriptionsPositionEnabled;
    }

    public void setRememberSubscriptionsPositionEnabled(boolean enable) {
        mIsRememberSubscriptionsPositionEnabled = enable;
        persistState();
    }

    public boolean is24HourLocaleEnabled() {
        return GlobalPreferences.sInstance.is24HourLocaleEnabled();
    }

    public void set24HourLocaleEnabled(boolean enable) {
        GlobalPreferences.sInstance.set24HourLocaleEnabled(enable);
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

    public ArrayList<Video> getPendingStreams() {

        if (mPendingStreams == null) {
            return new ArrayList<Video>();
        } else {
            return (ArrayList<Video>) Collections.unmodifiableList((List) mPendingStreams);
        }

    }

    public boolean containsPendingStream(Video video) {
        if (video == null || video.videoId == null) {
            return false;
        }

        return Helpers.containsIf(mPendingStreams, item -> video.videoId.equals(item.videoId));
    }

    public void addPendingStream(Video video) {
        if (video == null || video.videoId == null || containsPendingStream(video)) return;

        mPendingStreams.add(video);
        persistState();
    }

    public void removePendingStream(Video video) {
        if (video == null || video.videoId == null || !containsPendingStream(video)) return;

        Helpers.removeIf(mPendingStreams, item -> video.videoId.equals(item.videoId));
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
        if (item == null) return;

        mSelectedItems.put(sectionId, item);

        persistState();
    }

    public void removeSelectedItem(int sectionId) {
        mSelectedItems.remove(sectionId);
    }

    public ArrayList<String> getChangelog() {
        return mChangelog;
    }

    public void setChangelog(ArrayList<String> changelog) {
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

    private synchronized void restoreState() {

        /* 01 */ mLastPlaylistId = mDataStore.get(1);
        /* 02 */ mLastPlaylistTitle = mDataStore.get(2);
        /* 03 */ mPlaylistOrder = mDataStore.get(3);
        /* 04 */ mPendingStreams = mDataStore.get(4);
        /* 05 */ mVersionCode = mDataStore.get(5, -1);
        /* 06 */ mIsOldUpdateNotificationsEnabled = mDataStore.get(6, false);
        /* 07 */ mIsRememberSubscriptionsPositionEnabled = mDataStore.get(7, false);
        /* 08 */ mChangelog = mDataStore.get(8);
        /* 09 */ mSelectedItems = mDataStore.get(9);
        /* 10 */ mIsFirstUseTooltipEnabled = mDataStore.get(10, true);

    }

    public void persistState() {

        /* 00 */ mDataStore.put(0, null); 
        /* 01 */ mDataStore.put(1, mLastPlaylistId);
        /* 02 */ mDataStore.put(2, mLastPlaylistTitle);
        /* 03 */ mDataStore.put(3, mPlaylistOrder);
        /* 04 */ mDataStore.put(4, mPendingStreams);
        /* 05 */ mDataStore.put(5, mVersionCode);
        /* 06 */ mDataStore.put(6, mIsOldUpdateNotificationsEnabled);
        /* 07 */ mDataStore.put(7, mIsRememberSubscriptionsPositionEnabled);
        /* 08 */ mDataStore.put(8, mChangelog);
        /* 09 */ mDataStore.put(9, mSelectedItems);
        /* 10 */ mDataStore.put(10, mIsFirstUseTooltipEnabled);
            
    }

    @Override
    public void onProfileChanged() {
        persistState();
        restoreState();
    }
}
