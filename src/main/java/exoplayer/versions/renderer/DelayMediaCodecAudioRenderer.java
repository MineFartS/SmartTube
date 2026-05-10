package minefarts.smarttube.exoplayer.versions.renderer;

import android.content.Context;
import android.media.MediaCodec;
import android.os.Handler;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.AudioSink;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.liskovsoft.sharedutils.helpers.Helpers;

import java.nio.ByteBuffer;

public class DelayMediaCodecAudioRenderer extends MediaCodecAudioRenderer {

    private int mDelayUs;

    // Exo 2.10, 2.11
    public DelayMediaCodecAudioRenderer(
        Context context, 
        MediaCodecSelector mediaCodecSelector,
        @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
        boolean playClearSamplesWithoutKeys, 
        boolean enableDecoderFallback, 
        @Nullable Handler eventHandler,
        @Nullable AudioRendererEventListener eventListener, 
        AudioSink audioSink
    ) {
        super(
            context, 
            mediaCodecSelector, 
            drmSessionManager, 
            playClearSamplesWithoutKeys, 
            enableDecoderFallback, 
            eventHandler, 
            eventListener, 
            audioSink
        );
    }

    @Override
    public long getPositionUs() {
        return super.getPositionUs() + mDelayUs;
    }

    public void setAudioDelayMs(int delayMs) {
        mDelayUs = delayMs * 1_000;
    }

    public int getAudioDelayMs() {
        return mDelayUs / 1_000;
    }

    @Override
    protected boolean processOutputBuffer(
        long positionUs, 
        long elapsedRealtimeUs, 
        MediaCodec codec, 
        ByteBuffer buffer, 
        int bufferIndex,
        int bufferFlags, 
        long bufferPresentationTimeUs, 
        boolean isDecodeOnlyBuffer, 
        boolean isLastBuffer, 
        Format format
    ) throws ExoPlaybackException {

        return super.processOutputBuffer(
            positionUs, 
            elapsedRealtimeUs, 
            codec, 
            buffer, 
            bufferIndex, 
            bufferFlags,
            bufferPresentationTimeUs, 
            isDecodeOnlyBuffer, 
            isLastBuffer, 
            format
        );

    }

}
