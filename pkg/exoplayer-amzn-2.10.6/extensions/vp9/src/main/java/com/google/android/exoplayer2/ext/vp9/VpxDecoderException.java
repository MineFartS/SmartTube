
package com.google.android.exoplayer2.ext.vp9;

/** Thrown when a libvpx decoder error occurs. */
public final class VpxDecoderException extends Exception {

  /* package */ VpxDecoderException(String message) {
    super(message);
  }

  /* package */ VpxDecoderException(String message, Throwable cause) {
    super(message, cause);
  }
}
