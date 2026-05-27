package minefarts.smarttube.text.webvtt;

import minefarts.smarttube.C;
import minefarts.smarttube.text.Cue;
import minefarts.smarttube.text.Subtitle;
import minefarts.smarttube.utils.Assertions;
import java.util.Collections;
import java.util.List;

/**
 * Representation of a Webvtt subtitle embedded in a MP4 container file.
 */
/* package */ final class Mp4WebvttSubtitle implements Subtitle {

  private final List<Cue> cues;

  public Mp4WebvttSubtitle(List<Cue> cueList) {
    cues = Collections.unmodifiableList(cueList);
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
