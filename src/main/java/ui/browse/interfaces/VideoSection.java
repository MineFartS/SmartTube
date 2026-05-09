package minefarts.smarttube.ui.browse.interfaces;

import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.data.VideoGroup;

public interface VideoSection extends Section {
    void update(VideoGroup group);
    int getPosition();
    void setPosition(int index);
    void selectItem(Video item);
}
