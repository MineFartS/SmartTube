package SmartTubeApp.app.models.playback.listener;

public interface ViewEventListener {
    void onViewCreated();
    void onViewDestroyed();
    void onViewPaused();
    void onViewResumed();
}
