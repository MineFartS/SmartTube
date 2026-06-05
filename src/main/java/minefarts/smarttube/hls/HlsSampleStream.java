package minefarts.smarttube.hls;

import minefarts.smarttube.C;
import minefarts.smarttube.FormatHolder;
import minefarts.smarttube.decoder.DecoderInputBuffer;
import minefarts.smarttube.source.SampleStream;
import minefarts.smarttube.utils.Assertions;
import java.io.IOException;

/**
 * {@link SampleStream} for a particular sample queue in HLS.
 */
/* package */ final class HlsSampleStream implements SampleStream {

  private final int trackGroupIndex;
  private final HlsSampleStreamWrapper sampleStreamWrapper;
  private int sampleQueueIndex;

  public HlsSampleStream(HlsSampleStreamWrapper sampleStreamWrapper, int trackGroupIndex) {
    this.sampleStreamWrapper = sampleStreamWrapper;
    this.trackGroupIndex = trackGroupIndex;
    sampleQueueIndex = HlsSampleStreamWrapper.SAMPLE_QUEUE_INDEX_PENDING;
  }

  public void bindSampleQueue() {
    Assertions.checkArgument(sampleQueueIndex == HlsSampleStreamWrapper.SAMPLE_QUEUE_INDEX_PENDING);
    sampleQueueIndex = sampleStreamWrapper.bindSampleQueueToSampleStream(trackGroupIndex);
  }

  public void unbindSampleQueue() {
    if (sampleQueueIndex != HlsSampleStreamWrapper.SAMPLE_QUEUE_INDEX_PENDING) {
      sampleStreamWrapper.unbindSampleQueue(trackGroupIndex);
      sampleQueueIndex = HlsSampleStreamWrapper.SAMPLE_QUEUE_INDEX_PENDING;
    }
  }

  // SampleStream implementation.

  @Override
  public boolean isReady() {
    return sampleQueueIndex == HlsSampleStreamWrapper.SAMPLE_QUEUE_INDEX_NO_MAPPING_NON_FATAL
        || (hasValidSampleQueueIndex() && sampleStreamWrapper.isReady(sampleQueueIndex));
  }

  @Override
  public void maybeThrowError() throws IOException {
    if (sampleQueueIndex == HlsSampleStreamWrapper.SAMPLE_QUEUE_INDEX_NO_MAPPING_FATAL) {
      throw new SampleQueueMappingException(
          sampleStreamWrapper.getTrackGroups().get(trackGroupIndex).getFormat(0).sampleMimeType);
    }
    sampleStreamWrapper.maybeThrowError();
  }

  @Override
  public int readData(FormatHolder formatHolder, DecoderInputBuffer buffer, boolean requireFormat) {
    if (sampleQueueIndex == HlsSampleStreamWrapper.SAMPLE_QUEUE_INDEX_NO_MAPPING_NON_FATAL) {
      buffer.addFlag(C.BUFFER_FLAG_END_OF_STREAM);
      return C.RESULT_BUFFER_READ;
    }
    return hasValidSampleQueueIndex()
        ? sampleStreamWrapper.readData(sampleQueueIndex, formatHolder, buffer, requireFormat)
        : C.RESULT_NOTHING_READ;
  }

  @Override
  public int skipData(long positionUs) {
    return hasValidSampleQueueIndex()
        ? sampleStreamWrapper.skipData(sampleQueueIndex, positionUs)
        : 0;
  }

  // Internal methods.

  private boolean hasValidSampleQueueIndex() {
    return sampleQueueIndex != HlsSampleStreamWrapper.SAMPLE_QUEUE_INDEX_PENDING
        && sampleQueueIndex != HlsSampleStreamWrapper.SAMPLE_QUEUE_INDEX_NO_MAPPING_NON_FATAL
        && sampleQueueIndex != HlsSampleStreamWrapper.SAMPLE_QUEUE_INDEX_NO_MAPPING_FATAL;
  }
}
