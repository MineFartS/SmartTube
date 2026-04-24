
package com.google.android.exoplayer2.drm;

import android.media.MediaCrypto;
import java.util.UUID;

/**
 * An {@link ExoMediaCrypto} implementation that contains the necessary information to build or
 * update a framework {@link MediaCrypto}.
 */
public final class FrameworkMediaCrypto implements ExoMediaCrypto {

  /** The DRM scheme UUID. */
  public final UUID uuid;
  /** The DRM session id. */
  public final byte[] sessionId;
  /**
   * Whether to allow use of insecure decoder components even if the underlying platform says
   * otherwise.
   */
  public final boolean forceAllowInsecureDecoderComponents;

  /**
   * @param uuid The DRM scheme UUID.
   * @param sessionId The DRM session id.
   * @param forceAllowInsecureDecoderComponents Whether to allow use of insecure decoder components
   *     even if the underlying platform says otherwise.
   */
  public FrameworkMediaCrypto(
      UUID uuid, byte[] sessionId, boolean forceAllowInsecureDecoderComponents) {
    this.uuid = uuid;
    this.sessionId = sessionId;
    this.forceAllowInsecureDecoderComponents = forceAllowInsecureDecoderComponents;
  }
}
