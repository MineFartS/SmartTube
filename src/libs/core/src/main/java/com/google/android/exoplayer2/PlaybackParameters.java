package com.google.android.exoplayer2;

import androidx.annotation.Nullable;
import com.google.android.exoplayer2.util.Assertions;

/**
 * The parameters that apply to playback.
 */
public final class PlaybackParameters {

  /**
   * The default playback parameters: real-time playback with no pitch modification or silence
   * skipping.
   */
  public static final PlaybackParameters DEFAULT = new PlaybackParameters(1f);

  /** The factor by which playback will be sped up. */
  public final float speed;

  /** Whether to skip silence in the input. */
  public final boolean skipSilence;

  private final int scaledUsPerMs;

  /**
   * Creates new playback parameters that set the playback speed.
   *
   * @param speed The factor by which playback will be sped up. Must be greater than zero.
   */
  public PlaybackParameters(float speed) {
    this(speed, false);
  }

  /**
   * Creates new playback parameters that set the playback speed, audio pitch scaling factor and
   * whether to skip silence in the audio stream.
   *
   * @param speed The factor by which playback will be sped up. Must be greater than zero.
   * @param skipSilence Whether to skip silences in the audio stream.
   */
  public PlaybackParameters(float speed, boolean skipSilence) {
    Assertions.checkArgument(speed > 0);
    this.speed = speed;
    this.skipSilence = skipSilence;
    scaledUsPerMs = Math.round(speed * 1000f);
  }

  /**
   * Returns the media time in microseconds that will elapse in {@code timeMs} milliseconds of
   * wallclock time.
   *
   * @param timeMs The time to scale, in milliseconds.
   * @return The scaled time, in microseconds.
   */
  public long getMediaTimeUsForPlayoutTimeMs(long timeMs) {
    return timeMs * scaledUsPerMs;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    PlaybackParameters other = (PlaybackParameters) obj;
    return this.speed == other.speed
        && this.skipSilence == other.skipSilence;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + Float.floatToRawIntBits(speed);
    result = 31 * result + 1;
    result = 31 * result + (skipSilence ? 1 : 0);
    return result;
  }

}
