package com.google.android.exoplayer2;

import androidx.annotation.Nullable;

/**
 * The configuration of a {@link Renderer}.
 */
public final class RendererConfiguration {

  /**
   * The default configuration.
   */
  public static final RendererConfiguration DEFAULT =
      new RendererConfiguration(C.AUDIO_SESSION_ID_UNSET);

  /**
   * The audio session id to use for tunneling, or {@link C#AUDIO_SESSION_ID_UNSET} if tunneling
   * should not be enabled.
   */
  public final int tunnelingAudioSessionId;

  /**
   * @param tunnelingAudioSessionId The audio session id to use for tunneling, or
   *     {@link C#AUDIO_SESSION_ID_UNSET} if tunneling should not be enabled.
   */
  public RendererConfiguration(int tunnelingAudioSessionId) {
    this.tunnelingAudioSessionId = tunnelingAudioSessionId;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    RendererConfiguration other = (RendererConfiguration) obj;
    return tunnelingAudioSessionId == other.tunnelingAudioSessionId;
  }

  @Override
  public int hashCode() {
    return tunnelingAudioSessionId;
  }

}
