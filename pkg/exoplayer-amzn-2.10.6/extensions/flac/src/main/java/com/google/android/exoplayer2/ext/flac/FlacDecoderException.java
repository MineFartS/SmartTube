
package com.google.android.exoplayer2.ext.flac;

import com.google.android.exoplayer2.audio.AudioDecoderException;

/**
 * Thrown when an Flac decoder error occurs.
 */
public final class FlacDecoderException extends AudioDecoderException {

  /* package */ FlacDecoderException(String message) {
    super(message);
  }

  /* package */ FlacDecoderException(String message, Throwable cause) {
    super(message, cause);
  }
}
