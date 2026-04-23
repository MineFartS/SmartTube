package SmartTubeApp.ui.browse.interfaces;

import SmartTubeApp.app.models.data.Video;
import SmartTubeApp.app.models.data.VideoGroup;

public interface VideoSection extends Section {
    void update(VideoGroup group);
    int getPosition();
    void setPosition(int index);
    void selectItem(Video item);
}
