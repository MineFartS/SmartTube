package minefarts.exoplayer2.drm;

import minefarts.exoplayer2.drm.ExoMediaDrm.KeyRequest;
import minefarts.exoplayer2.drm.ExoMediaDrm.ProvisionRequest;
import minefarts.exoplayer2.util.Assertions;
import java.io.IOException;
import java.util.UUID;

/**
 * A {@link MediaDrmCallback} that provides a fixed response to key requests. Provisioning is not
 * supported. This implementation is primarily useful for providing locally stored keys to decrypt
 * ClearKey protected content. It is not suitable for use with Widevine or PlayReady protected
 * content.
 */
public final class LocalMediaDrmCallback implements MediaDrmCallback {

  private final byte[] keyResponse;

  /**
   * @param keyResponse The fixed response for all key requests.
   */
  public LocalMediaDrmCallback(byte[] keyResponse) {
    this.keyResponse = Assertions.checkNotNull(keyResponse);
  }

  @Override
  public byte[] executeProvisionRequest(UUID uuid, ProvisionRequest request) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public byte[] executeKeyRequest(UUID uuid, KeyRequest request) throws Exception {
    return keyResponse;
  }

}
