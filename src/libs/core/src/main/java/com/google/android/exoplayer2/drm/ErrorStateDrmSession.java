
package com.google.android.exoplayer2.drm;

import androidx.annotation.Nullable;
import com.google.android.exoplayer2.util.Assertions;
import java.util.Map;

/** A {@link DrmSession} that's in a terminal error state. */
public final class ErrorStateDrmSession<T extends ExoMediaCrypto> implements DrmSession<T> {

  private final DrmSessionException error;

  public ErrorStateDrmSession(DrmSessionException error) {
    this.error = Assertions.checkNotNull(error);
  }

  @Override
  public int getState() {
    return STATE_ERROR;
  }

  @Override
  public @Nullable DrmSessionException getError() {
    return error;
  }

  @Override
  public @Nullable T getMediaCrypto() {
    return null;
  }

  @Override
  public @Nullable Map<String, String> queryKeyStatus() {
    return null;
  }

  @Override
  public @Nullable byte[] getOfflineLicenseKeySetId() {
    return null;
  }

}
