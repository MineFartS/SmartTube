package smartyoutubetv1.app.views;

import smartyoutubetv1.app.models.playback.ui.OptionCategory;

import java.util.List;

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
