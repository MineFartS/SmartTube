package minefarts.smarttube.exoplayer.versions.renderer;

import android.content.Context;
import android.media.MediaCodec;
import android.os.Build.VERSION;
import android.os.Handler;
import android.view.Surface;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.liskovsoft.sharedutils.mylogger.Log;
import minefarts.smarttube.exoplayer.versions.ExoUtils;

public class DebugInfoMediaCodecVideoRenderer extends MediaCodecVideoRenderer {

    // Exo 2.10, 2.11
    public DebugInfoMediaCodecVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector, long allowedJoiningTimeMs,
                                            @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, boolean playClearSamplesWithoutKeys, boolean enableDecoderFallback, @Nullable Handler eventHandler, @Nullable VideoRendererEventListener eventListener, int maxDroppedFramesToNotify) {
        super(context, mediaCodecSelector, allowedJoiningTimeMs, drmSessionManager, playClearSamplesWithoutKeys, enableDecoderFallback, eventHandler, eventListener, maxDroppedFramesToNotify);
    }

    @Override
    protected CodecMaxValues getCodecMaxValues(
            MediaCodecInfo codecInfo, Format format, Format[] streamFormats) {
        ExoUtils.updateVideoDecoderInfo(codecInfo);

        return super.getCodecMaxValues(codecInfo, format, streamFormats);
    }

}
