package com.liskovsoft.smartyoutubetv2.common.exoplayer.other;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.ExoMediaDrm.KeyRequest;
import com.google.android.exoplayer2.drm.ExoMediaDrm.ProvisionRequest;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.liskovsoft.sharedutils.helpers.DeviceHelpers;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerData;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerTweaksData;

import java.util.UUID;

public class ExoPlayerInitializer {
    private final int mMaxBufferBytes;
    private final PlayerData mPlayerData;
    private final PlayerTweaksData mPlayerTweaksData;
    private static AudioAttributes sAudioAttributes;

    public ExoPlayerInitializer(Context context) {
        mPlayerData = PlayerData.instance(context);
        mPlayerTweaksData = PlayerTweaksData.instance(context);

        long deviceRam = DeviceHelpers.getDeviceRam(context);

        // If ram is too big, bigger then max int value DeviceRam will return a negative number...
        // use 196MB as that can only happens if device has more than 17GB of RAM, so 196 is enough and safe
        // https://github.com/yuliskov/SmartYouTubeTV/issues/532
        mMaxBufferBytes = deviceRam <= 0 ? 196_000_000 : (int)(deviceRam / 18);
    }

    public SimpleExoPlayer createPlayer(Context context, DefaultRenderersFactory renderersFactory, DefaultTrackSelector trackSelector) {
        DefaultLoadControl loadControl = createLoadControl();

        // HDR fix?
        //trackSelector.setParameters(trackSelector.buildUponParameters().setTunnelingAudioSessionId(C.generateAudioSessionIdV21(context)));

        // Old initializer
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context, renderersFactory, trackSelector, loadControl);

        // New initializer
        //SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(
        //        context, renderersFactory, trackSelector, loadControl,
        //        null, new DummyBandwidthMeter(), new AnalyticsCollector.Factory(), Util.getLooper()
        //);

        //enableAudioFocus(player);

        // Lead to numbered errors
        //player.setRepeatMode(Player.REPEAT_MODE_ONE);

        // Fix still image while audio is playing (happens after format change or exit from sleep)
        //player.setPlayWhenReady(true);

        applyPlaybackFixes(player);

        setupAudioFocus(player);

        setupVolumeBoost(player);

        return player;
    }

    private static AudioAttributes getAudioAttributes() {
        if (sAudioAttributes == null) {
            sAudioAttributes = new AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MOVIE)
                    .build();
        }

        return sAudioAttributes;
    }

    private DefaultLoadControl createLoadControl() {

        DefaultLoadControl.Builder baseBuilder = new DefaultLoadControl.Builder();

        int bufferForPlaybackMs = 2_500;
        int bufferForPlaybackAfterRebufferMs = 5_000;

        int minBufferMs = 50_000;
        int maxBufferMs = 100_000;
        
        baseBuilder.setTargetBufferBytes(mMaxBufferBytes);
        
        baseBuilder.setBackBuffer(minBufferMs, true);

        baseBuilder.setBufferDurationsMs(
            minBufferMs, 
            maxBufferMs, 
            bufferForPlaybackMs, 
            bufferForPlaybackAfterRebufferMs
        );

        return baseBuilder.createDefaultLoadControl();
    
    }

    private void setupVolumeBoost(SimpleExoPlayer player) {
        // 5.1 audio cannot be boosted (format isn't supported error)
        // also, other 2.0 tracks in 5.1 group is already too loud. so cancel them too.
        float volume = mPlayerTweaksData.isPlayerAutoVolumeEnabled() ? 2.0f : mPlayerData.getPlayerVolume();
        if (volume > 1f && Build.VERSION.SDK_INT >= 19) {
            VolumeBooster mVolumeBooster = new VolumeBooster(true, volume, player);
            player.addAudioListener(mVolumeBooster);
        }
    }

    /**
     * Manage audio focus. E.g. use Spotify when audio is disabled.
     */
    private void setupAudioFocus(SimpleExoPlayer player) {
        if (player != null && mPlayerTweaksData.isAudioFocusEnabled()) {
            try {
                player.setAudioAttributes(getAudioAttributes(), true);
            } catch (SecurityException e) { // uid 10390 not allowed to perform TAKE_AUDIO_FOCUS
                e.printStackTrace();
            }
        }
    }

    private void applyPlaybackFixes(SimpleExoPlayer player) {
        // Fix seeking on TextureView (some devices only)
        if (mPlayerTweaksData.isTextureViewEnabled()) {
            // Also, live stream (dash) seeking fix
            player.setSeekParameters(SeekParameters.CLOSEST_SYNC);
        }
    }

}
