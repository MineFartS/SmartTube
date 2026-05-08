package SmartTubeApp.app.models.playback.listener;

import com.liskovsoft.sharedutils.data.MediaItemMetadata;
import SmartTubeApp.app.models.data.Video;
import SmartTubeApp.misc.TickleManager.TickleListener;

public interface PlayerEventListener extends PlayerUiEventListener, PlayerEngineEventListener, ViewEventListener, TickleListener {
    void onNewVideo(Video item);
    void onMetadata(MediaItemMetadata metadata);
    /**
     * Called after creation of {@link PlayerManager}
     */
    void onInit();
    void onFinish();
}
