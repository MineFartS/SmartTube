package minefarts.smarttube.source.chunk;

import minefarts.smarttube.C;
import minefarts.smarttube.Format;
import minefarts.smarttube.extractor.DefaultExtractorInput;
import minefarts.smarttube.extractor.ExtractorInput;
import minefarts.smarttube.extractor.TrackOutput;
import minefarts.smarttube.upstream.DataSource;
import minefarts.smarttube.upstream.DataSpec;
import minefarts.smarttube.utils.Utils;
import java.io.IOException;

/**
 * A {@link BaseMediaChunk} for chunks consisting of a single raw sample.
 */
public final class SingleSampleMediaChunk extends BaseMediaChunk {

  private final int trackType;
  private final Format sampleFormat;

  private long nextLoadPosition;
  private boolean loadCompleted;

  /**
   * @param dataSource The source from which the data should be loaded.
   * @param dataSpec Defines the data to be loaded.
   * @param trackFormat See {@link #trackFormat}.
   * @param trackSelectionReason See {@link #trackSelectionReason}.
   * @param trackSelectionData See {@link #trackSelectionData}.
   * @param startTimeUs The start time of the media contained by the chunk, in microseconds.
   * @param endTimeUs The end time of the media contained by the chunk, in microseconds.
   * @param chunkIndex The index of the chunk, or {@link C#INDEX_UNSET} if it is not known.
   * @param trackType The type of the chunk. Typically one of the {@link C} {@code TRACK_TYPE_*}
   *     constants.
   * @param sampleFormat The {@link Format} of the sample in the chunk.
   */
  public SingleSampleMediaChunk(
      DataSource dataSource,
      DataSpec dataSpec,
      Format trackFormat,
      int trackSelectionReason,
      Object trackSelectionData,
      long startTimeUs,
      long endTimeUs,
      long chunkIndex,
      int trackType,
      Format sampleFormat) {
    super(
        dataSource,
        dataSpec,
        trackFormat,
        trackSelectionReason,
        trackSelectionData,
        startTimeUs,
        endTimeUs,
        /* clippedStartTimeUs= */ C.TIME_UNSET,
        /* clippedEndTimeUs= */ C.TIME_UNSET,
        chunkIndex);
    this.trackType = trackType;
    this.sampleFormat = sampleFormat;
  }


  @Override
  public boolean isLoadCompleted() {
    return loadCompleted;
  }

  // Loadable implementation.

  @Override
  public void cancelLoad() {
    // Do nothing.
  }

  @SuppressWarnings("NonAtomicVolatileUpdate")
  @Override
  public void load() throws IOException, InterruptedException {
    DataSpec loadDataSpec = dataSpec.subrange(nextLoadPosition);
    try {
      // Create and open the input.
      long length = dataSource.open(loadDataSpec);
      if (length != C.LENGTH_UNSET) {
        length += nextLoadPosition;
      }
      ExtractorInput extractorInput =
          new DefaultExtractorInput(dataSource, nextLoadPosition, length);
      BaseMediaChunkOutput output = getOutput();
      output.setSampleOffsetUs(0);
      TrackOutput trackOutput = output.track(0, trackType);
      trackOutput.format(sampleFormat);
      // Load the sample data.
      int result = 0;
      while (result != C.RESULT_END_OF_INPUT) {
        nextLoadPosition += result;
        result = trackOutput.sampleData(extractorInput, Integer.MAX_VALUE, true);
      }
      int sampleSize = (int) nextLoadPosition;
      trackOutput.sampleMetadata(startTimeUs, C.BUFFER_FLAG_KEY_FRAME, sampleSize, 0, null);
    } finally {
      Utils.closeQuietly(dataSource);
    }
    loadCompleted = true;
  }

}
