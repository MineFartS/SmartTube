package minefarts.smarttube.sabr;

import android.os.SystemClock;

import androidx.annotation.Nullable;

import minefarts.smarttube.Format;
import minefarts.smarttube.source.chunk.ChunkSource;
import minefarts.smarttube.sabr.PlayerEmsgHandler.PlayerTrackEmsgHandler;
import minefarts.smarttube.sabr.manifest.SabrManifest;
import minefarts.smarttube.trackselection.TrackSelection;
import minefarts.smarttube.upstream.LoaderErrorThrower;
import minefarts.smarttube.upstream.TransferListener;

import java.util.List;

/**
 * An {@link ChunkSource} for DASH streams.
 */
public interface SabrChunkSource extends ChunkSource {

  /** Factory for {@link SabrChunkSource}s. */
  interface Factory {

    /**
     * @param manifestLoaderErrorThrower Throws errors affecting loading of manifests.
     * @param manifest The initial manifest.
     * @param periodIndex The index of the corresponding period in the manifest.
     * @param adaptationSetIndices The indices of the corresponding adaptation sets in the period.
     * @param trackSelection The track selection.
     * @param elapsedRealtimeOffsetMs If known, an estimate of the instantaneous difference between
     *     server-side unix time and {@link SystemClock#elapsedRealtime()} in milliseconds,
     *     specified as the server's unix time minus the local elapsed time. If unknown, set to 0.
     * @param enableEventMessageTrack Whether to output an event message track.
     * @param closedCaptionFormats The {@link Format Formats} of closed caption tracks to be output.
     * @param transferListener The transfer listener which should be informed of any data transfers.
     *     May be null if no listener is available.
     * @return The created {@link SabrChunkSource}.
     */
    SabrChunkSource createSabrChunkSource(
        LoaderErrorThrower manifestLoaderErrorThrower,
        SabrManifest manifest,
        int periodIndex,
        int[] adaptationSetIndices,
        TrackSelection trackSelection,
        int type,
        long elapsedRealtimeOffsetMs,
        boolean enableEventMessageTrack,
        List<Format> closedCaptionFormats,
        @Nullable PlayerTrackEmsgHandler playerEmsgHandler,
        @Nullable TransferListener transferListener);
  }

  /**
   * Updates the manifest.
   *
   * @param newManifest The new manifest.
   */
  void updateManifest(SabrManifest newManifest, int periodIndex);

  /**
   * Updates the track selection.
   *
   * @param trackSelection The new track selection instance. Must be equivalent to the previous one.
   */
  void updateTrackSelection(TrackSelection trackSelection);
}
