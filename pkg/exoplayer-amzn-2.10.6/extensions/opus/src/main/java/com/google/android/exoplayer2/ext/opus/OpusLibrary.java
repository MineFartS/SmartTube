
package com.google.android.exoplayer2.ext.opus;

import androidx.annotation.Nullable;
import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.util.LibraryLoader;

/**
 * Configures and queries the underlying native library.
 */
public final class OpusLibrary {

  static {
    ExoPlayerLibraryInfo.registerModule("goog.exo.opus");
  }

  private static final LibraryLoader LOADER = new LibraryLoader("opusV2JNI");

  private OpusLibrary() {}

  /**
   * Override the names of the Opus native libraries. If an application wishes to call this method,
   * it must do so before calling any other method defined by this class, and before instantiating a
   * {@link LibopusAudioRenderer} instance.
   *
   * @param libraries The names of the Opus native libraries.
   */
  public static void setLibraries(String... libraries) {
    LOADER.setLibraries(libraries);
  }

  /**
   * Returns whether the underlying library is available, loading it if necessary.
   */
  public static boolean isAvailable() {
    return LOADER.isAvailable();
  }

  /** Returns the version of the underlying library if available, or null otherwise. */
  @Nullable
  public static String getVersion() {
    return isAvailable() ? opusGetVersion() : null;
  }

  public static native String opusGetVersion();
  public static native boolean opusIsSecureDecodeSupported();
}
