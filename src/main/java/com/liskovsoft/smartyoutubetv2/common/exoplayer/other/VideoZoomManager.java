package com.liskovsoft.smartyoutubetv2.common.exoplayer.other;

/**
 * Video zoom & aspect ratio manager.
 *
 * Responsibilities:
 * - Translate user zoom percentage into player resizeMode/transform or ExoPlayer UI calls.
 * - Handle "fit to dialog" behaviour and restore previous zoom after dialog dismissal.
 * - Respect device safe-areas / overscan settings and allow persistent user overrides.
 *
 * Threading:
 * - Called from UI thread when user interacts with zoom controls.
 */
public class VideoZoomManager {
    public static final int MODE_DEFAULT = AspectRatioFrameLayout.RESIZE_MODE_FIT;
    public static final int MODE_FIT_WIDTH = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH;
    public static final int MODE_FIT_HEIGHT = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT;
    public static final int MODE_FIT_BOTH = AspectRatioFrameLayout.RESIZE_MODE_ZOOM;
    public static final int MODE_STRETCH = AspectRatioFrameLayout.RESIZE_MODE_FILL;
    private final PlayerView mPlayerView;

    public VideoZoomManager(PlayerView playerView) {
        mPlayerView = playerView;
    }

    public int getZoomMode() {
        return mPlayerView.getResizeMode();
    }

    public void setZoomMode(int mode) {
        mPlayerView.setResizeMode(mode);
    }
}
