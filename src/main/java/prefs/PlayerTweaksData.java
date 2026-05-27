package minefarts.smarttube.prefs;

import android.annotation.SuppressLint;
import android.content.Context;

import minefarts.sharedutils.helpers.Helpers;
import minefarts.sharedutils.prefs.GlobalPreferences;
import minefarts.smarttube.prefs.AppPrefs.ProfileChangeListener;
import minefarts.smarttube.utils.Utils;
import minefarts.sharedutils.service.internal.MediaServiceData;

public class PlayerTweaksData implements ProfileChangeListener {

    private static final String VIDEO_PLAYER_TWEAKS_DATA = "video_player_tweaks_data";
    
    public static final int PLAYER_BUTTON_PLAYBACK_QUEUE = 1 << 4;
    public static final int PLAYER_BUTTON_VIDEO_SPEED = 1 << 5;
    public static final int PLAYER_BUTTON_OPEN_CHANNEL = 1 << 7;
    public static final int PLAYER_BUTTON_SUBTITLES = 1 << 8;
    public static final int PLAYER_BUTTON_SUBSCRIBE = 1 << 9;
    public static final int PLAYER_BUTTON_LIKE = 1 << 10;
    public static final int PLAYER_BUTTON_DISLIKE = 1 << 11;
    public static final int PLAYER_BUTTON_ADD_TO_PLAYLIST = 1 << 12;
    public static final int PLAYER_BUTTON_PLAY_PAUSE = 1 << 13;
    public static final int PLAYER_BUTTON_REPEAT_MODE = 1 << 14;
    public static final int PLAYER_BUTTON_NEXT = 1 << 15;
    public static final int PLAYER_BUTTON_PREVIOUS = 1 << 16;
    public static final int PLAYER_BUTTON_VIDEO_INFO = 1 << 18;
    public static final int PLAYER_BUTTON_CHAT = 1 << 22;
    
    public static final int PLAYER_BUTTON_DEFAULT = 
        PLAYER_BUTTON_VIDEO_SPEED |
        PLAYER_BUTTON_OPEN_CHANNEL | 
        PLAYER_BUTTON_SUBTITLES | 
        PLAYER_BUTTON_SUBSCRIBE |
        PLAYER_BUTTON_LIKE | 
        PLAYER_BUTTON_DISLIKE | 
        PLAYER_BUTTON_ADD_TO_PLAYLIST | 
        PLAYER_BUTTON_PLAY_PAUSE |
        PLAYER_BUTTON_REPEAT_MODE | 
        PLAYER_BUTTON_NEXT | 
        PLAYER_BUTTON_PREVIOUS |
        PLAYER_BUTTON_VIDEO_INFO | 
        PLAYER_BUTTON_CHAT;

    @SuppressLint("StaticFieldLeak")
    private static PlayerTweaksData sInstance;

    private final AppPrefs mPrefs;

    private boolean mIsSnapToVsyncDisabled;
    private boolean mIsSetOutputSurfaceWorkaroundEnabled;
    private boolean mIsPlaybackNotificationsDisabled;
    private boolean mIsRememberPositionOfLiveVideosEnabled;
    private boolean mIsSectionPlaylistEnabled;
    private boolean mIsLoopShortsEnabled;

    private PlayerTweaksData(Context context) {
        mPrefs = AppPrefs.instance(context);
        mPrefs.addListener(this);
        restoreState();
    }

    public static PlayerTweaksData instance(Context context) {
        if (sInstance == null) {
            sInstance = new PlayerTweaksData(context.getApplicationContext());
        }

        return sInstance;
    }

    public boolean isSetOutputSurfaceWorkaroundEnabled() {
        return mIsSetOutputSurfaceWorkaroundEnabled;
    }

    /**
     * Need to be enabled on older version of ExoPlayer (e.g. 2.10.6).<br/>
     * It's because there's no tweaks for modern devices.
     */
    public void setSetOutputSurfaceWorkaroundEnabled(boolean enable) {
        mIsSetOutputSurfaceWorkaroundEnabled = enable;
        persistState();
    }

    public boolean isPlaybackNotificationsDisabled() {
        return mIsPlaybackNotificationsDisabled;
    }

    public void setPlaybackNotificationsDisabled(boolean disable) {
        mIsPlaybackNotificationsDisabled = disable;
        persistState();
    }

    public boolean isRememberPositionOfLiveVideosEnabled() {
        return mIsRememberPositionOfLiveVideosEnabled;
    }

    public void setRememberPositionOfLiveVideosEnabled(boolean enable) {
        mIsRememberPositionOfLiveVideosEnabled = enable;
        persistState();
    }

    public boolean isSectionPlaylistEnabled() {
        return mIsSectionPlaylistEnabled;
    }

    public void setSectionPlaylistEnabled(boolean enable) {
        mIsSectionPlaylistEnabled = enable;
        persistState();
    }

    public boolean isLoopShortsEnabled() {
        return mIsLoopShortsEnabled;
    }

    public void setLoopShortsEnabled(boolean enable) {
        mIsLoopShortsEnabled = enable;
        persistState();
    }

    private void restoreState() {

        String data = mPrefs.getProfileData(VIDEO_PLAYER_TWEAKS_DATA);
        String[] split = Helpers.splitData(data);

        /* 0 */ mIsSnapToVsyncDisabled = Helpers.parseBoolean(split, 0, false);
        /* 1 */ mIsSetOutputSurfaceWorkaroundEnabled = Helpers.parseBoolean(split, 1, true);
        /* 2 */ mIsPlaybackNotificationsDisabled = Helpers.parseBoolean(split, 2, !Helpers.isAndroidTVLauncher(mPrefs.getContext()));

        /* 6 */ mIsSectionPlaylistEnabled = Helpers.parseBoolean(split, 6, Utils.isEnoughRam());
        /* 7 */ mIsLoopShortsEnabled = Helpers.parseBoolean(split, 7, true);
        /* 8 */ mIsRememberPositionOfLiveVideosEnabled = Helpers.parseBoolean(split, 8, true);

    }

    public void persistState() {
        mPrefs.setProfileData(
            VIDEO_PLAYER_TWEAKS_DATA, 
            Helpers.mergeData(
            /* 0 */ mIsSnapToVsyncDisabled,
            /* 1 */ mIsSetOutputSurfaceWorkaroundEnabled, 
            /* 2 */ mIsPlaybackNotificationsDisabled, 
            /* 3 */ null,
            /* 4 */ null, 
            /* 5 */ null, 
            /* 6 */ mIsSectionPlaylistEnabled,
            /* 7 */ mIsLoopShortsEnabled, 
            /* 8 */ mIsRememberPositionOfLiveVideosEnabled,
            /* 9 */ null
            )
        );
    }

    @Override
    public void onProfileChanged() {
        persistState();
        restoreState();
    }
}
