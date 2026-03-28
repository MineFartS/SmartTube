package smartyoutubetv1.app.models.playback.listener;

import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;
import smartyoutubetv1.app.models.data.Video;
import smartyoutubetv1.app.models.playback.manager.PlayerManager;
import smartyoutubetv1.misc.TickleManager.TickleListener;

public interface PlayerEventListener extends PlayerUiEventListener, PlayerEngineEventListener, ViewEventListener, TickleListener {
    void onNewVideo(Video item);
    void onMetadata(MediaItemMetadata metadata);
    /**
     * Called after creation of {@link PlayerManager}
     */
    void onInit();
    void onFinish();
}
