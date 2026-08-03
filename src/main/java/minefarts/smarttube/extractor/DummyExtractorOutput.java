package minefarts.smarttube.extractor;

/** A dummy {@link ExtractorOutput} implementation. */
public final class DummyExtractorOutput implements ExtractorOutput {

  @Override
  public TrackOutput track(int id, int type) {
    return new DummyTrackOutput();
  }

  @Override
  public void endTracks() {
    // Do nothing.
  }

  @Override
  public void seekMap(SeekMap seekMap) {
    // Do nothing.
  }
}
