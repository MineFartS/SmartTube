
package com.google.android.exoplayer2.util;

import com.google.android.exoplayer2.PlaybackParameters;

/**
 * Tracks the progression of media time.
 */
public interface MediaClock {

  /**
   * Returns the current media position in microseconds.
   */
  long getPositionUs();

  /**
   * Attempts to set the playback parameters and returns the active playback parameters, which may
   * differ from those passed in.
   *
   * @param playbackParameters The playback parameters.
   * @return The active playback parameters.
   */
  PlaybackParameters setPlaybackParameters(PlaybackParameters playbackParameters);

  /**
   * Returns the active playback parameters.
   */
  PlaybackParameters getPlaybackParameters();

}
