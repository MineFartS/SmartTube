package minefarts.smarttube.ss.manifest;

import android.net.Uri;
import minefarts.smarttube.utils.Utils;

/** SmoothStreaming related utility methods. */
public final class SsUtil {

  /** Returns a fixed SmoothStreaming client manifest {@link Uri}. */
  public static Uri fixManifestUri(Uri manifestUri) {
    String lastPathSegment = manifestUri.getLastPathSegment();
    if (lastPathSegment != null
        && Utils.toLowerInvariant(lastPathSegment).matches("manifest(\\(.+\\))?")) {
      return manifestUri;
    }
    return Uri.withAppendedPath(manifestUri, "Manifest");
  }

  private SsUtil() {}
}
