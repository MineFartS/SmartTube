package SmartTubeApp.app.views;

import SmartTubeApp.app.models.playback.manager.PlayerManager;
import SmartTubeApp.app.models.playback.listener.PlayerEventListener;

public interface PlaybackView extends PlayerManager {
    void showProgressBar(boolean show);
}
