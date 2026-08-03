package minefarts.smarttube.source.chunk;

import minefarts.smarttube.C;
import minefarts.smarttube.Format;
import minefarts.smarttube.extractor.DefaultExtractorInput;
import minefarts.smarttube.extractor.Extractor;
import minefarts.smarttube.extractor.ExtractorInput;
import minefarts.smarttube.extractor.PositionHolder;
import minefarts.smarttube.upstream.DataSource;
import minefarts.smarttube.upstream.DataSpec;
import minefarts.smarttube.utils.Assertions;
import minefarts.smarttube.utils.Utils;
import java.io.IOException;

/**
 * A {@link BaseMediaChunk} that uses an {@link Extractor} to decode sample data.
 */
public class ContainerMediaChunk extends BaseMediaChunk {

  private static final PositionHolder DUMMY_POSITION_HOLDER = new PositionHolder();

  private final int chunkCount;
  private final long sampleOffsetUs;
  private final ChunkExtractorWrapper extractorWrapper;

  private long nextLoadPosition;
  private volatile boolean loadCanceled;
  private boolean loadCompleted;

  /**
   * @param dataSource The source from which the data should be loaded.
   * @param dataSpec Defines the data to be loaded.
   * @param trackFormat See {@link #trackFormat}.
   * @param trackSelectionReason See {@link #trackSelectionReason}.
   * @param trackSelectionData See {@link #trackSelectionData}.
   * @param startTimeUs The start time of the media contained by the chunk, in microseconds.
   * @param endTimeUs The end time of the media contained by the chunk, in microseconds.
   * @param clippedStartTimeUs The time in the chunk from which output will begin, or {@link
   *     C#TIME_UNSET} to output from the start of the chunk.
   * @param clippedEndTimeUs The time in the chunk from which output will end, or {@link
   *     C#TIME_UNSET} to output to the end of the chunk.
   * @param chunkIndex The index of the chunk, or {@link C#INDEX_UNSET} if it is not known.
   * @param chunkCount The number of chunks in the underlying media that are spanned by this
   *     instance. Normally equal to one, but may be larger if multiple chunks as defined by the
   *     underlying media are being merged into a single load.
   * @param sampleOffsetUs An offset to add to the sample timestamps parsed by the extractor.
   * @param extractorWrapper A wrapped extractor to use for parsing the data.
   */
  public ContainerMediaChunk(
      DataSource dataSource,
      DataSpec dataSpec,
      Format trackFormat,
      int trackSelectionReason,
      Object trackSelectionData,
      long startTimeUs,
      long endTimeUs,
      long clippedStartTimeUs,
      long clippedEndTimeUs,
      long chunkIndex,
      int chunkCount,
      long sampleOffsetUs,
      ChunkExtractorWrapper extractorWrapper) {
    super(
        dataSource,
        dataSpec,
        trackFormat,
        trackSelectionReason,
        trackSelectionData,
        startTimeUs,
        endTimeUs,
        clippedStartTimeUs,
        clippedEndTimeUs,
        chunkIndex);
    this.chunkCount = chunkCount;
    this.sampleOffsetUs = sampleOffsetUs;
    this.extractorWrapper = extractorWrapper;
  }

  @Override
  public long getNextChunkIndex() {
    return chunkIndex + chunkCount;
  }

  @Override
  public boolean isLoadCompleted() {
    return loadCompleted;
  }

  // Loadable implementation.

  @Override
  public final void cancelLoad() {
    loadCanceled = true;
  }

  @SuppressWarnings("NonAtomicVolatileUpdate")
  @Override
  public final void load() throws IOException, InterruptedException {
    DataSpec loadDataSpec = dataSpec.subrange(nextLoadPosition);
    try {
      // Create and open the input.
      ExtractorInput input = new DefaultExtractorInput(dataSource,
          loadDataSpec.absoluteStreamPosition, dataSource.open(loadDataSpec));
      if (nextLoadPosition == 0) {
        // Configure the output and set it as the target for the extractor wrapper.
        BaseMediaChunkOutput output = getOutput();
        output.setSampleOffsetUs(sampleOffsetUs);
        extractorWrapper.init(
            getTrackOutputProvider(output),
            clippedStartTimeUs == C.TIME_UNSET
                ? C.TIME_UNSET
                : (clippedStartTimeUs - sampleOffsetUs),
            clippedEndTimeUs == C.TIME_UNSET ? C.TIME_UNSET : (clippedEndTimeUs - sampleOffsetUs));
      }
      // Load and decode the sample data.
      try {
        Extractor extractor = extractorWrapper.extractor;
        int result = Extractor.RESULT_CONTINUE;
        while (result == Extractor.RESULT_CONTINUE && !loadCanceled) {
          result = extractor.read(input, DUMMY_POSITION_HOLDER);
        }
        Assertions.checkState(result != Extractor.RESULT_SEEK);
      } finally {
        nextLoadPosition = input.getPosition() - dataSpec.absoluteStreamPosition;
      }
    } finally {
      Utils.closeQuietly(dataSource);
    }
    loadCompleted = true;
  }

  /**
   * Returns the {@link ChunkExtractorWrapper.TrackOutputProvider} to be used by the wrapped
   * extractor.
   *
   * @param baseMediaChunkOutput The {@link BaseMediaChunkOutput} most recently passed to {@link
   *     #init(BaseMediaChunkOutput)}.
   * @return A {@link ChunkExtractorWrapper.TrackOutputProvider} to be used by the wrapped
   *     extractor.
   */
  protected ChunkExtractorWrapper.TrackOutputProvider getTrackOutputProvider(
      BaseMediaChunkOutput baseMediaChunkOutput) {
    return baseMediaChunkOutput;
  }
}
