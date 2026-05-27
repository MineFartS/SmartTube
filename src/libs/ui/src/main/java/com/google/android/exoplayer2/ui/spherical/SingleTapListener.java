package minefarts.exoplayer2.ui.spherical;

import android.view.MotionEvent;

/** Listens tap events on a {@link android.view.View}. */
public interface SingleTapListener {
  /**
   * Notified when a tap occurs with the up {@link MotionEvent} that triggered it.
   *
   * @param e The up motion event that completed the first tap.
   * @return Whether the event is consumed.
   */
  boolean onSingleTapUp(MotionEvent e);
}
