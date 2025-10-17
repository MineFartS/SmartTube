package com.liskovsoft.smartyoutubetv2.common.app.views;

import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionCategory;

import java.util.List;

/**
 * Dialog view interface used by AppDialogPresenter.
 * - show(...) displays categories with dialog-level flags
 * - finish/goBack/clearBackstack control dialog lifecycle
 * - isShown/isTransparent/isOverlay/isPaused provide state checks
 */
public interface AppDialogView {
    void show(List<OptionCategory> categories, String title, boolean isExpandable, boolean isTransparent, boolean isOverlay, int id);
    void finish();
    void goBack();
    void clearBackstack();
    boolean isShown();
    boolean isTransparent();
    boolean isOverlay();
    boolean isPaused();
    int getViewId();
}
