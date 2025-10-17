package com.liskovsoft.smartyoutubetv2.common.app.models.playback.listener;

/** Lifecycle callbacks for views used by controllers. */
public interface ViewEventListener {
    void onViewCreated();
    void onViewDestroyed();
    void onViewPaused();
    void onViewResumed();
}
