
package com.google.android.exoplayer2.offline;

import com.google.android.exoplayer2.C;

/** Mutable {@link Download} progress. */
public class DownloadProgress {

  /** The number of bytes that have been downloaded. */
  public long bytesDownloaded;

  /** The percentage that has been downloaded, or {@link C#PERCENTAGE_UNSET} if unknown. */
  public float percentDownloaded;
}
