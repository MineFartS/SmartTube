
package com.google.android.exoplayer2;

import android.os.Handler;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

/**
 * Builds {@link Renderer} instances for use by a {@link SimpleExoPlayer}.
 */
public interface RenderersFactory {

  /**
   * Builds the {@link Renderer} instances for a {@link SimpleExoPlayer}.
   *
   * @param eventHandler A handler to use when invoking event listeners and outputs.
   * @param videoRendererEventListener An event listener for video renderers.
   * @param audioRendererEventListener An event listener for audio renderers.
   * @param textRendererOutput An output for text renderers.
   * @param metadataRendererOutput An output for metadata renderers.
   * @param drmSessionManager A drm session manager used by renderers.
   * @return The {@link Renderer instances}.
   */
  Renderer[] createRenderers(
      Handler eventHandler,
      VideoRendererEventListener videoRendererEventListener,
      AudioRendererEventListener audioRendererEventListener,
      TextOutput textRendererOutput,
      MetadataOutput metadataRendererOutput,
      @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager);
}
