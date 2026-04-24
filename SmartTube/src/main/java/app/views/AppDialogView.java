package SmartTubeApp.app.views;

import SmartTubeApp.app.models.playback.ui.OptionCategory;

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
