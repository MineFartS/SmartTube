package com.liskovsoft.smartyoutubetv2.common.exoplayer.other;

/**
 * Applies software gain using LoudnessEnhancer to increase perceived volume when needed.
 *
 * Constraints:
 * - Only available on API >= 19 and when the audio session supports LoudnessEnhancer.
 * - Avoid boosting multi-channel (5.1+) audio because some devices/drivers will throw "format not supported".
 * - Use conservative gain calculations to reduce clipping risk.
 *
 * Integration:
 * - Registered as an AudioListener on SimpleExoPlayer and created only when user requests higher-than-normal volume.
 */

import android.media.audiofx.LoudnessEnhancer;
import android.os.Build.VERSION;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioListener;
import com.liskovsoft.sharedutils.mylogger.Log;

public class VolumeBooster implements AudioListener {
    private static final String TAG = VolumeBooster.class.getSimpleName();
    private boolean mIsEnabled;
    private final float mVolume;
    private final SimpleExoPlayer mPlayer;
    private LoudnessEnhancer mBooster;
    private boolean mIsSupported;
    private int mCurrentSessionId = -1;

    public VolumeBooster(boolean enabled, float volume, @Nullable SimpleExoPlayer player) {
        mIsEnabled = enabled;
        mVolume = volume;
        mPlayer = player;
    }

    @Override
    public void onAudioSessionId(int audioSessionId) {
        if (VERSION.SDK_INT < 19 || mVolume <= 1) {
            return;
        }

        // NOTE: 5.1 audio cannot be boosted (format isn't supported error)
        if (mPlayer != null && mPlayer.getAudioFormat() != null && mPlayer.getAudioFormat().channelCount > 2) {
            return;
        }

        Log.d(TAG, "Audio session id is %s, supported gain %s", audioSessionId, LoudnessEnhancer.PARAM_TARGET_GAIN_MB);

        if (audioSessionId == mCurrentSessionId) {
            return; // Already initialized for this session
        }

        mCurrentSessionId = audioSessionId;

        if (mBooster != null) {
            mBooster.release();
        }

        try {
            mBooster = new LoudnessEnhancer(audioSessionId);
            mBooster.setEnabled(mIsEnabled);

            //double log2 = Math.log(mVolume) / Math.log(2);
            //double gainMb = 10 * log2 * 100;
            //mBooster.setTargetGain((int) gainMb);

            double gainMb = 20 * Math.log10(mVolume * 3) * 100;
            mBooster.setTargetGain((int) gainMb);

            //mBooster.setTargetGain((int) (1000 * mVolume));

            mIsSupported = true;
        } catch (RuntimeException | UnsatisfiedLinkError | NoClassDefFoundError | NoSuchFieldError e) { // Cannot initialize effect engine
            e.printStackTrace();
            mIsSupported = false;
        }
    }

    public boolean isEnabled() {
        return mIsEnabled;
    }

    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
        if (mBooster != null) {
            mBooster.setEnabled(enabled);
        }
    }

    public boolean isSupported() {
        return mIsSupported;
    }
}
