package com.liskovsoft.smartyoutubetv2.common.exoplayer.versions.renderer;

import android.content.Context;
import android.os.Handler;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.DefaultAudioSink;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.util.AmazonQuirks;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.liskovsoft.smartyoutubetv2.common.exoplayer.versions.selector.BlacklistMediaCodecSelector;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerData;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerTweaksData;

import java.util.ArrayList;

/**
 * Main intent: override audio delay
 */
public class CustomOverridesRenderersFactory extends CustomRenderersFactoryBase {

    private final PlayerData mPlayerData;
    private final PlayerTweaksData mPlayerTweaksData;

    // 2.9, 2.10, 2.11
    public CustomOverridesRenderersFactory(Context activity) {
        super(activity);

        mPlayerData = PlayerData.instance(activity);
        mPlayerTweaksData = PlayerTweaksData.instance(activity);

        setExtensionRendererMode(EXTENSION_RENDERER_MODE_ON);

        if (mPlayerTweaksData.isSWDecoderForced()) {
            setMediaCodecSelector(new BlacklistMediaCodecSelector());
        }

        AmazonQuirks.disableSnappingToVsync(mPlayerTweaksData.isSnappingToVsyncDisabled());
        AmazonQuirks.skipProfileLevelCheck(mPlayerTweaksData.isProfileLevelCheckSkipped());
    }

    // 2.10, 2.11
    @Override
    protected void buildAudioRenderers(Context context, @ExtensionRendererMode int extensionRendererMode, MediaCodecSelector mediaCodecSelector,
                                       @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, boolean playClearSamplesWithoutKeys,
                                       boolean enableDecoderFallback, AudioProcessor[] audioProcessors, Handler eventHandler,
                                       AudioRendererEventListener eventListener, ArrayList<Renderer> out) {
        super.buildAudioRenderers(context, extensionRendererMode, mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys,
                enableDecoderFallback, audioProcessors, eventHandler, eventListener, out);

        if (mPlayerData.getAudioDelayMs() == 0 && !mPlayerTweaksData.isAudioSyncFixEnabled()) {
            // Improve performance a bit by eliminating calculations presented in custom renderer.

            return;
        }

        DelayMediaCodecAudioRenderer audioRenderer =
                new DelayMediaCodecAudioRenderer(context, mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys, enableDecoderFallback,
                        eventHandler, eventListener, new DefaultAudioSink(AudioCapabilities.getCapabilities(context), audioProcessors));

        audioRenderer.setAudioDelayMs(mPlayerData.getAudioDelayMs());
        audioRenderer.enableAudioSyncFix(mPlayerTweaksData.isAudioSyncFixEnabled());

        replaceAudioRenderer(out, audioRenderer);
    }

    // 2.10, 2.11
    @Override
    protected void buildVideoRenderers(Context context, int extensionRendererMode, MediaCodecSelector mediaCodecSelector,
                                       @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, boolean playClearSamplesWithoutKeys,
                                       boolean enableDecoderFallback, Handler eventHandler, VideoRendererEventListener eventListener,
                                       long allowedVideoJoiningTimeMs, ArrayList<Renderer> out) {
        super.buildVideoRenderers(context, extensionRendererMode, mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys,
                enableDecoderFallback, eventHandler, eventListener, allowedVideoJoiningTimeMs, out);
        
        if (!mPlayerTweaksData.isAmazonFrameDropFixEnabled()) {
            // Improve performance a bit by eliminating some if conditions presented in tweaks.
            // But we need to obtain codec real name somehow. So use interceptor below.

            DebugInfoMediaCodecVideoRenderer videoRenderer =
                    new DebugInfoMediaCodecVideoRenderer(context, mediaCodecSelector, allowedVideoJoiningTimeMs, drmSessionManager,
                        playClearSamplesWithoutKeys, enableDecoderFallback, eventHandler, eventListener, MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY);

            videoRenderer.enableSetOutputSurfaceWorkaround(true); // Force enable?

            replaceVideoRenderer(out, videoRenderer);

            return;
        }

        TweaksMediaCodecVideoRenderer videoRenderer =
                new TweaksMediaCodecVideoRenderer(context, mediaCodecSelector, allowedVideoJoiningTimeMs, drmSessionManager,
                        playClearSamplesWithoutKeys, enableDecoderFallback, eventHandler, eventListener, MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY);

        videoRenderer.enableFrameDropFix(mPlayerTweaksData.isAmazonFrameDropFixEnabled());

        videoRenderer.enableSetOutputSurfaceWorkaround(true); // Force enable?

        replaceVideoRenderer(out, videoRenderer);
    }

}
