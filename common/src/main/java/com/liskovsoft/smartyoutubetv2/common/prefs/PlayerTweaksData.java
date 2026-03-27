package com.liskovsoft.smartyoutubetv2.common.prefs;

import android.annotation.SuppressLint;
import android.content.Context;

import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.prefs.GlobalPreferences;
import com.liskovsoft.smartyoutubetv2.common.prefs.AppPrefs.ProfileChangeListener;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils;
import com.liskovsoft.youtubeapi.service.internal.MediaServiceData;

public class PlayerTweaksData implements ProfileChangeListener {

    private static final String VIDEO_PLAYER_TWEAKS_DATA = "video_player_tweaks_data";
    
    public static final int PLAYER_DATA_SOURCE_DEFAULT = 0;
    public static final int PLAYER_DATA_SOURCE_OKHTTP = 1;
    public static final int PLAYER_DATA_SOURCE_CRONET = 2;
    public static final int PLAYER_BUTTON_SEARCH = 1 << 1;
    public static final int PLAYER_BUTTON_PIP = 1 << 2;
    public static final int PLAYER_BUTTON_PLAYBACK_QUEUE = 1 << 4;
    public static final int PLAYER_BUTTON_VIDEO_SPEED = 1 << 5;
    public static final int PLAYER_BUTTON_VIDEO_STATS = 1 << 6;
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
    public static final int PLAYER_BUTTON_SHARE = 1 << 19;
    public static final int PLAYER_BUTTON_CONTENT_BLOCK = 1 << 21;
    public static final int PLAYER_BUTTON_CHAT = 1 << 22;
    public static final int PLAYER_BUTTON_VIDEO_ROTATE = 1 << 23;
    public static final int PLAYER_BUTTON_AFR = 1 << 26;
    public static final int PLAYER_BUTTON_VIDEO_FLIP = 1 << 27;
    
    public static final int PLAYER_BUTTON_DEFAULT = 
        PLAYER_BUTTON_SEARCH | 
        PLAYER_BUTTON_PIP | 
        PLAYER_BUTTON_VIDEO_SPEED |
        PLAYER_BUTTON_VIDEO_STATS | 
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
        PLAYER_BUTTON_HIGH_QUALITY |
        PLAYER_BUTTON_VIDEO_INFO | 
        PLAYER_BUTTON_CHAT;

    @SuppressLint("StaticFieldLeak")
    private static PlayerTweaksData sInstance;
    private final AppPrefs mPrefs;

    private boolean mIsSnapToVsyncDisabled;
    private boolean mIsProfileLevelCheckSkipped;
    private boolean mIsSWDecoderForced;
    private boolean mIsTextureViewEnabled;
    private boolean mIsSetOutputSurfaceWorkaroundEnabled;
    private boolean mIsAudioSyncFixEnabled;
    private boolean mIsKeepFinishedActivityEnabled;
    private boolean mIsPlaybackNotificationsDisabled;
    private boolean mIsTunneledPlaybackEnabled;
    private int mPlayerButtons;
    private boolean mIsRememberPositionOfLiveVideosEnabled;
    private boolean mIsRealChannelIconEnabled;
    private boolean mIsQualityInfoBitrateEnabled;
    private boolean mIsButtonLongClickEnabled;
    private boolean mIsLongSpeedListEnabled;
    private boolean mIsExtraLongSpeedListEnabled;
    private int mPlayerDataSource;
    private boolean mIsBufferOnStreamsDisabled;
    private boolean mIsSectionPlaylistEnabled;
    private boolean mIsPlayerAutoVolumeEnabled;
    private boolean mIsSyncRowButtonIndexEnabled;
    private boolean mIsLoopShortsEnabled;
    
    private final Runnable mPersistDataInt = this::persistDataInt;

    private PlayerTweaksData(Context context) {
        mPrefs = AppPrefs.instance(context);
        mPrefs.addListener(this);
        restoreData();
    }

    public static PlayerTweaksData instance(Context context) {
        if (sInstance == null) {
            sInstance = new PlayerTweaksData(context.getApplicationContext());
        }

        return sInstance;
    }

    public boolean isProfileLevelCheckSkipped() {
        return mIsProfileLevelCheckSkipped;
    }

    public void setProfileLevelCheckSkipped(boolean enable) {
        mIsProfileLevelCheckSkipped = enable;
        persistData();
    }

    public boolean isSWDecoderForced() {
        return mIsSWDecoderForced;
    }

    public void setSWDecoderForced(boolean force) {
        mIsSWDecoderForced = force;
        persistData();
    }

    public boolean isTextureViewEnabled() {
        return mIsTextureViewEnabled;
    }

    public void setTextureViewEnabled(boolean enable) {
        mIsTextureViewEnabled = enable;
        persistData();
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
        persistData();
    }

    public boolean isAudioSyncFixEnabled() {
        return mIsAudioSyncFixEnabled;
    }

    public void setAudioSyncFixEnabled(boolean enable) {
        mIsAudioSyncFixEnabled = enable;
        persistData();
    }

    /**
     * Fix crashes on chinese projectors
     */
    public boolean isKeepFinishedActivityEnabled() {
        return mIsKeepFinishedActivityEnabled;
    }

    /**
     * Fix crashes on chinese projectors
     */
    public void setKeepFinishedActivityEnabled(boolean enable) {
        mIsKeepFinishedActivityEnabled = enable;
        persistData();
    }

    public boolean isPlaybackNotificationsDisabled() {
        return mIsPlaybackNotificationsDisabled;
    }

    public void setPlaybackNotificationsDisabled(boolean disable) {
        mIsPlaybackNotificationsDisabled = disable;
        persistData();
    }

    public boolean isTunneledPlaybackEnabled() {
        return mIsTunneledPlaybackEnabled;
    }

    public void setTunneledPlaybackEnabled(boolean enable) {
        mIsTunneledPlaybackEnabled = enable;
        persistData();
    }

    public boolean isPlayerButtonEnabled(int menuItems) {
        return (mPlayerButtons & menuItems) == menuItems;
    }

    public void setPlayerButtonEnabled(int playerButtons) {
        mPlayerButtons |= playerButtons;
        persistData();
    }

    public void setPlayerButtonDisabled(int playerButtons) {
        mPlayerButtons &= ~playerButtons;
        persistData();
    }

    public int getPlayerDataSource() {
        return mPlayerDataSource;
    }

    public void setPlayerDataSource(int dataSource) {
        mPlayerDataSource = dataSource;
        persistData();
    }

    public boolean isRememberPositionOfLiveVideosEnabled() {
        return mIsRememberPositionOfLiveVideosEnabled;
    }

    public void setRememberPositionOfLiveVideosEnabled(boolean enable) {
        mIsRememberPositionOfLiveVideosEnabled = enable;
        persistData();
    }

    public boolean isRealChannelIconEnabled() {
        return mIsRealChannelIconEnabled;
    }

    public void setRealChannelIconEnabled(boolean enable) {
        mIsRealChannelIconEnabled = enable;
        persistData();
    }

    public boolean isQualityInfoBitrateEnabled() {
        return mIsQualityInfoBitrateEnabled;
    }

    public void setQualityInfoBitrateEnabled(boolean enable) {
        mIsQualityInfoBitrateEnabled = enable;
        persistData();
    }

    public boolean isButtonLongClickEnabled() {
        return mIsButtonLongClickEnabled;
    }

    public void setButtonLongClickEnabled(boolean enable) {
        mIsButtonLongClickEnabled = enable;
        persistData();
    }

    public boolean isLongSpeedListEnabled() {
        return mIsLongSpeedListEnabled;
    }

    public void setLongSpeedListEnabled(boolean enable) {
        mIsExtraLongSpeedListEnabled = false;
        mIsLongSpeedListEnabled = enable;
        persistData();
    }

    public boolean isExtraLongSpeedListEnabled() {
        return mIsExtraLongSpeedListEnabled;
    }

    public void setExtraLongSpeedListEnabled(boolean enable) {
        mIsLongSpeedListEnabled = false;
        mIsExtraLongSpeedListEnabled = enable;
        persistData();
    }

    public boolean isBufferOnStreamsDisabled() {
        return mIsBufferOnStreamsDisabled;
    }

    public void setBufferOnStreamsDisabled(boolean disable) {
        mIsBufferOnStreamsDisabled = disable;
        persistData();
    }

    public boolean isSectionPlaylistEnabled() {
        return mIsSectionPlaylistEnabled;
    }

    public void setSectionPlaylistEnabled(boolean enable) {
        mIsSectionPlaylistEnabled = enable;
        persistData();
    }

    public boolean isPlayerAutoVolumeEnabled() {
        return mIsPlayerAutoVolumeEnabled;
    }

    public void setPlayerAutoVolumeEnabled(boolean enable) {
        mIsPlayerAutoVolumeEnabled = enable;
        persistData();
    }

    public boolean isSyncRowButtonIndexEnabled() {
        return mIsSyncRowButtonIndexEnabled;
    }

    public void setSyncRowButtonIndexEnabled(boolean enable) {
        mIsSyncRowButtonIndexEnabled = enable;
        persistData();
    }

    public boolean isLoopShortsEnabled() {
        return mIsLoopShortsEnabled;
    }

    public void setLoopShortsEnabled(boolean enable) {
        mIsLoopShortsEnabled = enable;
        persistData();
    }

    private void restoreData() {

        String data = mPrefs.getProfileData(VIDEO_PLAYER_TWEAKS_DATA);

        String[] split = Helpers.splitData(data);

        mIsSnapToVsyncDisabled = Helpers.parseBoolean(split, 2, false);
        mIsProfileLevelCheckSkipped = Helpers.parseBoolean(split, 3, false);
        mIsSWDecoderForced = Helpers.parseBoolean(split, 4, false);
        mIsTextureViewEnabled = Helpers.parseBoolean(split, 5, false);
        // Need to be enabled (?) on older version of ExoPlayer (e.g. 2.10.6).
        // It's because there's no tweaks for modern devices.
        mIsSetOutputSurfaceWorkaroundEnabled = Helpers.parseBoolean(split, 7, true);
        mIsAudioSyncFixEnabled = Helpers.parseBoolean(split, 8, false);
        mIsKeepFinishedActivityEnabled = Helpers.parseBoolean(split, 9, false);
        mIsPlaybackNotificationsDisabled = Helpers.parseBoolean(split, 11, !Helpers.isAndroidTVLauncher(mPrefs.getContext()));
        mIsTunneledPlaybackEnabled = Helpers.parseBoolean(split, 12, false);
        mPlayerButtons = Helpers.parseInt(split, 13, PLAYER_BUTTON_DEFAULT);
        mIsRealChannelIconEnabled = Helpers.parseBoolean(split, 20, true);
        mIsQualityInfoBitrateEnabled = Helpers.parseBoolean(split, 22, false);
        mIsButtonLongClickEnabled = Helpers.parseBoolean(split, 24, true);
        mIsLongSpeedListEnabled = Helpers.parseBoolean(split, 25, true);
        mPlayerDataSource = Helpers.parseInt(split, 26, PLAYER_DATA_SOURCE_DEFAULT);
        mIsBufferOnStreamsDisabled = Helpers.parseBoolean(split, 30, false);
        // Cause severe garbage collector stuttering
        mIsSectionPlaylistEnabled = Helpers.parseBoolean(split, 31, Utils.isEnoughRam());
        mIsPlayerAutoVolumeEnabled = Helpers.parseBoolean(split, 40, true);
        mIsSyncRowButtonIndexEnabled = Helpers.parseBoolean(split, 41, true);
        mIsLoopShortsEnabled = Helpers.parseBoolean(split, 44, true);
        mIsRememberPositionOfLiveVideosEnabled = Helpers.parseBoolean(split, 46, true);
        mIsExtraLongSpeedListEnabled = Helpers.parseBoolean(split, 49, false);

        updateDefaultValues();
    }

    public void persistNow() {
        Utils.post(mPersistDataInt);
    }

    private void persistData() {
        //Utils.postDelayed(mPersistDataInt, 10_000);
        persistNow();
    }

    private void persistDataInt() {
        mPrefs.setProfileData(
            VIDEO_PLAYER_TWEAKS_DATA, 
            Helpers.mergeData(
                mIsSnapToVsyncDisabled,
                mIsProfileLevelCheckSkipped, 
                mIsSWDecoderForced, 
                mIsTextureViewEnabled,
                null, 
                mIsSetOutputSurfaceWorkaroundEnabled, 
                mIsAudioSyncFixEnabled, 
                mIsKeepFinishedActivityEnabled, 
                mIsPlaybackNotificationsDisabled, 
                mIsTunneledPlaybackEnabled, 
                mPlayerButtons,
                        null,
                mIsRealChannelIconEnabled,
                mIsQualityInfoBitrateEnabled,
                mIsButtonLongClickEnabled, 
                mIsLongSpeedListEnabled, 
                mPlayerDataSource, 
                mIsBufferOnStreamsDisabled, 
                        mIsSectionPlaylistEnabled,
                mIsPlayerAutoVolumeEnabled, 
                mIsSyncRowButtonIndexEnabled,
                null, 
                mIsLoopShortsEnabled, 
                mIsRememberPositionOfLiveVideosEnabled,
                null, 
                mIsExtraLongSpeedListEnabled, 
                        null
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
        Utils.removeCallbacks(mPersistDataInt);
        restoreData();
    }
}
