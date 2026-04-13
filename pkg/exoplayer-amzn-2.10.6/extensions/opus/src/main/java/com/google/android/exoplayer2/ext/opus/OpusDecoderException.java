
package com.google.android.exoplayer2.ext.opus;

import com.google.android.exoplayer2.audio.AudioDecoderException;

/**
 * Thrown when an Opus decoder error occurs.
 */
public final class OpusDecoderException extends AudioDecoderException {

  /* package */ OpusDecoderException(String message) {
    super(message);
  }

  /* package */ OpusDecoderException(String message, Throwable cause) {
    super(message, cause);
  }

}
