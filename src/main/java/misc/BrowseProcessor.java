package SmartTubeApp.misc;

import SmartTubeApp.app.models.data.Video;
import SmartTubeApp.app.models.data.VideoGroup;

public interface BrowseProcessor {
    interface OnItemReady {
        void onItemReady(Video video);
    }
    void process(VideoGroup videoGroup);
    void dispose();
}
