
package com.google.android.exoplayer2.text.cea;

import androidx.annotation.NonNull;
import android.text.Layout.Alignment;
import com.google.android.exoplayer2.text.Cue;

/**
 * A {@link Cue} for CEA-708.
 */
/* package */ final class Cea708Cue extends Cue implements Comparable<Cea708Cue> {

  /**
   * An unset priority.
   */
  public static final int PRIORITY_UNSET = -1;

  /**
   * The priority of the cue box.
   */
  public final int priority;

  /**
   * @param text See {@link #text}.
   * @param textAlignment See {@link #textAlignment}.
   * @param line See {@link #line}.
   * @param lineType See {@link #lineType}.
   * @param lineAnchor See {@link #lineAnchor}.
   * @param position See {@link #position}.
   * @param positionAnchor See {@link #positionAnchor}.
   * @param size See {@link #size}.
   * @param windowColorSet See {@link #windowColorSet}.
   * @param windowColor See {@link #windowColor}.
   * @param priority See (@link #priority}.
   */
  public Cea708Cue(CharSequence text, Alignment textAlignment, float line, @LineType int lineType,
      @AnchorType int lineAnchor, float position, @AnchorType int positionAnchor, float size,
      boolean windowColorSet, int windowColor, int priority) {
    super(text, textAlignment, line, lineType, lineAnchor, position, positionAnchor, size,
        windowColorSet, windowColor);
    this.priority = priority;
  }

  @Override
  public int compareTo(@NonNull Cea708Cue other) {
    if (other.priority < priority) {
      return -1;
    } else if (other.priority > priority) {
      return 1;
    }
    return 0;
  }

}
