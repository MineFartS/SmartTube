
package com.google.android.exoplayer2.ext.vp9;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * A GLSurfaceView extension that scales itself to the given aspect ratio.
 */
public class VpxVideoSurfaceView extends GLSurfaceView implements VpxOutputBufferRenderer {

  private final VpxRenderer renderer;

  public VpxVideoSurfaceView(Context context) {
    this(context, null);
  }

  public VpxVideoSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
    renderer = new VpxRenderer();
    setPreserveEGLContextOnPause(true);
    setEGLContextClientVersion(2);
    setRenderer(renderer);
    setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
  }

  @Override
  public void setOutputBuffer(VpxOutputBuffer outputBuffer) {
    renderer.setFrame(outputBuffer);
    requestRender();
  }

}
