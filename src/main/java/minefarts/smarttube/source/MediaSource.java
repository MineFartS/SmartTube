package minefarts.smarttube.source;

import android.os.Handler;
import androidx.annotation.Nullable;
import minefarts.smarttube.C;
import minefarts.smarttube.ExoPlayer;
import minefarts.smarttube.Timeline;
import minefarts.smarttube.upstream.Allocator;
import minefarts.smarttube.upstream.TransferListener;
import java.io.IOException;

/**
 * Defines and provides media to be played by an {@link minefarts.smarttube.ExoPlayer}. A
 * MediaSource has two main responsibilities:
 *
 * <ul>
 *   <li>To provide the player with a {@link Timeline} defining the structure of its media, and to
 *       provide a new timeline whenever the structure of the media changes. The MediaSource
 *       provides these timelines by calling {@link SourceInfoRefreshListener#onSourceInfoRefreshed}
 *       on the {@link SourceInfoRefreshListener}s passed to {@link
 *       #prepareSource(SourceInfoRefreshListener, TransferListener)}.
 *   <li>To provide {@link MediaPeriod} instances for the periods in its timeline. MediaPeriods are
 *       obtained by calling {@link #createPeriod(MediaPeriodId, Allocator, long)}, and provide a
 *       way for the player to load and read the media.
 * </ul>
 *
 * All methods are called on the player's public playback thread, as described in the {@link
 * minefarts.smarttube.ExoPlayer} Javadoc. They should not be called directly from
 * application code. Instances can be re-used, but only for one {@link
 * minefarts.smarttube.ExoPlayer} instance simultaneously.
 */
public interface MediaSource {

  /** Listener for source events. */
  interface SourceInfoRefreshListener {

    /**
     * Called when manifest and/or timeline has been refreshed.
     * <p>
     * Called on the playback thread.
     *
     * @param source The {@link MediaSource} whose info has been refreshed.
     * @param timeline The source's timeline.
     * @param manifest The loaded manifest. May be null.
     */
    void onSourceInfoRefreshed(MediaSource source, Timeline timeline, @Nullable Object manifest);

  }

    final class MediaPeriodId {

        /** The unique id of the timeline period. */
        public final Object periodUid;

        /**
         * The sequence number of the window in the buffered sequence of windows this media period is
         * part of. {@link C#INDEX_UNSET} if the media period id is not part of a buffered sequence of
         * windows.
         */
        public final long windowSequenceNumber;

        /**
         * Creates a media period identifier for a dummy period which is not part of a buffered sequence
         * of windows.
         *
         * @param periodUid The unique id of the timeline period.
         */
        public MediaPeriodId(Object periodUid) {
            this(
                periodUid, 
                C.INDEX_UNSET
            );
        }

        /**
         * Creates a media period identifier that identifies an ad within an ad group at the specified
         * timeline period.
         *
         * @param periodUid The unique id of the timeline period.
         * @param windowSequenceNumber The sequence number of the window in the buffered sequence of windows this media period is part of.
         */
        public MediaPeriodId(
            Object periodUid,
            long windowSequenceNumber
        ) {
            this.periodUid = periodUid;
            this.windowSequenceNumber = windowSequenceNumber;
        }

        /** Returns a copy of this period identifier but with {@code newPeriodUid} as its period uid. */
        public MediaPeriodId copyWithPeriodUid(Object newPeriodUid) {
            return periodUid.equals(newPeriodUid)
                ? this
                : new MediaPeriodId(newPeriodUid, windowSequenceNumber);
        }

        public boolean equals(@Nullable Object obj) {
            if (this == obj)
                return true;
            
            if (obj == null || getClass() != obj.getClass())
                return false;

            MediaPeriodId periodId = (MediaPeriodId) obj;

            return periodUid.equals(periodId.periodUid)
                && windowSequenceNumber == periodId.windowSequenceNumber;
        }

        public int hashCode() {
            int result = 17;
            result = 31 * result + periodUid.hashCode();
            result = 31 * result + (int) windowSequenceNumber;
            return result;
        }

    }

  /**
   * Adds a {@link MediaSourceEventListener} to the list of listeners which are notified of media
   * source events.
   *
   * @param handler A handler on the which listener events will be posted.
   * @param eventListener The listener to be added.
   */
  void addEventListener(Handler handler, MediaSourceEventListener eventListener);

  /**
   * Removes a {@link MediaSourceEventListener} from the list of listeners which are notified of
   * media source events.
   *
   * @param eventListener The listener to be removed.
   */
  void removeEventListener(MediaSourceEventListener eventListener);

  /** Returns the tag set on the media source, or null if none was set. */
  @Nullable
  default Object getTag() {
    return null;
  }

  /**
   * Starts source preparation if not yet started, and adds a listener for timeline and/or manifest
   * updates.
   *
   * <p>Should not be called directly from application code.
   *
   * <p>The listener will be also be notified if the source already has a timeline and/or manifest.
   *
   * <p>For each call to this method, a call to {@link #releaseSource(SourceInfoRefreshListener)} is
   * needed to remove the listener and to release the source if no longer required.
   *
   * @param listener The listener to be added.
   * @param mediaTransferListener The transfer listener which should be informed of any media data
   *     transfers. May be null if no listener is available. Note that this listener should be only
   *     informed of transfers related to the media loads and not of auxiliary loads for manifests
   *     and other data.
   */
  void prepareSource(
      SourceInfoRefreshListener listener, @Nullable TransferListener mediaTransferListener);

  /**
   * Throws any pending error encountered while loading or refreshing source information.
   * <p>
   * Should not be called directly from application code.
   */
  void maybeThrowSourceInfoRefreshError() throws IOException;

  /**
   * Returns a new {@link MediaPeriod} identified by {@code periodId}. This method may be called
   * multiple times without an intervening call to {@link #releasePeriod(MediaPeriod)}.
   *
   * <p>Should not be called directly from application code.
   *
   * @param id The identifier of the period.
   * @param allocator An {@link Allocator} from which to obtain media buffer allocations.
   * @param startPositionUs The expected start position, in microseconds.
   * @return A new {@link MediaPeriod}.
   */
  MediaPeriod createPeriod(MediaPeriodId id, Allocator allocator, long startPositionUs);

  /**
   * Releases the period.
   * <p>
   * Should not be called directly from application code.
   *
   * @param mediaPeriod The period to release.
   */
  void releasePeriod(MediaPeriod mediaPeriod);

  /**
   * Removes a listener for timeline and/or manifest updates and releases the source if no longer
   * required.
   *
   * <p>Should not be called directly from application code.
   *
   * @param listener The listener to be removed.
   */
  void releaseSource(SourceInfoRefreshListener listener);
}
