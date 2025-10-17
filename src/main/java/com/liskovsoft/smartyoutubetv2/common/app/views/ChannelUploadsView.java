package com.liskovsoft.smartyoutubetv2.common.app.views;

/**
 * View used to display channel uploads / playlist rows and progress.
 * Used by ChannelUploadsPresenter to present and continue playlists.
 */
public interface ChannelUploadsView {
    void update(VideoGroup videoGroup);
    void showProgressBar(boolean show);
    void clear();
}
