
package com.google.android.exoplayer2.ext.vp9;

import androidx.annotation.Nullable;
import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.util.LibraryLoader;

/**
 * Configures and queries the underlying native library.
 */
public final class VpxLibrary {

  static {
    ExoPlayerLibraryInfo.registerModule("goog.exo.vpx");
  }

  private static final LibraryLoader LOADER = new LibraryLoader("vpx", "vpxV2JNI");

  private VpxLibrary() {}

  /**
   * Override the names of the Vpx native libraries. If an application wishes to call this method,
   * it must do so before calling any other method defined by this class, and before instantiating a
   * {@link LibvpxVideoRenderer} instance.
   *
   * @param libraries The names of the Vpx native libraries.
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
    return isAvailable() ? vpxGetVersion() : null;
  }

  /**
   * Returns the configuration string with which the underlying library was built if available, or
   * null otherwise.
   */
  @Nullable
  public static String getBuildConfig() {
    return isAvailable() ? vpxGetBuildConfig() : null;
  }

  /**
   * Returns true if the underlying libvpx library supports high bit depth.
   */
  public static boolean isHighBitDepthSupported() {
    String config = getBuildConfig();
    int indexHbd = config != null
        ? config.indexOf("--enable-vp9-highbitdepth") : -1;
    return indexHbd >= 0;
  }

  private static native String vpxGetVersion();
  private static native String vpxGetBuildConfig();
  public static native boolean vpxIsSecureDecodeSupported();

}
