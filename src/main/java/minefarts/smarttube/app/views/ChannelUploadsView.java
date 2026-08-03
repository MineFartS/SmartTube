package minefarts.smarttube.app.views;

import minefarts.smarttube.app.models.data.VideoGroup;

public interface ChannelUploadsView {
    void update(VideoGroup videoGroup);
    void showProgressBar(boolean show);
    void clear();
}
