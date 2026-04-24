package SmartTubeApp.app.views;

import SmartTubeApp.app.models.data.VideoGroup;

public interface ChannelUploadsView {
    void update(VideoGroup videoGroup);
    void showProgressBar(boolean show);
    void clear();
}
