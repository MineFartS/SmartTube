
package com.google.android.exoplayer2.ext.vp9;

import com.google.android.exoplayer2.decoder.DecoderInputBuffer;
import com.google.android.exoplayer2.video.ColorInfo;

/**
 * Input buffer to a {@link VpxDecoder}.
 */
/* package */ final class VpxInputBuffer extends DecoderInputBuffer {

  public ColorInfo colorInfo;

  public VpxInputBuffer() {
    super(DecoderInputBuffer.BUFFER_REPLACEMENT_MODE_DIRECT);
  }

}
