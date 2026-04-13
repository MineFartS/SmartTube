
package com.google.android.exoplayer2.ext.ffmpeg;

import com.google.android.exoplayer2.audio.AudioDecoderException;

/**
 * Thrown when an FFmpeg decoder error occurs.
 */
public final class FfmpegDecoderException extends AudioDecoderException {

  /* package */ FfmpegDecoderException(String message) {
    super(message);
  }

  /* package */ FfmpegDecoderException(String message, Throwable cause) {
    super(message, cause);
  }
}
