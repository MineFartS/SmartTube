package com.google.android.exoplayer2.trackselection;

import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.chunk.MediaChunk;
import com.google.android.exoplayer2.source.chunk.MediaChunkIterator;
import java.util.List;

/** A {@link TrackBitrateEstimator} which derives estimates from a window of time. */
public final class WindowedTrackBitrateEstimator implements TrackBitrateEstimator {

  private final long maxPastDurationUs;
  private final long maxFutureDurationUs;
  private final boolean useFormatBitrateAsLowerBound;

  /**
   * @param maxPastDurationMs Maximum duration of past chunks to be included in average bitrate
   *     values, in milliseconds.
   * @param maxFutureDurationMs Maximum duration of future chunks to be included in average bitrate
   *     values, in milliseconds.
   * @param useFormatBitrateAsLowerBound Whether to use the bitrate of the track's format as a lower
   *     bound for the estimated bitrate.
   */
  public WindowedTrackBitrateEstimator(
      long maxPastDurationMs, long maxFutureDurationMs, boolean useFormatBitrateAsLowerBound) {
    this.maxPastDurationUs = C.msToUs(maxPastDurationMs);
    this.maxFutureDurationUs = C.msToUs(maxFutureDurationMs);
    this.useFormatBitrateAsLowerBound = useFormatBitrateAsLowerBound;
  }

  @Override
  public int[] getBitrates(
      Format[] formats,
      List<? extends MediaChunk> queue,
      MediaChunkIterator[] iterators,
      @Nullable int[] bitrates) {
    if (maxFutureDurationUs > 0 || maxPastDurationUs > 0) {
      return TrackSelectionUtil.getBitratesUsingPastAndFutureInfo(
          formats,
          queue,
          maxPastDurationUs,
          iterators,
          maxFutureDurationUs,
          useFormatBitrateAsLowerBound,
          bitrates);
    }
    return TrackSelectionUtil.getFormatBitrates(formats, bitrates);
  }
}
