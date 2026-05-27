package com.google.android.exoplayer2;

import androidx.annotation.Nullable;
import com.google.android.exoplayer2.drm.DecryptionResource;

/**
 * Holds a {@link Format}.
 */
public final class FormatHolder {

  /**
   * Whether the object expected to populate {@link #format} is also expected to populate {@link
   * #decryptionResource}.
   */
  // TODO: Remove once all Renderers and MediaSources have migrated to the new DRM model [Internal
  // ref: b/129764794].
  public boolean decryptionResourceIsProvided;

  /** An accompanying context for decrypting samples in the format. */
  @Nullable public DecryptionResource<?> decryptionResource;

  /** The held {@link Format}. */
  @Nullable public Format format;
}
