package SmartTubeApp.ui.browse.interfaces;

import smartyoutubetv1.app.models.data.Video;
import smartyoutubetv1.app.models.data.VideoGroup;

public interface VideoSection extends Section {
    void update(VideoGroup group);
    int getPosition();
    void setPosition(int index);
    void selectItem(Video item);
}
