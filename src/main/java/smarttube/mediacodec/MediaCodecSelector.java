package minefarts.smarttube.mediacodec;

import android.media.MediaCodec;
import androidx.annotation.Nullable;
import minefarts.smarttube.mediacodec.MediaCodecUtil.DecoderQueryException;
import java.util.List;

/**
 * Selector of {@link MediaCodec} instances.
 */
public interface MediaCodecSelector {

  /**
   * Default implementation of {@link MediaCodecSelector}, which returns the preferred decoder for
   * the given format.
   */
  MediaCodecSelector DEFAULT =
      new MediaCodecSelector() {
        @Override
        public List<MediaCodecInfo> getDecoderInfos(
            String mimeType, boolean requiresSecureDecoder, boolean requiresTunnelingDecoder)
            throws DecoderQueryException {
          return MediaCodecUtil.getDecoderInfos(
              mimeType, requiresSecureDecoder, requiresTunnelingDecoder);
        }

        @Override
        public @Nullable MediaCodecInfo getPassthroughDecoderInfo() throws DecoderQueryException {
          return MediaCodecUtil.getPassthroughDecoderInfo();
        }
      };

  /**
   * Returns a list of decoders that can decode media in the specified MIME type, in priority order.
   *
   * @param mimeType The MIME type for which a decoder is required.
   * @param requiresSecureDecoder Whether a secure decoder is required.
   * @param requiresTunnelingDecoder Whether a tunneling decoder is required.
   * @return An unmodifiable list of {@link MediaCodecInfo}s corresponding to decoders. May be
   *     empty.
   * @throws DecoderQueryException Thrown if there was an error querying decoders.
   */
  List<MediaCodecInfo> getDecoderInfos(
      String mimeType, boolean requiresSecureDecoder, boolean requiresTunnelingDecoder)
      throws DecoderQueryException;

  /**
   * Selects a decoder to instantiate for audio passthrough.
   *
   * @return A {@link MediaCodecInfo} describing the decoder, or null if no suitable decoder exists.
   * @throws DecoderQueryException Thrown if there was an error querying decoders.
   */
  @Nullable
  MediaCodecInfo getPassthroughDecoderInfo() throws DecoderQueryException;
}
