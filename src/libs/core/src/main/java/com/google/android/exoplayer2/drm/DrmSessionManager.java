
package com.google.android.exoplayer2.drm;

import android.os.Looper;
import com.google.android.exoplayer2.drm.DrmInitData.SchemeData;

/**
 * Manages a DRM session.
 */
public interface DrmSessionManager<T extends ExoMediaCrypto> {

  /**
   * Returns whether the manager is capable of acquiring a session for the given
   * {@link DrmInitData}.
   *
   * @param drmInitData DRM initialization data.
   * @return Whether the manager is capable of acquiring a session for the given
   *     {@link DrmInitData}.
   */
  boolean canAcquireSession(DrmInitData drmInitData);

  /**
   * Acquires a {@link DrmSession} for the specified {@link DrmInitData}. The {@link DrmSession}
   * must be returned to {@link #releaseSession(DrmSession)} when it is no longer required.
   *
   * @param playbackLooper The looper associated with the media playback thread.
   * @param drmInitData DRM initialization data. All contained {@link SchemeData}s must contain
   *     non-null {@link SchemeData#data}.
   * @return The DRM session.
   */
  DrmSession<T> acquireSession(Looper playbackLooper, DrmInitData drmInitData);

  /**
   * Releases a {@link DrmSession}.
   */
  void releaseSession(DrmSession<T> drmSession);

}
