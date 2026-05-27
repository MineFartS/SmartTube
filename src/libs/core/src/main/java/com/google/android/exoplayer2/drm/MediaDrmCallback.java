package com.google.android.exoplayer2.drm;

import com.google.android.exoplayer2.drm.ExoMediaDrm.KeyRequest;
import com.google.android.exoplayer2.drm.ExoMediaDrm.ProvisionRequest;
import java.util.UUID;

/**
 * Performs {@link ExoMediaDrm} key and provisioning requests.
 */
public interface MediaDrmCallback {

  /**
   * Executes a provisioning request.
   *
   * @param uuid The UUID of the content protection scheme.
   * @param request The request.
   * @return The response data.
   * @throws Exception If an error occurred executing the request.
   */
  byte[] executeProvisionRequest(UUID uuid, ProvisionRequest request) throws Exception;

  /**
   * Executes a key request.
   *
   * @param uuid The UUID of the content protection scheme.
   * @param request The request.
   * @return The response data.
   * @throws Exception If an error occurred executing the request.
   */
  byte[] executeKeyRequest(UUID uuid, KeyRequest request) throws Exception;
}
