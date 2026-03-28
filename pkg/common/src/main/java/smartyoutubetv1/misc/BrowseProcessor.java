package smartyoutubetv1.misc;

import smartyoutubetv1.app.models.data.Video;
import smartyoutubetv1.app.models.data.VideoGroup;

public interface BrowseProcessor {
    interface OnItemReady {
        void onItemReady(Video video);
    }
    void process(VideoGroup videoGroup);
    void dispose();
}
