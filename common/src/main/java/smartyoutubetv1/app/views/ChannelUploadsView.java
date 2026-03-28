package smartyoutubetv1.app.views;

import smartyoutubetv1.app.models.data.VideoGroup;

public interface ChannelUploadsView {
    void update(VideoGroup videoGroup);
    void showProgressBar(boolean show);
    void clear();
}
