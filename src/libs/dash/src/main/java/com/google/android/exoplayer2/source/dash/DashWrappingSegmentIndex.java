package minefarts.exoplayer2.source.dash;

import minefarts.exoplayer2.extractor.ChunkIndex;
import minefarts.exoplayer2.source.dash.manifest.RangedUri;

/**
 * An implementation of {@link DashSegmentIndex} that wraps a {@link ChunkIndex} parsed from a
 * media stream.
 */
public final class DashWrappingSegmentIndex implements DashSegmentIndex {

  private final ChunkIndex chunkIndex;
  private final long timeOffsetUs;

  /**
   * @param chunkIndex The {@link ChunkIndex} to wrap.
   * @param timeOffsetUs An offset to subtract from the times in the wrapped index, in microseconds.
   */
  public DashWrappingSegmentIndex(ChunkIndex chunkIndex, long timeOffsetUs) {
    this.chunkIndex = chunkIndex;
    this.timeOffsetUs = timeOffsetUs;
  }

  @Override
  public long getFirstSegmentNum() {
    return 0;
  }

  @Override
  public int getSegmentCount(long periodDurationUs) {
    return chunkIndex.length;
  }

  @Override
  public long getTimeUs(long segmentNum) {
    return chunkIndex.timesUs[(int) segmentNum] - timeOffsetUs;
  }

  @Override
  public long getDurationUs(long segmentNum, long periodDurationUs) {
    return chunkIndex.durationsUs[(int) segmentNum];
  }

  @Override
  public RangedUri getSegmentUrl(long segmentNum) {
    return new RangedUri(
        null, chunkIndex.offsets[(int) segmentNum], chunkIndex.sizes[(int) segmentNum]);
  }

  @Override
  public long getSegmentNum(long timeUs, long periodDurationUs) {
    return chunkIndex.getChunkIndex(timeUs + timeOffsetUs);
  }

  @Override
  public boolean isExplicit() {
    return true;
  }

}
