package com.liskovsoft.smartyoutubetv2.common.app.views;

/**
 * View for channel page: shows channel rows, playlists and supports continuation/loading events.
 */
public interface ChannelView {
    void update(VideoGroup videoGroup);
    void setPosition(int index);
    void showProgressBar(boolean show);
    void clear();
}
