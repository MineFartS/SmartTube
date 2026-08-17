package minefarts.smarttube.app.views;

import com.liskovsoft.smartyoutubetv2.common.app.models.data.VideoGroup;

public interface ChannelView {
    void update(VideoGroup videoGroup);
    void setPosition(int index);
    void showProgressBar(boolean show);
    void clear();
}
