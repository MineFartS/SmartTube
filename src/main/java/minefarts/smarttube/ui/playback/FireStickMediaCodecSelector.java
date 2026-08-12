package minefarts.smarttube.ui.playback;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FireStickMediaCodecSelector implements MediaCodecSelector {

    @Override
    public List<MediaCodecInfo> getDecoderInfos(
            String mimeType, 
            boolean requiresSecureDecoder, 
            boolean requiresTunnelingDecoder
    ) throws MediaCodecUtil.DecoderQueryException {
        
        // 1. Fetch ExoPlayer's default immutable platform decoder list
        List<MediaCodecInfo> defaultDecoders = MediaCodecSelector.DEFAULT.getDecoderInfos(
                mimeType, requiresSecureDecoder, requiresTunnelingDecoder
        );

        if (defaultDecoders.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Clone it into a mutable ArrayList to prevent UnmodifiableList crashes
        List<MediaCodecInfo> mutableDecoders = new ArrayList<>(defaultDecoders);

        // 3. Shim Logic: Handle Fire Stick HEVC hardware profile rejections
        if ("video/hevc".equals(mimeType)) {
            // Push hardware decoders with strict/broken profile checks to the back,
            // or bubble up a stable software/alternative decoder profile wrapper.
            Collections.reverse(mutableDecoders); 
        }

        return mutableDecoders;
    }

    @Nullable @Override
    public MediaCodecInfo getPassthroughDecoderInfo() throws MediaCodecUtil.DecoderQueryException {
        return MediaCodecUtil.getPassthroughDecoderInfo();
    }

}
