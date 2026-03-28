package smartyoutubetv1.app.presenters.interfaces;

import smartyoutubetv1.app.models.data.Video;

public interface VideoGroupPresenter {
    void onVideoItemSelected(Video item);
    void onVideoItemClicked(Video item);
    void onVideoItemLongClicked(Video item);
    void onScrollEnd(Video item);
    boolean hasPendingActions();
}
