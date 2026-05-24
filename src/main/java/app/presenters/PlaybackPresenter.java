package minefarts.smarttube.app.presenters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import com.liskovsoft.sharedutils.data.MediaItemMetadata;
import minefarts.smarttube.app.models.data.Queue;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.playback.BasePlayerController;
import minefarts.smarttube.app.models.playback.controllers.ChatController;
import minefarts.smarttube.app.models.playback.controllers.CommentsController;
import minefarts.smarttube.app.models.playback.controllers.ContentBlockController;
import minefarts.smarttube.app.models.playback.controllers.PlayerUIController;
import minefarts.smarttube.app.models.playback.controllers.RemoteController;
import minefarts.smarttube.app.models.playback.controllers.SuggestionsController;
import minefarts.smarttube.app.models.playback.controllers.VideoLoaderController;
import minefarts.smarttube.app.models.playback.controllers.VideoStateController;
import minefarts.smarttube.app.models.playback.PlayerEventListener;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.app.presenters.dialogs.menu.VideoMenuPresenter;
import minefarts.smarttube.app.models.playback.PlayerEngine;
import minefarts.smarttube.exoplayer.selector.FormatItem;
import minefarts.smarttube.utils.Utils;
import com.liskovsoft.googlecommon.common.helpers.ServiceHelper;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlaybackPresenter extends BasePresenter<PlayerEngine> implements PlayerEventListener {
    
    @SuppressLint("StaticFieldLeak")
    private static PlaybackPresenter sInstance;
    
    private final List<PlayerEventListener> mEventListeners = new CopyOnWriteArrayList<PlayerEventListener>() {
        @Override
        public boolean add(PlayerEventListener listener) {
            ((BasePlayerController) listener).setMainController(PlaybackPresenter.this);

            return super.add(listener);
        }
    };
    private WeakReference<Video> mVideo;
    // Fix for using destroyed view
    private WeakReference<PlayerEngine> mPlayer = new WeakReference<>(null);
    private boolean mIsEmbedPlayerStarted;

    private PlaybackPresenter(Context context) {
        super(context);

        // NOTE: position matters!!!
        mEventListeners.add(new VideoStateController());
        mEventListeners.add(new SuggestionsController());
        mEventListeners.add(new PlayerUIController());
        mEventListeners.add(new VideoLoaderController(this));
        mEventListeners.add(new RemoteController(context));
        mEventListeners.add(new ContentBlockController());
        mEventListeners.add(new ChatController());
        mEventListeners.add(new CommentsController());
    }

    public static PlaybackPresenter instance(Context context) {
        if (sInstance == null) {
            sInstance = new PlaybackPresenter(context);
        }

        sInstance.setContext(context);

        return sInstance;
    }

    @Override
    public void onViewInitialized() {
        super.onViewInitialized();
        
        mEventListeners.forEach(PlayerEventListener::onInit);
    }


    public void openVideo(String videoId) {
        openVideo(videoId, false, -1);
    }

    /**
     * Opens video item from splash view
     */
    public void openVideo(String videoId, boolean finishOnEnded, long timeMs) {
        if (videoId == null) {
            return;
        }

        Video video = Video.from(videoId);
        video.finishOnEnded = finishOnEnded;
        video.pendingPosMs = timeMs;

        openVideo(video);
    }

    public void openVideo(Video video) {
        if (video == null) {
            return;
        }

        if (getView() != null && getView().isEmbed()) { // switching from the embed player to the fullscreen one
            // The embed player doesn't disposed properly
            // NOTE: don't release after init check because this depends on timings
            getView().finishReally();
            setView(null);
            //getController(VideoStateController.class).saveState();
        }

        onNewVideo(video);

        getViewManager().startView(PlayerEngine.class);
        mIsEmbedPlayerStarted = false;
    }

    public Video getVideo() {
        return mVideo != null ? mVideo.get() : null;
    }

    public boolean isRunningInBackground() {
        return getView() != null &&
                getView().isEngineBlocked() &&
                getView().isEngineInitialized() &&
                !getViewManager().isPlayerInForeground() &&
                getContext() instanceof Activity && Utils.checkActivity((Activity) getContext()); // Check that activity is not in Finishing state
    }

    public Boolean isOverlayShown() {
        return getView() != null && getView().isOverlayShown();
    }

    public boolean isPlaying() {
        return getView() != null && getView().isPlaying();
    }

    public Boolean isEngineBlocked() {
        return getView() != null && getView().isEngineBlocked();
    }

    public Boolean isEngineInitialized() {
        return getView() != null && getView().isEngineInitialized();
    }

    public void forceFinish() {
        if (getView() != null) {
            getView().finishReally();
        }
    }

    public void setPosition(String timeCode) {
        setPosition(ServiceHelper.timeTextToMillis(timeCode));
    }

    public void setPosition(long positionMs) {

        if (getViewManager().isPlayerInForeground() && getView() != null) {
            getView().setPositionMs(positionMs);
            getView().setPlayWhenReady(true);
            getView().showOverlay(false);
        } else {
            Video video = VideoMenuPresenter.sVideoHolder.get();
            if (video != null) {
                video.pendingPosMs = positionMs;
                openVideo(video);
            }
        }
    }

    // Controller methods

    @Override
    public void setView(PlayerEngine view) {
        super.setView(view);
        mPlayer = new WeakReference<>(view);

        // Fix playing the previous video when switching between embed and fullscreen players.
        // E.g. when the user pressed back on the Channel content screen
        if (view != null && view.getVideo() != null && mIsEmbedPlayerStarted) {
            mVideo = new WeakReference<>(view.getVideo());
            Queue.add(view.getVideo()); // don't show queue
        }
    }

    public PlayerEngine getPlayer() {
        return mPlayer.get(); // return view even if the one is destroyed
    }

    public Activity getActivity() {
        return getContext() instanceof Activity ? (Activity) getContext() : null;
    }

    @SuppressWarnings("unchecked")
    public <T extends PlayerEventListener> T getController(Class<T> clazz) {
        for (PlayerEventListener listener : mEventListeners) {
            if (clazz.isInstance(listener)) {
                return (T) listener;
            }
        }

        return null;
    }

    // Core events

    @Override
    public void onNewVideo(Video video) {
        mEventListeners.forEach(listener -> listener.onNewVideo(video));
        mVideo = new WeakReference<>(video);
        mIsEmbedPlayerStarted = true;
    }

    @Override
    public void onFinish() {
        mEventListeners.forEach(PlayerEventListener::onFinish);
    }

    @Override
    public void onInit() {
        // NOP. Internal event.
    }

    @Override
    public void onMetadata(MediaItemMetadata metadata) {
        mEventListeners.forEach(listener -> listener.onMetadata(metadata));
    }

    // Common events

    @Override
    public void onViewCreated() {
        mEventListeners.forEach(PlayerEventListener::onViewCreated);
    }

    @Override
    public void onViewDestroyed() {
        mEventListeners.forEach(PlayerEventListener::onViewDestroyed);
    }

    @Override
    public void onViewPaused() {
        super.onViewPaused();

        mEventListeners.forEach(PlayerEventListener::onViewPaused);
    }

    @Override
    public void onViewResumed() {
        super.onViewResumed();

        mEventListeners.forEach(PlayerEventListener::onViewResumed);
    }

    // End common events

    // Start engine events

    @Override
    public void onSourceChanged(Video item) {
        mEventListeners.forEach(listener -> listener.onSourceChanged(item));
    }

    @Override
    public void onEngineInitialized() {
        getTickleManager().addListener(this);

        mEventListeners.forEach(PlayerEventListener::onEngineInitialized);
    }

    @Override
    public void onEngineReleased() {
        getTickleManager().removeListener(this);

        mEventListeners.forEach(PlayerEventListener::onEngineReleased);
    }

    @Override
    public void onEngineError(int type, int rendererIndex, Throwable error) {
        mEventListeners.forEach(listener -> listener.onEngineError(type, rendererIndex, error));
    }

    @Override
    public void onPlay() {
        mEventListeners.forEach(PlayerEventListener::onPlay);
    }

    @Override
    public void onPause() {
        mEventListeners.forEach(PlayerEventListener::onPause);
    }

    @Override
    public void onPlayClicked() {
        mEventListeners.forEach(PlayerEventListener::onPlayClicked);
    }

    @Override
    public void onPauseClicked() {
        mEventListeners.forEach(PlayerEventListener::onPauseClicked);
    }

    @Override
    public void onSeekEnd() {
        mEventListeners.forEach(PlayerEventListener::onSeekEnd);
    }

    @Override
    public void onSeekPositionChanged(long positionMs) {
        mEventListeners.forEach(listener -> listener.onSeekPositionChanged(positionMs));
    }

    @Override
    public void onSpeedChanged(float speed) {
        mEventListeners.forEach(listener -> listener.onSpeedChanged(speed));
    }

    @Override
    public void onPlayEnd() {
        mEventListeners.forEach(PlayerEventListener::onPlayEnd);
    }

    @Override
    public void onBuffering() {
        mEventListeners.forEach(PlayerEventListener::onBuffering);
    }

    @Override
    public boolean onKeyDown(int keyCode) {

        for (PlayerEventListener listener : mEventListeners) {

            if (listener.onKeyDown(keyCode)) return true;
            
        }

        return false;

    }

    @Override
    public void onVideoLoaded(Video item) {
        mEventListeners.forEach(listener -> listener.onVideoLoaded(item));
    }

    @Override
    public void onTickle() {
        mEventListeners.forEach(PlayerEventListener::onTickle);
    }

    // End engine events

    // Start UI events

    @Override
    public void onSuggestionItemClicked(Video item) {
        mEventListeners.forEach(listener -> listener.onSuggestionItemClicked(item));
    }

    @Override
    public void onSuggestionItemLongClicked(Video item) {
        mEventListeners.forEach(listener -> listener.onSuggestionItemLongClicked(item));
    }

    @Override
    public void onScrollEnd(Video item) {
        mEventListeners.forEach(listener -> listener.onScrollEnd(item));
    }

    @Override
    public boolean onPreviousClicked() {

        for (PlayerEventListener listener : mEventListeners) {

            if (listener.onPreviousClicked()) return true;
            
        }

        return false;

    }

    @Override
    public boolean onNextClicked() {

        for (PlayerEventListener listener : mEventListeners) {

            if (listener.onNextClicked()) return true;
            
        }

        return false;
    }

    @Override
    public void onTrackSelected(FormatItem track) {
        mEventListeners.forEach(listener -> listener.onTrackSelected(track));
    }

    @Override
    public void onControlsShown(boolean shown) {
        mEventListeners.forEach(listener -> listener.onControlsShown(shown));
    }

    @Override
    public void onTrackChanged(FormatItem track) {
        mEventListeners.forEach(listener -> listener.onTrackChanged(track));
    }

    @Override
    public void onButtonClicked(int buttonId, int buttonState) {

        //super.onButtonClicked(buttonId, buttonState);

        mEventListeners.forEach(listener -> listener.onButtonClicked(buttonId, buttonState));
        
    }

    @Override
    public void onButtonLongClicked(int buttonId, int buttonState) {
        mEventListeners.forEach(listener -> listener.onButtonLongClicked(buttonId, buttonState));
    }

    // End UI events
}
