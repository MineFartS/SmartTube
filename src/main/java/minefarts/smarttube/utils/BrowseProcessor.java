package minefarts.smarttube.utils;

import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.data.VideoGroup;

public interface BrowseProcessor {
    interface OnItemReady {
        void onItemReady(Video video);
    }
    void process(VideoGroup videoGroup);
    void dispose();
}
