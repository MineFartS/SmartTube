package SmartTubeApp.prefs;

import android.annotation.SuppressLint;
import android.content.Context;

import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.prefs.GlobalPreferences;
import SmartTubeApp.prefs.AppPrefs.ProfileChangeListener;
import SmartTubeApp.utils.Utils;
import com.liskovsoft.youtubeapi.service.internal.MediaServiceData;

public class PlayerTweaksData implements ProfileChangeListener {

    private static final String VIDEO_PLAYER_TWEAKS_DATA = "video_player_tweaks_data";
    
    public static final int PLAYER_DATA_SOURCE_DEFAULT = 0;
    public static final int PLAYER_DATA_SOURCE_OKHTTP = 1;
    public static final int PLAYER_DATA_SOURCE_CRONET = 2;
    
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
    public static final int PLAYER_BUTTON_HIGH_QUALITY = 1 << 17;
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
    private int mPlayerButtons;
    private boolean mIsRememberPositionOfLiveVideosEnabled;
    private boolean mIsLongSpeedListEnabled;
    private boolean mIsExtraLongSpeedListEnabled;
    private int mPlayerDataSource;
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

    public boolean isPlayerButtonEnabled(int menuItems) {
        return (mPlayerButtons & menuItems) == menuItems;
    }

    public void setPlayerButtonEnabled(int playerButtons) {
        mPlayerButtons |= playerButtons;
        persistState();
    }

    public void setPlayerButtonDisabled(int playerButtons) {
        mPlayerButtons &= ~playerButtons;
        persistState();
    }

    public int getPlayerDataSource() {
        return mPlayerDataSource;
    }

    public void setPlayerDataSource(int dataSource) {
        mPlayerDataSource = dataSource;
        persistState();
    }

    public boolean isRememberPositionOfLiveVideosEnabled() {
        return mIsRememberPositionOfLiveVideosEnabled;
    }

    public void setRememberPositionOfLiveVideosEnabled(boolean enable) {
        mIsRememberPositionOfLiveVideosEnabled = enable;
        persistState();
    }

    public boolean isLongSpeedListEnabled() {
        return mIsLongSpeedListEnabled;
    }

    public void setLongSpeedListEnabled(boolean enable) {
        mIsExtraLongSpeedListEnabled = false;
        mIsLongSpeedListEnabled = enable;
        persistState();
    }

    public boolean isExtraLongSpeedListEnabled() {
        return mIsExtraLongSpeedListEnabled;
    }

    public void setExtraLongSpeedListEnabled(boolean enable) {
        mIsLongSpeedListEnabled = false;
        mIsExtraLongSpeedListEnabled = enable;
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
        /* 3 */ mPlayerButtons = Helpers.parseInt(split, 3, PLAYER_BUTTON_DEFAULT);
        /* 4 */ mIsLongSpeedListEnabled = Helpers.parseBoolean(split, 4, true);
        /* 5 */ mPlayerDataSource = Helpers.parseInt(split, 5, PLAYER_DATA_SOURCE_DEFAULT);
        /* 6 */ mIsSectionPlaylistEnabled = Helpers.parseBoolean(split, 6, Utils.isEnoughRam());
        /* 7 */ mIsLoopShortsEnabled = Helpers.parseBoolean(split, 7, true);
        /* 8 */ mIsRememberPositionOfLiveVideosEnabled = Helpers.parseBoolean(split, 8, true);
        /* 9 */ mIsExtraLongSpeedListEnabled = Helpers.parseBoolean(split, 9, false);

        updateDefaultValues();

    }

    public void persistState() {
        mPrefs.setProfileData(
            VIDEO_PLAYER_TWEAKS_DATA, 
            Helpers.mergeData(
            /* 0 */ mIsSnapToVsyncDisabled,
            /* 1 */ mIsSetOutputSurfaceWorkaroundEnabled, 
            /* 2 */ mIsPlaybackNotificationsDisabled, 
            /* 3 */ mPlayerButtons,
            /* 4 */ mIsLongSpeedListEnabled, 
            /* 5 */ mPlayerDataSource, 
            /* 6 */ mIsSectionPlaylistEnabled,
            /* 7 */ mIsLoopShortsEnabled, 
            /* 8 */ mIsRememberPositionOfLiveVideosEnabled,
            /* 9 */ mIsExtraLongSpeedListEnabled
            )
        );
    }

    private void updateDefaultValues() {
        // Enable only certain buttons (not all, like it was)
        if (mPlayerButtons >>> 30 == 0b1) { // check leftmost bit (old format)
            int bits = 32 - 24;
            mPlayerButtons = mPlayerButtons << bits >>> bits; // remove auto enabled bits
        }
    }

    @Override
    public void onProfileChanged() {
        persistState();
        restoreState();
    }
}
