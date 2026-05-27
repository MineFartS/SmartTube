package minefarts.exoplayer2.text.cea;

import minefarts.exoplayer2.C;
import minefarts.exoplayer2.text.Cue;
import minefarts.exoplayer2.text.Subtitle;
import minefarts.exoplayer2.util.Assertions;
import java.util.Collections;
import java.util.List;

/**
 * A representation of a CEA subtitle.
 */
/* package */ final class CeaSubtitle implements Subtitle {

  private final List<Cue> cues;

  /**
   * @param cues The subtitle cues.
   */
  public CeaSubtitle(List<Cue> cues) {
    this.cues = cues;
  }

  @Override
  public int getNextEventTimeIndex(long timeUs) {
    return timeUs < 0 ? 0 : C.INDEX_UNSET;
  }

  @Override
  public int getEventTimeCount() {
    return 1;
  }

  @Override
  public long getEventTime(int index) {
    Assertions.checkArgument(index == 0);
    return 0;
  }

  @Override
  public List<Cue> getCues(long timeUs) {
    return timeUs >= 0 ? cues : Collections.emptyList();
  }

}
