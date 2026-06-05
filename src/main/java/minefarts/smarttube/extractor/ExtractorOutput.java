package minefarts.smarttube.extractor;

/**
 * Receives stream level data extracted by an {@link Extractor}.
 */
public interface ExtractorOutput {

  /**
   * Called by the {@link Extractor} to get the {@link TrackOutput} for a specific track.
   * <p>
   * The same {@link TrackOutput} is returned if multiple calls are made with the same {@code id}.
   *
   * @param id A track identifier.
   * @param type The type of the track. Typically one of the {@link minefarts.smarttube.C}
   *     {@code TRACK_TYPE_*} constants.
   * @return The {@link TrackOutput} for the given track identifier.
   */
  TrackOutput track(int id, int type);

  /**
   * Called when all tracks have been identified, meaning no new {@code trackId} values will be
   * passed to {@link #track(int, int)}.
   */
  void endTracks();

  /**
   * Called when a {@link SeekMap} has been extracted from the stream.
   *
   * @param seekMap The extracted {@link SeekMap}.
   */
  void seekMap(SeekMap seekMap);

}
