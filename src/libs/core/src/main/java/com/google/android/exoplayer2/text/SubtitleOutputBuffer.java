package com.google.android.exoplayer2.text;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.decoder.OutputBuffer;
import com.google.android.exoplayer2.util.Assertions;
import java.util.List;

/**
 * Base class for {@link SubtitleDecoder} output buffers.
 */
public abstract class SubtitleOutputBuffer extends OutputBuffer implements Subtitle {

  private Subtitle subtitle;
  private long subsampleOffsetUs;

  /**
   * Sets the content of the output buffer, consisting of a {@link Subtitle} and associated
   * metadata.
   *
   * @param timeUs The time of the start of the subtitle in microseconds.
   * @param subtitle The subtitle.
   * @param subsampleOffsetUs An offset that must be added to the subtitle's event times, or
   *     {@link Format#OFFSET_SAMPLE_RELATIVE} if {@code timeUs} should be added.
   */
  public void setContent(long timeUs, Subtitle subtitle, long subsampleOffsetUs) {
    this.timeUs = timeUs;
    this.subtitle = subtitle;
    this.subsampleOffsetUs = subsampleOffsetUs == Format.OFFSET_SAMPLE_RELATIVE ? this.timeUs
        : subsampleOffsetUs;
  }

  @Override
  public int getEventTimeCount() {
    return Assertions.checkNotNull(subtitle).getEventTimeCount();
  }

  @Override
  public long getEventTime(int index) {
    return Assertions.checkNotNull(subtitle).getEventTime(index) + subsampleOffsetUs;
  }

  @Override
  public int getNextEventTimeIndex(long timeUs) {
    return Assertions.checkNotNull(subtitle).getNextEventTimeIndex(timeUs - subsampleOffsetUs);
  }

  @Override
  public List<Cue> getCues(long timeUs) {
    return Assertions.checkNotNull(subtitle).getCues(timeUs - subsampleOffsetUs);
  }

  @Override
  public abstract void release();

  @Override
  public void clear() {
    super.clear();
    subtitle = null;
  }

}
