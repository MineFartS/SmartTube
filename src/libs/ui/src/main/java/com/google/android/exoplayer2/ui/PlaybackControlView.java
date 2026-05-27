package com.google.android.exoplayer2.ui;

import android.content.Context;
import android.util.AttributeSet;

/** @deprecated Use {@link PlayerControlView}. */
@Deprecated
public class PlaybackControlView extends PlayerControlView {

  /** @deprecated Use {@link com.google.android.exoplayer2.ControlDispatcher}. */
  @Deprecated
  public interface ControlDispatcher extends com.google.android.exoplayer2.ControlDispatcher {}

  @Deprecated
  @SuppressWarnings("deprecation")
  private static final class DefaultControlDispatcher
      extends com.google.android.exoplayer2.DefaultControlDispatcher implements ControlDispatcher {}
  /** @deprecated Use {@link com.google.android.exoplayer2.DefaultControlDispatcher}. */
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
