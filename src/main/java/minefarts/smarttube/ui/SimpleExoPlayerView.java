package minefarts.smarttube.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import minefarts.smarttube.Player;
import minefarts.smarttube.ui.playback.SimpleExoPlayer;

/** @deprecated Use {@link PlayerView}. */
@Deprecated
public final class SimpleExoPlayerView extends PlayerView {

  public SimpleExoPlayerView(Context context) {
    super(context);
  }

  public SimpleExoPlayerView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SimpleExoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  /**
   * Switches the view targeted by a given {@link SimpleExoPlayer}.
   *
   * @param player The player whose target view is being switched.
   * @param oldPlayerView The old view to detach from the player.
   * @param newPlayerView The new view to attach to the player.
   * @deprecated Use {@link PlayerView#switchTargetView(Player, PlayerView, PlayerView)} instead.
   */
  @Deprecated
  @SuppressWarnings("deprecation")
  public static void switchTargetView(
      @NonNull SimpleExoPlayer player,
      @Nullable SimpleExoPlayerView oldPlayerView,
      @Nullable SimpleExoPlayerView newPlayerView) {
    PlayerView.switchTargetView(player, oldPlayerView, newPlayerView);
  }

}
