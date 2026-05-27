package minefarts.exoplayer2.text.pgs;

import minefarts.exoplayer2.C;
import minefarts.exoplayer2.text.Cue;
import minefarts.exoplayer2.text.Subtitle;
import java.util.List;

/** A representation of a PGS subtitle. */
/* package */ final class PgsSubtitle implements Subtitle {

  private final List<Cue> cues;

  public PgsSubtitle(List<Cue> cues) {
    this.cues = cues;
  }

  @Override
  public int getNextEventTimeIndex(long timeUs) {
    return C.INDEX_UNSET;
  }

  @Override
  public int getEventTimeCount() {
    return 1;
  }

  @Override
  public long getEventTime(int index) {
    return 0;
  }

  @Override
  public List<Cue> getCues(long timeUs) {
    return cues;
  }
}
