package minefarts.smarttube.ui;

import android.content.Context;
import android.util.AttributeSet;

/** @deprecated Use {@link PlayerControlView}. */
@Deprecated
public class PlaybackControlView extends PlayerControlView {

  /** @deprecated Use {@link minefarts.smarttube.ControlDispatcher}. */
  @Deprecated
  public interface ControlDispatcher extends minefarts.smarttube.ControlDispatcher {}

  @Deprecated
  @SuppressWarnings("deprecation")
  private static final class DefaultControlDispatcher
      extends minefarts.smarttube.DefaultControlDispatcher implements ControlDispatcher {}
  /** @deprecated Use {@link minefarts.smarttube.DefaultControlDispatcher}. */
  @Deprecated
  @SuppressWarnings("deprecation")
  public static final ControlDispatcher DEFAULT_CONTROL_DISPATCHER = new DefaultControlDispatcher();

  public PlaybackControlView(Context context) {
    super(context);
  }

  public PlaybackControlView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public PlaybackControlView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public PlaybackControlView(
      Context context, AttributeSet attrs, int defStyleAttr, AttributeSet playbackAttrs) {
    super(context, attrs, defStyleAttr, playbackAttrs);
  }
}
