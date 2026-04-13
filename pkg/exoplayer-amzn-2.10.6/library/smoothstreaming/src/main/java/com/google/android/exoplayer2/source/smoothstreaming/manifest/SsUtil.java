
package com.google.android.exoplayer2.source.smoothstreaming.manifest;

import android.net.Uri;
import com.google.android.exoplayer2.util.Util;

/** SmoothStreaming related utility methods. */
public final class SsUtil {

  /** Returns a fixed SmoothStreaming client manifest {@link Uri}. */
  public static Uri fixManifestUri(Uri manifestUri) {
    String lastPathSegment = manifestUri.getLastPathSegment();
    if (lastPathSegment != null
        && Util.toLowerInvariant(lastPathSegment).matches("manifest(\\(.+\\))?")) {
      return manifestUri;
    }
    return Uri.withAppendedPath(manifestUri, "Manifest");
  }

  private SsUtil() {}
}
