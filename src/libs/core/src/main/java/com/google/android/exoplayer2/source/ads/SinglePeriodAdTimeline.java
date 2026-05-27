package com.google.android.exoplayer2.source.ads;

import androidx.annotation.VisibleForTesting;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ForwardingTimeline;
import com.google.android.exoplayer2.util.Assertions;

/** A {@link Timeline} for sources that have ads. */
@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
public final class SinglePeriodAdTimeline extends ForwardingTimeline {

  private final AdPlaybackState adPlaybackState;

  /**
   * Creates a new timeline with a single period containing ads.
   *
   * @param contentTimeline The timeline of the content alongside which ads will be played. It must
   *     have one window and one period.
   * @param adPlaybackState The state of the period's ads.
   */
  public SinglePeriodAdTimeline(Timeline contentTimeline, AdPlaybackState adPlaybackState) {
    super(contentTimeline);
    Assertions.checkState(contentTimeline.getPeriodCount() == 1);
    Assertions.checkState(contentTimeline.getWindowCount() == 1);
    this.adPlaybackState = adPlaybackState;
  }

  @Override
  public Period getPeriod(int periodIndex, Period period, boolean setIds) {
    timeline.getPeriod(periodIndex, period, setIds);
    period.set(
        period.id,
        period.uid,
        period.windowIndex,
        period.durationUs,
        period.getPositionInWindowUs(),
        adPlaybackState);
    return period;
  }

  @Override
  public Window getWindow(
      int windowIndex, Window window, boolean setTag, long defaultPositionProjectionUs) {
    window = super.getWindow(windowIndex, window, setTag, defaultPositionProjectionUs);
    if (window.durationUs == C.TIME_UNSET) {
      window.durationUs = adPlaybackState.contentDurationUs;
    }
    return window;
  }

}
