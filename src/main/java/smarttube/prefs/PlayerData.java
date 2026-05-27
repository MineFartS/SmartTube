package minefarts.smarttube.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;

import androidx.annotation.NonNull;

import minefarts.smarttube.text.CaptionStyleCompat;
import minefarts.smarttube.utils.helpers.DeviceHelpers;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.locale.LocaleUtility;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.playback.PlayerEngine;
import minefarts.smarttube.app.models.playback.PlayerEngine;
import minefarts.smarttube.exoplayer.selector.ExoFormatItem;
import minefarts.smarttube.exoplayer.selector.FormatItem;
import minefarts.smarttube.exoplayer.selector.track.MediaTrack;
import minefarts.smarttube.prefs.AppPrefs.ProfileChangeListener;
import minefarts.smarttube.prefs.common.DataChangeBase;
import minefarts.smarttube.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerData extends DataChangeBase implements PlayerEngine, ProfileChangeListener {

    private static final String VIDEO_PLAYER_DATA = "video_player_data";

    public static final int ONLY_UI = 0;
    public static final int UI_AND_PAUSE = 1;
    public static final int ONLY_PAUSE = 2;
    public static final int AUTO_HIDE_NEVER = 0;

    @SuppressLint("StaticFieldLeak")
    private static PlayerData sInstance;
    private final AppPrefs mPrefs;

    private FormatItem mVideoFormat;
    private FormatItem mTempVideoFormat;
    private FormatItem mAudioFormat;
    private FormatItem mSubtitleFormat;
    private final Map<String, FormatItem> mDefaultVideoFormats = new HashMap<>();
    private boolean mIsAfrEnabled;
    private boolean mIsAfrFpsCorrectionEnabled;
    private boolean mIsAfrResSwitchEnabled;
    private int mAudioDelayMs;
    private String mAudioLanguage;
    private String mSubtitleLanguage;
    private int mPlaybackMode;
    private boolean mIsTimeCorrectionEnabled;
    private boolean mIsDoubleRefreshRateEnabled;
    private float mSubtitleScale;
    private float mPlayerVolume;
    private float mSubtitlePosition;
    private boolean mIsSkip24RateEnabled;
    private boolean mIsSkipShortsEnabled;
    private boolean mIsLiveChatEnabled;
    private List<FormatItem> mLastSubtitleFormats;
    private float mPitch;
    private List<String> mLastAudioLanguages;

    private PlayerData(Context context) {
        
        mPrefs = AppPrefs.instance(context);
        mPrefs.addListener(this);
        
        initDefaultFormats();
        
        restoreState();
    }

    public static PlayerData instance(Context context) {
        if (sInstance == null) {
            sInstance = new PlayerData(context.getApplicationContext());
        }

        return sInstance;
    }

    public void setPlaybackMode(int mode) {
        mPlaybackMode = mode;
        persistState();
    }

    public int getPlaybackMode() {
        return mPlaybackMode;
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

    public int getAudioDelayMs() {
        return mAudioDelayMs;
    }

    public void setAudioDelayMs(int delayMs) {
        mAudioDelayMs = delayMs;
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

        /* 01 */ mVideoFormat = Helpers.firstNonNull(ExoFormatItem.from(Helpers.parseStr(split, 1)), getDefaultVideoFormat());
        /* 02 */ mAudioFormat = Helpers.firstNonNull(ExoFormatItem.from(Helpers.parseStr(split, 2)), getDefaultAudioFormat());
        /* 03 */ mSubtitleFormat = Helpers.firstNonNull(ExoFormatItem.from(Helpers.parseStr(split, 3)), getDefaultSubtitleFormat());

        /* 07 */ mIsAfrEnabled = Helpers.parseBoolean(split, 7, false);
        /* 08 */ mIsAfrFpsCorrectionEnabled = Helpers.parseBoolean(split, 8, true);
        /* 09 */ mIsAfrResSwitchEnabled = Helpers.parseBoolean(split, 9, false);
        /* 10 */ mAudioDelayMs = Helpers.parseInt(split, 10, 0);

        /* 13 */ mIsTimeCorrectionEnabled = Helpers.parseBoolean(split, 13, true);
        /* 14 */ mIsDoubleRefreshRateEnabled = Helpers.parseBoolean(split, 14, true);
        /* 15 */ mSubtitleScale = Helpers.parseFloat(split, 15, .7f);
        /* 16 */ mPlayerVolume = Helpers.parseFloat(split, 16, 1.0f);

        /* 18 */ mSubtitlePosition = Helpers.parseFloat(split, 18, 0.1f);
        /* 19 */ mIsSkip24RateEnabled = Helpers.parseBoolean(split, 19, false);
        /* 20 */ mIsLiveChatEnabled = Helpers.parseBoolean(split, 20, false);
        /* 21 */ mLastSubtitleFormats = Helpers.parseList(split, 21, ExoFormatItem::from);

        /* 25 */ mPlaybackMode = Helpers.parseInt(split, 25, PlayerEngine.PLAYBACK_MODE_ALL);
        /* 26 */ mAudioLanguage = Helpers.parseStr(split, 26, LocaleUtility.getCurrentLanguage(mPrefs.getContext()));
        /* 27 */ mSubtitleLanguage = Helpers.parseStr(split, 27, LocaleUtility.getCurrentLanguage(mPrefs.getContext()));

        /* 30 */ mPitch = Helpers.parseFloat(split, 30, 1.0f);
        /* 31 */ mIsSkipShortsEnabled = Helpers.parseBoolean(split, 31, false);
        /* 32 */ mLastAudioLanguages = Helpers.parseStrList(split, 32);
        
    }

    public void persistState() {
        mPrefs.setProfileData(
            VIDEO_PLAYER_DATA, 
            Helpers.mergeData(
            /* 00 */ null,
            /* 01 */ mVideoFormat, 
            /* 02 */ mAudioFormat, 
            /* 03 */ mSubtitleFormat,
            /* 04 */ null, 
            /* 05 */ null, 
            /* 06 */ null,
            /* 07 */ mIsAfrEnabled, 
            /* 08 */ mIsAfrFpsCorrectionEnabled, 
            /* 09 */ mIsAfrResSwitchEnabled, 
            /* 10 */ mAudioDelayMs, 
            /* 11 */ null, 
            /* 12 */ null,
            /* 13 */ mIsTimeCorrectionEnabled,
            /* 14 */ mIsDoubleRefreshRateEnabled, 
            /* 15 */ mSubtitleScale, 
            /* 16 */ mPlayerVolume, 
            /* 17 */ null,
            /* 18 */ mSubtitlePosition, 
            /* 19 */ mIsSkip24RateEnabled, 
            /* 20 */ mIsLiveChatEnabled, 
            /* 21 */ mLastSubtitleFormats, 
            /* 22 */ null, 
            /* 23 */ null, 
            /* 24 */ null, 
            /* 25 */ mPlaybackMode, 
            /* 26 */ mAudioLanguage, 
            /* 27 */ mSubtitleLanguage,
            /* 28 */ null,
            /* 29 */ null, 
            /* 30 */ mPitch, 
            /* 31 */ mIsSkipShortsEnabled, 
            /* 32 */ mLastAudioLanguages
        ));
    }

    @Override
    public void onProfileChanged() {
        persistState();
        restoreState();
    }

}
