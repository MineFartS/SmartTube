package minefarts.smarttube.app.models.playback;

import minefarts.exoplayer2.ExoPlaybackException;

import minefarts.sharedutils.service.data.MediaItemMetadata;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.misc.TickleManager.TickleListener;
import minefarts.smarttube.exoplayer.selector.FormatItem;
import minefarts.smarttube.exoplayer.selector.TrackSelectorManager;

public interface PlayerEventListener extends TickleListener {

    void onViewCreated();
    void onViewDestroyed();
    void onViewPaused();
    void onViewResumed();
    
    void onSuggestionItemClicked(Video item);
    void onSuggestionItemLongClicked(Video item);
    void onScrollEnd(Video item);
    boolean onPreviousClicked();
    boolean onNextClicked();
    void onPlayClicked();
    void onPauseClicked();
    boolean onKeyDown(int keyCode);
    void onButtonClicked(int buttonId, int buttonState);
    void onButtonLongClicked(int buttonId, int buttonState);
    void onControlsShown(boolean shown);

    void onNewVideo(Video item);
    void onMetadata(MediaItemMetadata metadata);
    
    /**
     * Called after creation of {@link PlayerManager}
     */
    void onInit();
    void onFinish();

    int ERROR_TYPE_SOURCE = ExoPlaybackException.TYPE_SOURCE;
    int ERROR_TYPE_RENDERER = ExoPlaybackException.TYPE_RENDERER;
    int ERROR_TYPE_UNEXPECTED = ExoPlaybackException.TYPE_UNEXPECTED;
    int ERROR_TYPE_REMOTE = ExoPlaybackException.TYPE_REMOTE;
    int ERROR_TYPE_OUT_OF_MEMORY = ExoPlaybackException.TYPE_OUT_OF_MEMORY;
    int RENDERER_INDEX_UNKNOWN = TrackSelectorManager.RENDERER_INDEX_UNKNOWN;
    int RENDERER_INDEX_VIDEO = TrackSelectorManager.RENDERER_INDEX_VIDEO;
    int RENDERER_INDEX_AUDIO = TrackSelectorManager.RENDERER_INDEX_AUDIO;
    int RENDERER_INDEX_SUBTITLE = TrackSelectorManager.RENDERER_INDEX_SUBTITLE;

    void onPlay();
    void onPause();
    void onPlayEnd();
    void onBuffering();
    void onSeekEnd();
    void onSeekPositionChanged(long positionMs);
    void onSpeedChanged(float speed);
    void onSourceChanged(Video item);
    void onVideoLoaded(Video item);
    void onEngineInitialized();
    void onEngineReleased();
    void onEngineError(int type, int rendererIndex, Throwable error);
    void onTrackChanged(FormatItem track);
    void onTrackSelected(FormatItem track);

}
