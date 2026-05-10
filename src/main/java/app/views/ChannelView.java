package minefarts.smarttube.app.views;

import minefarts.smarttube.app.models.data.VideoGroup;

public interface ChannelView {
    void update(VideoGroup videoGroup);
    void setPosition(int index);
    void showProgressBar(boolean show);
    void clear();
}
