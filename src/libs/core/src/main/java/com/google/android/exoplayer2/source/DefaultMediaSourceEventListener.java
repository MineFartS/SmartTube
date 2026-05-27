package com.google.android.exoplayer2.source;

import androidx.annotation.Nullable;
import com.google.android.exoplayer2.source.MediaSource.MediaPeriodId;
import java.io.IOException;

/**
 * A {@link MediaSourceEventListener} allowing selective overrides. All methods are implemented as
 * no-ops.
 */
public abstract class DefaultMediaSourceEventListener implements MediaSourceEventListener {

  @Override
  public void onMediaPeriodCreated(int windowIndex, MediaPeriodId mediaPeriodId) {
    // Do nothing.
  }

  @Override
  public void onMediaPeriodReleased(int windowIndex, MediaPeriodId mediaPeriodId) {
    // Do nothing.
  }

  @Override
  public void onLoadStarted(
      int windowIndex,
      @Nullable MediaPeriodId mediaPeriodId,
      LoadEventInfo loadEventInfo,
      MediaLoadData mediaLoadData) {
    // Do nothing.
  }

  @Override
  public void onLoadCompleted(
      int windowIndex,
      @Nullable MediaPeriodId mediaPeriodId,
      LoadEventInfo loadEventInfo,
      MediaLoadData mediaLoadData) {
    // Do nothing.
  }

  @Override
  public void onLoadCanceled(
      int windowIndex,
      @Nullable MediaPeriodId mediaPeriodId,
      LoadEventInfo loadEventInfo,
      MediaLoadData mediaLoadData) {
    // Do nothing.
  }

  @Override
  public void onLoadError(
      int windowIndex,
      @Nullable MediaPeriodId mediaPeriodId,
      LoadEventInfo loadEventInfo,
      MediaLoadData mediaLoadData,
      IOException error,
      boolean wasCanceled) {
    // Do nothing.
  }

  @Override
  public void onReadingStarted(int windowIndex, MediaPeriodId mediaPeriodId) {
    // Do nothing.
  }

  @Override
  public void onUpstreamDiscarded(
      int windowIndex, @Nullable MediaPeriodId mediaPeriodId, MediaLoadData mediaLoadData) {
    // Do nothing.
  }

  @Override
  public void onDownstreamFormatChanged(
      int windowIndex, @Nullable MediaPeriodId mediaPeriodId, MediaLoadData mediaLoadData) {
    // Do nothing.
  }
}
