package com.liskovsoft.smartyoutubetv2.common.app.presenters.interfaces;

/** Presenter contract for components that display/manipulate VideoGroup lists (click/scroll callbacks). */
public interface VideoGroupPresenter {
    void onVideoItemSelected(Video item);
    void onVideoItemClicked(Video item);
    void onVideoItemLongClicked(Video item);
    void onScrollEnd(Video item);
    boolean hasPendingActions();
}
