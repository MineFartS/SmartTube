package SmartTubeApp.app.presenters.interfaces;

import SmartTubeApp.app.models.data.Video;

public interface VideoGroupPresenter {
    void onVideoItemSelected(Video item);
    void onVideoItemClicked(Video item);
    void onVideoItemLongClicked(Video item);
    void onScrollEnd(Video item);
    boolean hasPendingActions();
}
