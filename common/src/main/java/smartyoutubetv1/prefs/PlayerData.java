package smartyoutubetv1.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.liskovsoft.sharedutils.helpers.DeviceHelpers;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.locale.LocaleUtility;
import smartyoutubetv1.R;
import smartyoutubetv1.app.models.playback.manager.PlayerEngine;
import smartyoutubetv1.app.models.playback.manager.PlayerConstants;
import smartyoutubetv1.exoplayer.other.SubtitleManager.SubtitleStyle;
import smartyoutubetv1.exoplayer.selector.ExoFormatItem;
import smartyoutubetv1.exoplayer.selector.FormatItem;
import smartyoutubetv1.exoplayer.selector.track.MediaTrack;
import smartyoutubetv1.prefs.AppPrefs.ProfileChangeListener;
import smartyoutubetv1.prefs.common.DataChangeBase;
import smartyoutubetv1.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerData extends DataChangeBase implements PlayerConstants, ProfileChangeListener {
    private static final String VIDEO_PLAYER_DATA = "video_player_data";
    public static final int ONLY_UI = 0;
    public static final int UI_AND_PAUSE = 1;
    public static final int ONLY_PAUSE = 2;
    public static final int AUTO_HIDE_NEVER = 0;

    @SuppressLint("StaticFieldLeak")
    private static PlayerData sInstance;
    private final AppPrefs mPrefs;

    private int mBackgroundMode;
    private FormatItem mVideoFormat;
    private FormatItem mTempVideoFormat;
    private FormatItem mAudioFormat;
    private FormatItem mSubtitleFormat;
    private final List<SubtitleStyle> mSubtitleStyles = new ArrayList<>();
    private final Map<String, FormatItem> mDefaultVideoFormats = new HashMap<>();
    private int mSubtitleStyleIndex;
    private int mResizeMode;
    private int mZoomPercents;
    private int mRotationAngle;
    private boolean mIsVideoFlipEnabled;
    private float mSpeed;
    private float mLastSpeed;
    private boolean mIsAfrEnabled;
    private boolean mIsAfrFpsCorrectionEnabled;
    private boolean mIsAfrResSwitchEnabled;
    private int mAudioDelayMs;
    private String mAudioLanguage;
    private String mSubtitleLanguage;
    private boolean mIsAllSpeedEnabled;
    private int mPlaybackMode;
    private boolean mIsSpeedPerVideoEnabled;
    private boolean mIsTimeCorrectionEnabled;
    private boolean mIsDoubleRefreshRateEnabled;
    private float mSubtitleScale;
    private float mPlayerVolume;
    private boolean mIsTooltipsEnabled;
    private float mSubtitlePosition;
    private boolean mIsSkip24RateEnabled;
    private boolean mIsSkipShortsEnabled;
    private boolean mIsLiveChatEnabled;
    private List<FormatItem> mLastSubtitleFormats;
    private boolean mIsSpeedPerChannelEnabled;
    private final Map<String, SpeedItem> mSpeeds = new HashMap<>();
    private float mPitch;
    private List<String> mLastAudioLanguages;

    private static class SpeedItem {
        public String channelId;
        public float speed;

        public SpeedItem(String channelId, float speed) {
            this.channelId = channelId;
            this.speed = speed;
        }

        public static SpeedItem fromString(String specs) {
            String[] split = Helpers.splitObj(specs);

            if (split == null || split.length != 2) {
                return new SpeedItem(null, 1);
            }

            return new SpeedItem(Helpers.parseStr(split[0]), Helpers.parseFloat(split[1]));
        }

        @NonNull
        @Override
        public String toString() {
            return Helpers.mergeObj(channelId, speed);
        }
    }

    private PlayerData(Context context) {
        mPrefs = AppPrefs.instance(context);
        mPrefs.addListener(this);
        initSubtitleStyles();
        initDefaultFormats();
        restoreState();
    }

    public static PlayerData instance(Context context) {
        if (sInstance == null) {
            sInstance = new PlayerData(context.getApplicationContext());
        }

        return sInstance;
    }

    public void setBackgroundMode(int type) {
        mBackgroundMode = type;
        persistState();
    }

    public int getBackgroundMode() {
        return mBackgroundMode;
    }

    public void setPlaybackMode(int mode) {
        mPlaybackMode = mode;
        persistState();
    }

    public int getPlaybackMode() {
        return mPlaybackMode;
    }

    public boolean isAllSpeedEnabled() {
        return mIsAllSpeedEnabled;
    }

    public void setAllSpeedEnabled(boolean enable) {
        mIsAllSpeedEnabled = enable;
        mIsSpeedPerVideoEnabled = false;
        mIsSpeedPerChannelEnabled = false;
        persistState();
    }

    public boolean isSpeedPerVideoEnabled() {
        return mIsSpeedPerVideoEnabled;
    }

    public void setSpeedPerVideoEnabled(boolean enable) {
        mIsSpeedPerVideoEnabled = enable;
        mIsAllSpeedEnabled = false;
        mIsSpeedPerChannelEnabled = false;
        persistState();
    }

    public boolean isAfrEnabled() {
        return mIsAfrEnabled;
    }

    public void setAfrEnabled(boolean enabled) {
        mIsAfrEnabled = enabled;
        persistState();
    }

    public boolean isAfrFpsCorrectionEnabled() {
        return mIsAfrFpsCorrectionEnabled;
    }

    public void setAfrFpsCorrectionEnabled(boolean enabled) {
        mIsAfrFpsCorrectionEnabled = enabled;
        persistState();
    }

    public boolean isAfrResSwitchEnabled() {
        return mIsAfrResSwitchEnabled;
    }

    public void setAfrResSwitchEnabled(boolean enabled) {
        mIsAfrResSwitchEnabled = enabled;
        persistState();
    }

    public boolean isDoubleRefreshRateEnabled() {
        return mIsDoubleRefreshRateEnabled;
    }

    public void setDoubleRefreshRateEnabled(boolean enabled) {
        mIsDoubleRefreshRateEnabled = enabled;
        persistState();
    }

    public boolean isTooltipsEnabled() {
        return mIsTooltipsEnabled;
    }

    public void setTooltipsEnabled(boolean enable) {
        mIsTooltipsEnabled = enable;
        persistState();
    }

    public FormatItem getFormat(int type) {
        FormatItem format = null;

        switch (type) {
            case FormatItem.TYPE_VIDEO:
                format = mVideoFormat;
                break;
            case FormatItem.TYPE_AUDIO:
                format = mAudioFormat;
                break;
            case FormatItem.TYPE_SUBTITLE:
                format = mSubtitleFormat;
                break;
        }

        MediaTrack track = FormatItem.toMediaTrack(format);
        if (track != null) {
            track.isSaved = true;
        }

        return FormatItem.checkFormat(format, type);
    }

    public void setFormat(FormatItem format) {

        if (format == null) {
            return;
        }

        switch (format.getType()) {
            case FormatItem.TYPE_VIDEO:
                mVideoFormat = format;
                break;
            case FormatItem.TYPE_AUDIO:
                mAudioFormat = format;
                break;
            case FormatItem.TYPE_SUBTITLE:
                setLastSubtitleFormat(format);
                mSubtitleFormat = format;
                break;
        }
        
        persistState();
    }

    public void setTempVideoFormat(FormatItem format) {
        mTempVideoFormat = format;
    }

    public FormatItem getTempVideoFormat() {
        return mTempVideoFormat;
    }

    public FormatItem getLastSubtitleFormat() {
        return !mLastSubtitleFormats.isEmpty() ? mLastSubtitleFormats.get(0) : FormatItem.SUBTITLE_NONE;
    }

    public List<FormatItem> getLastSubtitleFormats() {
        return mLastSubtitleFormats;
    }

    private void setLastSubtitleFormat(FormatItem format) {
        if (format != null && !format.isDefault()) {
            mLastSubtitleFormats.remove(format);
            mLastSubtitleFormats.add(0, format);
        } else if (mSubtitleFormat != null && !mSubtitleFormat.isDefault()) {
            mLastSubtitleFormats.remove(mSubtitleFormat);
            mLastSubtitleFormats.add(0, mSubtitleFormat);
        }
    }

    public List<SubtitleStyle> getSubtitleStyles() {
        return mSubtitleStyles;
    }

    public SubtitleStyle getSubtitleStyle() {
        return mSubtitleStyles.get(mSubtitleStyleIndex);
    }

    public void setSubtitleStyle(SubtitleStyle subtitleStyle) {
        mSubtitleStyleIndex = mSubtitleStyles.indexOf(subtitleStyle);
        persistState();
    }

    public float getSubtitleScale() {
        return mSubtitleScale;
    }

    public void setSubtitleScale(float scale) {
        mSubtitleScale = scale;
        persistState();
    }

    public float getPlayerVolume() {
        return mPlayerVolume;
    }

    public void setPlayerVolume(float scale) {
        mPlayerVolume = scale;
        persistState();
    }

    public int getResizeMode() {
        return mResizeMode;
    }

    public void setResizeMode(int mode) {
        mResizeMode = mode;
        persistState();
    }

    public int getZoomPercents() {
        return mZoomPercents;
    }

    public void setZoomPercents(int percents) {
        mZoomPercents = percents;
        persistState();
    }

    public int getRotationAngle() {
        return mRotationAngle;
    }

    public void setRotationAngle(int angle) {
        mRotationAngle = angle;
        persistState();
    }

    public boolean isVideoFlipEnabled() {
        return mIsVideoFlipEnabled;
    }

    public void setVideoFlipEnabled(boolean enabled) {
        mIsVideoFlipEnabled = enabled;
        persistState();
    }

    public float getSpeed() {
        return getSpeed(null);
    }

    public void setSpeed(float speed) {
        setSpeed(null, speed);
    }

    public float getSpeed(String channelId) {
        SpeedItem speed = null;

        if (isSpeedPerChannelEnabled() && channelId != null) {
            speed = mSpeeds.get(channelId);
            mSpeed = 1.0f; // reset speed if the channel not found
        }

        if (speed != null) {
            mSpeed = speed.speed;
        }

        return mSpeed;
    }

    public void setSpeed(String channelId, float speed) {
        if (mSpeed == speed && channelId == null) {
            return;
        }

        if (isSpeedPerChannelEnabled() && channelId != null) {
            if (Helpers.floatEquals(speed, 1.0f)) {
                mSpeeds.remove(channelId);
            } else {
                mSpeeds.put(channelId, new SpeedItem(channelId, speed));
            }
        }
        setLastSpeed(speed);
        mSpeed = speed;
        persistState();
    }

    public float getLastSpeed() {
        return mLastSpeed;
    }

    public void setLastSpeed(float speed) {
        if (speed > 0 && !Helpers.floatEquals(speed, 1.0f)) {
            mLastSpeed = speed;
        } else if (mSpeed > 0 && !Helpers.floatEquals(mSpeed, 1.0f)) {
            mLastSpeed = mSpeed;
        }
    }

    public boolean isSpeedPerChannelEnabled() {
        return mIsSpeedPerChannelEnabled;
    }

    public void setSpeedPerChannelEnabled(boolean enable) {
        mIsSpeedPerChannelEnabled = enable;
        mIsSpeedPerVideoEnabled = false;
        mIsAllSpeedEnabled = false;
        persistState();
    }

    public int getAudioDelayMs() {
        return mAudioDelayMs;
    }

    public void setAudioDelayMs(int delayMs) {
        mAudioDelayMs = delayMs;
        persistState();
    }

    public float getPitch() {
        return mPitch;
    }

    public void setPitch(float pitch) {
        mPitch = pitch;
        persistState();
    }

    public String getAudioLanguage() {
        return mAudioLanguage;
    }

    public void setAudioLanguage(String language) {
        mAudioLanguage = language;
        setLastAudioLanguage(language);
        persistState();
    }

    public List<String> getLastAudioLanguages() {
        return mLastAudioLanguages;
    }

    private void setLastAudioLanguage(String language) {
        mLastAudioLanguages.remove(language);
        mLastAudioLanguages.add(0, language);
    }

    public String getSubtitleLanguage() {
        return mSubtitleLanguage;
    }

    public void setSubtitleLanguage(String language) {
        mSubtitleLanguage = language;
        persistState();
    }

    public boolean isTimeCorrectionEnabled() {
        return mIsTimeCorrectionEnabled;
    }

    public void setTimeCorrectionEnabled(boolean enable) {
        mIsTimeCorrectionEnabled = enable;
        persistState();
    }

    public boolean isSkip24RateEnabled() {
        return mIsSkip24RateEnabled;
    }

    public void setSkip24RateEnabled(boolean enable) {
        mIsSkip24RateEnabled = enable;
        persistState();
    }

    public boolean isSkipShortsEnabled() {
        return mIsSkipShortsEnabled;
    }

    public void setSkipShortsEnabled(boolean enable) {
        mIsSkipShortsEnabled = enable;
        persistState();
    }

    public boolean isLiveChatEnabled() {
        return mIsLiveChatEnabled;
    }

    public void setLiveChatEnabled(boolean enable) {
        mIsLiveChatEnabled = enable;
        persistState();
    }

    public FormatItem getDefaultAudioFormat() {
        // Android 4 (probably some others) doesn't support opus (ac3 will be reverted to opus)
        // Note, 5.1 mp4a doesn't work in 5.1 mode
        // Use opus (ac3 fallback) on modern devices. vp9 and opus should be supported at the same time?
        return DeviceHelpers.isVP9ResolutionSupported(2160) ? FormatItem.AUDIO_51_AC3 : FormatItem.AUDIO_HQ_MP4A;
    }

    public FormatItem getDefaultVideoFormat() {
        FormatItem formatItem = mDefaultVideoFormats.get(Build.MODEL);

        if (formatItem == null) {
            if (VERSION.SDK_INT <= 19) { // Android 4 playback crash fix (memory leak?)
                formatItem = FormatItem.VIDEO_SD_AVC_30;
            } else if (VERSION.SDK_INT <= 23 && DeviceHelpers.isVP9ResolutionSupported(1080)) {
                formatItem = FormatItem.VIDEO_FHD_VP9_60;
            } else if (DeviceHelpers.isVP9ResolutionSupported(2160)) {
                formatItem = FormatItem.VIDEO_4K_VP9_60;
            } else if (DeviceHelpers.isVP9ResolutionSupported(1080)) {
                formatItem = FormatItem.VIDEO_FHD_VP9_60;
            }
        }

        return formatItem != null ? formatItem : FormatItem.VIDEO_HD_AVC_30;
    }

    public FormatItem getDefaultSubtitleFormat() {
        return FormatItem.SUBTITLE_NONE;
    }

    private void initSubtitleStyles() {
        mSubtitleStyles.add(new SubtitleStyle(R.string.subtitle_white_transparent, R.color.light_grey, R.color.transparent, CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW));
        mSubtitleStyles.add(new SubtitleStyle(R.string.subtitle_white_semi_transparent, R.color.light_grey, R.color.semi_transparent, CaptionStyleCompat.EDGE_TYPE_OUTLINE));
        mSubtitleStyles.add(new SubtitleStyle(R.string.subtitle_white_black, R.color.light_grey, R.color.black, CaptionStyleCompat.EDGE_TYPE_OUTLINE));
        mSubtitleStyles.add(new SubtitleStyle(R.string.subtitle_yellow_transparent, R.color.yellow, R.color.transparent, CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW));
        mSubtitleStyles.add(new SubtitleStyle(R.string.subtitle_yellow_semi_transparent, R.color.yellow, R.color.semi_transparent, CaptionStyleCompat.EDGE_TYPE_OUTLINE));
        mSubtitleStyles.add(new SubtitleStyle(R.string.subtitle_yellow_black, R.color.yellow, R.color.black, CaptionStyleCompat.EDGE_TYPE_OUTLINE));

        if (VERSION.SDK_INT >= 19) {
            mSubtitleStyles.add(new SubtitleStyle(R.string.subtitle_system));
        }
    }

    /**
     * Overrides for auto detected values
     */
    private void initDefaultFormats() {
        mDefaultVideoFormats.put("SHIELD Android TV", FormatItem.VIDEO_4K_VP9_60);
        mDefaultVideoFormats.put("AFTMM", FormatItem.VIDEO_4K_VP9_60); // Stick 4K 2018
        mDefaultVideoFormats.put("AFTKA", FormatItem.VIDEO_4K_VP9_60); // Stick 4K Max 2021
        mDefaultVideoFormats.put("P1", FormatItem.VIDEO_FHD_AVC_60); // Chinese projector (see annoying emails)
    }

    private void restoreState() {

        String data = mPrefs.getProfileData(VIDEO_PLAYER_DATA);
        String[] split = Helpers.splitData(data);

        /* 00 */ mBackgroundMode = Helpers.parseInt(split, 0, PlayerEngine.BACKGROUND_MODE_DEFAULT);
        /* 01 */ mVideoFormat = Helpers.firstNonNull(ExoFormatItem.from(Helpers.parseStr(split, 1)), getDefaultVideoFormat());
        /* 02 */ mAudioFormat = Helpers.firstNonNull(ExoFormatItem.from(Helpers.parseStr(split, 2)), getDefaultAudioFormat());
        /* 03 */ mSubtitleFormat = Helpers.firstNonNull(ExoFormatItem.from(Helpers.parseStr(split, 3)), getDefaultSubtitleFormat());
        /* 04 */ mSubtitleStyleIndex = Helpers.parseInt(split, 4, 4); // yellow on semi bg
        /* 05 */ mResizeMode = Helpers.parseInt(split, 5, PlayerEngine.RESIZE_MODE_DEFAULT);
        /* 06 */ mSpeed = Helpers.parseFloat(split, 6, 1.0f);
        /* 07 */ mIsAfrEnabled = Helpers.parseBoolean(split, 7, false);
        /* 08 */ mIsAfrFpsCorrectionEnabled = Helpers.parseBoolean(split, 8, true);
        /* 09 */ mIsAfrResSwitchEnabled = Helpers.parseBoolean(split, 9, false);
        /* 10 */ mAudioDelayMs = Helpers.parseInt(split, 10, 0);
        /* 11 */ mIsAllSpeedEnabled = Helpers.parseBoolean(split, 11, false);
        /* 12 */ mIsSpeedPerVideoEnabled = Helpers.parseBoolean(split, 12, false);
        /* 13 */ mIsTimeCorrectionEnabled = Helpers.parseBoolean(split, 13, true);
        /* 14 */ mIsDoubleRefreshRateEnabled = Helpers.parseBoolean(split, 14, true);
        /* 15 */ mSubtitleScale = Helpers.parseFloat(split, 15, .7f);
        /* 16 */ mPlayerVolume = Helpers.parseFloat(split, 16, 1.0f);
        /* 17 */ mIsTooltipsEnabled = Helpers.parseBoolean(split, 17, true);
        /* 18 */ mSubtitlePosition = Helpers.parseFloat(split, 18, 0.1f);
        /* 19 */ mIsSkip24RateEnabled = Helpers.parseBoolean(split, 19, false);
        /* 20 */ mIsLiveChatEnabled = Helpers.parseBoolean(split, 20, false);
        /* 21 */ mLastSubtitleFormats = Helpers.parseList(split, 21, ExoFormatItem::from);
        /* 22 */ mLastSpeed = Helpers.parseFloat(split, 22, 1.0f);
        /* 23 */ mRotationAngle = Helpers.parseInt(split, 23, 0);
        /* 24 */ mZoomPercents = Helpers.parseInt(split, 24, -1);
        /* 25 */ mPlaybackMode = Helpers.parseInt(split, 25, PlayerConstants.PLAYBACK_MODE_ALL);
        /* 26 */ mAudioLanguage = Helpers.parseStr(split, 26, LocaleUtility.getCurrentLanguage(mPrefs.getContext()));
        /* 27 */ mSubtitleLanguage = Helpers.parseStr(split, 27, LocaleUtility.getCurrentLanguage(mPrefs.getContext()));
        /* 28 */ mIsSpeedPerChannelEnabled = Helpers.parseBoolean(split, 28, true);
        /* 29 */ String[] speeds = Helpers.parseArray(split, 29);
        /* 30 */ mPitch = Helpers.parseFloat(split, 30, 1.0f);
        /* 31 */ mIsSkipShortsEnabled = Helpers.parseBoolean(split, 31, false);
        /* 32 */ mLastAudioLanguages = Helpers.parseStrList(split, 32);
        /* 33 */ mIsVideoFlipEnabled = Helpers.parseBoolean(split, 33, false);

        if (speeds != null) {
            for (String speedSpec : speeds) {
                SpeedItem item = SpeedItem.fromString(speedSpec);
                mSpeeds.put(item.channelId, item);
            }
        }

        if (!mIsAllSpeedEnabled) {
            mSpeed = 1.0f;
        }
    }

    public void persistState() {
        mPrefs.setProfileData(
            VIDEO_PLAYER_DATA, 
            Helpers.mergeData(
            /* 00 */ mBackgroundMode,
            /* 01 */ mVideoFormat, 
            /* 02 */ mAudioFormat, 
            /* 03 */ mSubtitleFormat,
            /* 04 */ mSubtitleStyleIndex, 
            /* 05 */ mResizeMode, 
            /* 06 */ mSpeed,
            /* 07 */ mIsAfrEnabled, 
            /* 08 */ mIsAfrFpsCorrectionEnabled, 
            /* 09 */ mIsAfrResSwitchEnabled, 
            /* 10 */ mAudioDelayMs, 
            /* 11 */ mIsAllSpeedEnabled, 
            /* 12 */ mIsSpeedPerVideoEnabled,
            /* 13 */ mIsTimeCorrectionEnabled,
            /* 14 */ mIsDoubleRefreshRateEnabled, 
            /* 15 */ mSubtitleScale, 
            /* 16 */ mPlayerVolume, 
            /* 17 */ mIsTooltipsEnabled, 
            /* 18 */ mSubtitlePosition, 
            /* 19 */ mIsSkip24RateEnabled, 
            /* 20 */ mIsLiveChatEnabled, 
            /* 21 */ mLastSubtitleFormats, 
            /* 22 */ mLastSpeed, 
            /* 23 */ mRotationAngle, 
            /* 24 */ mZoomPercents, 
            /* 25 */ mPlaybackMode, 
            /* 26 */ mAudioLanguage, 
            /* 27 */ mSubtitleLanguage,
            /* 28 */ mIsSpeedPerChannelEnabled, 
            /* 29 */ Helpers.mergeArray(mSpeeds.values().toArray()), 
            /* 30 */ mPitch, 
            /* 31 */ mIsSkipShortsEnabled, 
            /* 32 */ mLastAudioLanguages, 
            /* 33 */ mIsVideoFlipEnabled
        ));
    }

    @Override
    public void onProfileChanged() {
        
        persistState();

        // reset on profile change
        mSpeeds.clear();

        restoreState();
    }
}
