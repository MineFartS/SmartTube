
package com.google.android.exoplayer2.source.dash.manifest;

import com.google.android.exoplayer2.source.dash.DashSegmentIndex;

/**
 * A {@link DashSegmentIndex} that defines a single segment.
 */
/* package */ final class SingleSegmentIndex implements DashSegmentIndex {

  private final RangedUri uri;

  /**
   * @param uri A {@link RangedUri} defining the location of the segment data.
   */
  public SingleSegmentIndex(RangedUri uri) {
    this.uri = uri;
  }

  @Override
  public long getSegmentNum(long timeUs, long periodDurationUs) {
    return 0;
  }

  @Override
  public long getTimeUs(long segmentNum) {
    return 0;
  }

  @Override
  public long getDurationUs(long segmentNum, long periodDurationUs) {
    return periodDurationUs;
  }

  @Override
  public RangedUri getSegmentUrl(long segmentNum) {
    return uri;
  }

  @Override
  public long getFirstSegmentNum() {
    return 0;
  }

  @Override
  public int getSegmentCount(long periodDurationUs) {
    return 1;
  }

  @Override
  public boolean isExplicit() {
    return true;
  }

}
