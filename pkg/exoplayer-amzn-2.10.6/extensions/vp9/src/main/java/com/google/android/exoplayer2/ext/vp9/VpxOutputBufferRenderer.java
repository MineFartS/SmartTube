
package com.google.android.exoplayer2.ext.vp9;

/**
 * Renders the {@link VpxOutputBuffer}.
 */
public interface VpxOutputBufferRenderer {

  /**
   * Sets the output buffer to be rendered. The renderer is responsible for releasing the buffer.
   *
   * @param outputBuffer The output buffer to be rendered.
   */
  void setOutputBuffer(VpxOutputBuffer outputBuffer);

}
