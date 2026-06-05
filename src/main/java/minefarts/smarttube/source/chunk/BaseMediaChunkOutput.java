package minefarts.smarttube.source.chunk;

import minefarts.smarttube.extractor.DummyTrackOutput;
import minefarts.smarttube.extractor.TrackOutput;
import minefarts.smarttube.source.SampleQueue;
import minefarts.smarttube.source.chunk.ChunkExtractorWrapper.TrackOutputProvider;
import minefarts.smarttube.utils.mylogger.Log;

/** An output for {@link BaseMediaChunk}s. */
public final class BaseMediaChunkOutput implements TrackOutputProvider {

  private static final String TAG = "BaseMediaChunkOutput";

  private final int[] trackTypes;
  private final SampleQueue[] sampleQueues;

  /**
   * @param trackTypes The track types of the individual track outputs.
   * @param sampleQueues The individual sample queues.
   */
  public BaseMediaChunkOutput(int[] trackTypes, SampleQueue[] sampleQueues) {
    this.trackTypes = trackTypes;
    this.sampleQueues = sampleQueues;
  }

  @Override
  public TrackOutput track(int id, int type) {
    for (int i = 0; i < trackTypes.length; i++) {
      if (type == trackTypes[i]) {
        return sampleQueues[i];
      }
    }
    Log.e(TAG, "Unmatched track of type: " + type);
    return new DummyTrackOutput();
  }

  /**
   * Returns the current absolute write indices of the individual sample queues.
   */
  public int[] getWriteIndices() {
    int[] writeIndices = new int[sampleQueues.length];
    for (int i = 0; i < sampleQueues.length; i++) {
      if (sampleQueues[i] != null) {
        writeIndices[i] = sampleQueues[i].getWriteIndex();
      }
    }
    return writeIndices;
  }

  /**
   * Sets an offset that will be added to the timestamps (and sub-sample timestamps) of samples
   * subsequently written to the sample queues.
   */
  public void setSampleOffsetUs(long sampleOffsetUs) {
    for (SampleQueue sampleQueue : sampleQueues) {
      if (sampleQueue != null) {
        sampleQueue.setSampleOffsetUs(sampleOffsetUs);
      }
    }
  }

}
