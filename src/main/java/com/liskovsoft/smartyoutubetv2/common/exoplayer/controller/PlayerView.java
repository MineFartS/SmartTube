package com.liskovsoft.smartyoutubetv2.common.exoplayer.controller;

/**
 * UI contract for the player view used by ExoPlayerController / PlaybackPresenter.
 *
 * Expected behaviour:
 * - Provide methods to set video metadata, show/hide progress and overlays, apply track changes,
 *   handle user input, and expose debugging info.
 * - Responsible for wiring surface/texture view to ExoPlayer and forwarding surface lifecycle events.
 *
 * Threading:
 * - Most view operations should be performed on the main/UI thread.
 */
public interface PlayerView {
    void setQualityInfo(String info);
    void setVideo(Video video);
}
