package minefarts.smarttube.source;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import minefarts.smarttube.Timeline;
import minefarts.smarttube.upstream.TransferListener;
import minefarts.smarttube.utils.Assertions;
import java.util.ArrayList;

/**
 * Base {@link MediaSource} implementation to handle parallel reuse and to keep a list of {@link
 * MediaSourceEventListener}s.
 *
 * <p>Whenever an implementing subclass needs to provide a new timeline and/or manifest, it must
 * call {@link #refreshSourceInfo(Timeline, Object)} to notify all listeners.
 */
public abstract class BaseMediaSource implements MediaSource {

  private final ArrayList<SourceInfoRefreshListener> sourceInfoListeners;
  private final MediaSourceEventListener.EventDispatcher eventDispatcher;

  @Nullable private Looper looper;
  @Nullable private Timeline timeline;
  @Nullable private Object manifest;

  public BaseMediaSource() {
    sourceInfoListeners = new ArrayList<>(/* initialCapacity= */ 1);
    eventDispatcher = new MediaSourceEventListener.EventDispatcher();
  }

  /**
   * Starts source preparation. This method is called at most once until the next call to {@link
   * #releaseSourceInternal()}.
   *
   * @param mediaTransferListener The transfer listener which should be informed of any media data
   *     transfers. May be null if no listener is available. Note that this listener should usually
   *     be only informed of transfers related to the media loads and not of auxiliary loads for
   *     manifests and other data.
   */
  protected abstract void prepareSourceInternal(@Nullable TransferListener mediaTransferListener);

  /**
   * Releases the source. This method is called exactly once after each call to {@link
   * #prepareSourceInternal(TransferListener)}.
   */
  protected abstract void releaseSourceInternal();

  /**
   * Updates timeline and manifest and notifies all listeners of the update.
   *
   * @param timeline The new {@link Timeline}.
   * @param manifest The new manifest. May be null.
   */
  protected final void refreshSourceInfo(Timeline timeline, @Nullable Object manifest) {
    this.timeline = timeline;
    this.manifest = manifest;
    for (SourceInfoRefreshListener listener : sourceInfoListeners) {
      listener.onSourceInfoRefreshed(/* source= */ this, timeline, manifest);
    }
  }

  /**
   * Returns a {@link MediaSourceEventListener.EventDispatcher} which dispatches all events to the
   * registered listeners with the specified media period id.
   *
   * @param mediaPeriodId The {@link MediaPeriodId} to be reported with the events. May be null, if
   *     the events do not belong to a specific media period.
   * @return An event dispatcher with pre-configured media period id.
   */
  protected final MediaSourceEventListener.EventDispatcher createEventDispatcher(
      @Nullable MediaPeriodId mediaPeriodId) {
    return eventDispatcher.withParameters(
        /* windowIndex= */ 0, mediaPeriodId, /* mediaTimeOffsetMs= */ 0);
  }

  /**
   * Returns a {@link MediaSourceEventListener.EventDispatcher} which dispatches all events to the
   * registered listeners with the specified media period id and time offset.
   *
   * @param mediaPeriodId The {@link MediaPeriodId} to be reported with the events.
   * @param mediaTimeOffsetMs The offset to be added to all media times, in milliseconds.
   * @return An event dispatcher with pre-configured media period id and time offset.
   */
  protected final MediaSourceEventListener.EventDispatcher createEventDispatcher(
      MediaPeriodId mediaPeriodId, long mediaTimeOffsetMs) {
    Assertions.checkArgument(mediaPeriodId != null);
    return eventDispatcher.withParameters(/* windowIndex= */ 0, mediaPeriodId, mediaTimeOffsetMs);
  }

  /**
   * Returns a {@link MediaSourceEventListener.EventDispatcher} which dispatches all events to the
   * registered listeners with the specified window index, media period id and time offset.
   *
   * @param windowIndex The timeline window index to be reported with the events.
   * @param mediaPeriodId The {@link MediaPeriodId} to be reported with the events. May be null, if
   *     the events do not belong to a specific media period.
   * @param mediaTimeOffsetMs The offset to be added to all media times, in milliseconds.
   * @return An event dispatcher with pre-configured media period id and time offset.
   */
  protected final MediaSourceEventListener.EventDispatcher createEventDispatcher(
      int windowIndex, @Nullable MediaPeriodId mediaPeriodId, long mediaTimeOffsetMs) {
    return eventDispatcher.withParameters(windowIndex, mediaPeriodId, mediaTimeOffsetMs);
  }

  @Override
  public final void addEventListener(Handler handler, MediaSourceEventListener eventListener) {
    eventDispatcher.addEventListener(handler, eventListener);
  }

  @Override
  public final void removeEventListener(MediaSourceEventListener eventListener) {
    eventDispatcher.removeEventListener(eventListener);
  }

  @Override
  public final void prepareSource(
      SourceInfoRefreshListener listener,
      @Nullable TransferListener mediaTransferListener) {
    Looper looper = Looper.myLooper();
    Assertions.checkArgument(this.looper == null || this.looper == looper);
    sourceInfoListeners.add(listener);
    if (this.looper == null) {
      this.looper = looper;
      prepareSourceInternal(mediaTransferListener);
    } else if (timeline != null) {
      listener.onSourceInfoRefreshed(/* source= */ this, timeline, manifest);
    }
  }

  @Override
  public final void releaseSource(SourceInfoRefreshListener listener) {
    sourceInfoListeners.remove(listener);
    if (sourceInfoListeners.isEmpty()) {
      looper = null;
      timeline = null;
      manifest = null;
      releaseSourceInternal();
    }
  }
}
