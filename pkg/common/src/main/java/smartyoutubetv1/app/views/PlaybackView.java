package smartyoutubetv1.app.views;

import smartyoutubetv1.app.models.playback.manager.PlayerManager;
import smartyoutubetv1.app.models.playback.listener.PlayerEventListener;

public interface PlaybackView extends PlayerManager {
    void showProgressBar(boolean show);
}
