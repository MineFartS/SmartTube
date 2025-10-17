package com.liskovsoft.smartyoutubetv2.common.app.views;

/**
 * Player view interface exposing UI controls and state used by PlaybackPresenter and controllers.
 * Methods include setVideo, showOverlay, updateSuggestions and button state setters.
 */
public interface PlaybackView extends PlayerManager {
    void showProgressBar(boolean show);
}
