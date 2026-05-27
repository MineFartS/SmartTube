package com.google.android.exoplayer2;

import com.google.android.exoplayer2.Player.RepeatMode;

/**
 * Default {@link ControlDispatcher} that dispatches all operations to the player without
 * modification.
 */
public class DefaultControlDispatcher implements ControlDispatcher {

  @Override
  public boolean dispatchSetPlayWhenReady(Player player, boolean playWhenReady) {
    player.setPlayWhenReady(playWhenReady);
    return true;
  }

  @Override
  public boolean dispatchSeekTo(Player player, int windowIndex, long positionMs) {
    player.seekTo(windowIndex, positionMs);
    return true;
  }

  @Override
  public boolean dispatchSetRepeatMode(Player player, @RepeatMode int repeatMode) {
    player.setRepeatMode(repeatMode);
    return true;
  }

  @Override
  public boolean dispatchSetShuffleModeEnabled(Player player, boolean shuffleModeEnabled) {
    player.setShuffleModeEnabled(shuffleModeEnabled);
    return true;
  }

  @Override
  public boolean dispatchStop(Player player, boolean reset) {
    player.stop(reset);
    return true;
  }
}
