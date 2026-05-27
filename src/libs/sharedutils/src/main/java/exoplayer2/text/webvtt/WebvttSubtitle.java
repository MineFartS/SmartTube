package minefarts.exoplayer2.text.webvtt;

import android.text.SpannableStringBuilder;
import minefarts.exoplayer2.C;
import minefarts.exoplayer2.text.Cue;
import minefarts.exoplayer2.text.Subtitle;
import minefarts.exoplayer2.util.Assertions;
import minefarts.exoplayer2.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A representation of a WebVTT subtitle.
 */
/* package */ final class WebvttSubtitle implements Subtitle {

  private static final long MAX_CUE_LENGTH_US = 10_000_000; // NOTE: lower values may broke some videos
  private final List<WebvttCue> cues;
  private final int numCues;
  private final long[] cueTimesUs;
  private final long[] sortedCueTimesUs;

  /**
   * @param cues A list of the cues in this subtitle.
   */
  public WebvttSubtitle(List<WebvttCue> cues) {
    this.cues = cues;
    numCues = cues.size();
    cueTimesUs = new long[2 * numCues];
    for (int cueIndex = 0; cueIndex < numCues; cueIndex++) {
      WebvttCue cue = cues.get(cueIndex);
      int arrayIndex = cueIndex * 2;
      long startTime = cue.startTime;
      long endTime = cue.endTime;
      long length = endTime - startTime;
      cueTimesUs[arrayIndex] = startTime;
      // MOD: fix long lasting subs
      //cueTimesUs[arrayIndex + 1] = cue.endTime;
      cueTimesUs[arrayIndex + 1] = length <= MAX_CUE_LENGTH_US ? endTime : startTime + MAX_CUE_LENGTH_US;
    }
    sortedCueTimesUs = Arrays.copyOf(cueTimesUs, cueTimesUs.length);
    Arrays.sort(sortedCueTimesUs);
  }

  @Override
  public int getNextEventTimeIndex(long timeUs) {
    int index = Util.binarySearchCeil(sortedCueTimesUs, timeUs, false, false);
    return index < sortedCueTimesUs.length ? index : C.INDEX_UNSET;
  }

  @Override
  public int getEventTimeCount() {
    return sortedCueTimesUs.length;
  }

  @Override
  public long getEventTime(int index) {
    Assertions.checkArgument(index >= 0);
    Assertions.checkArgument(index < sortedCueTimesUs.length);
    return sortedCueTimesUs[index];
  }

  @Override
  public List<Cue> getCues(long timeUs) {
    ArrayList<Cue> list = null;
    WebvttCue firstNormalCue = null;
    SpannableStringBuilder normalCueTextBuilder = null;

    for (int i = 0; i < numCues; i++) {
      if ((cueTimesUs[i * 2] <= timeUs) && (timeUs < cueTimesUs[i * 2 + 1])) {
        if (list == null) {
          list = new ArrayList<>();
        }
        WebvttCue cue = cues.get(i);
        if (cue.isNormalCue()) {
          // we want to merge all of the normal cues into a single cue to ensure they are drawn
          // correctly (i.e. don't overlap) and to emulate roll-up, but only if there are multiple
          // normal cues, otherwise we can just append the single normal cue
          if (firstNormalCue == null) {
            firstNormalCue = cue;
          } else if (normalCueTextBuilder == null) {
            normalCueTextBuilder = new SpannableStringBuilder();
            normalCueTextBuilder.append(firstNormalCue.text).append("\n").append(cue.text);
          } else {
            normalCueTextBuilder.append("\n").append(cue.text);
          }
        } else {
          list.add(cue);
        }
      }
    }
    if (normalCueTextBuilder != null) {
      // there were multiple normal cues, so create a new cue with all of the text
      list.add(new WebvttCue(normalCueTextBuilder));
    } else if (firstNormalCue != null) {
      // there was only a single normal cue, so just add it to the list
      list.add(firstNormalCue);
    }

    if (list != null) {
      return list;
    } else {
      return Collections.emptyList();
    }
  }

}
