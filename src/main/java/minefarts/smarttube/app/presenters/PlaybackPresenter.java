package minefarts.smarttube.app.presenters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;
import minefarts.smarttube.app.models.data.Queue;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.playback.BasePlayerController;
import minefarts.smarttube.app.models.playback.controllers.ChatController;
import minefarts.smarttube.app.models.playback.controllers.CommentsController;
import minefarts.smarttube.app.models.playback.controllers.ContentBlockController;
import minefarts.smarttube.app.models.playback.controllers.PlayerUIController;
import minefarts.smarttube.app.models.playback.controllers.RemoteController;
import minefarts.smarttube.app.models.playback.controllers.VideoLoaderController;
import minefarts.smarttube.app.models.playback.controllers.VideoStateController;
import minefarts.smarttube.app.models.playback.PlayerEventListener;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.app.presenters.dialogs.menu.VideoMenuPresenter;
import minefarts.smarttube.ui.playback.PlaybackFragment2;
import minefarts.smarttube.exoplayer.selector.FormatItem;
import minefarts.smarttube.utils.Utils;
import com.liskovsoft.googlecommon.common.helpers.ServiceHelper;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlaybackPresenter extends BasePresenter<PlaybackFragment2> implements PlayerEventListener {
    
    @SuppressLint("StaticFieldLeak")
    private static PlaybackPresenter sInstance;
    
    public List<PlayerEventListener> mEventListeners = new CopyOnWriteArrayList<PlayerEventListener>() {
        @Override
        public boolean add(PlayerEventListener listener) {
            ((BasePlayerController) listener).setMainController(PlaybackPresenter.this);

            return super.add(listener);
        }
    };
    private WeakReference<Video> mVideo;
    // Fix for using destroyed view
    private WeakReference<PlaybackFragment2> mPlayer = new WeakReference<>(null);
    private boolean mIsEmbedPlayerStarted;

    public static PlaybackPresenter instance(Context context) {
        if (sInstance == null) {
            sInstance = new PlaybackPresenter();
            sInstance.mEventListeners.add(new VideoStateController());
            sInstance.mEventListeners.add(new PlayerUIController());
            sInstance.mEventListeners.add(new VideoLoaderController(sInstance));
            sInstance.mEventListeners.add(new RemoteController(context));
            sInstance.mEventListeners.add(new ContentBlockController());
            sInstance.mEventListeners.add(new ChatController());
            sInstance.mEventListeners.add(new CommentsController());
        }

        sInstance.setContext(context);

        return sInstance;
    }

    @Override
    public void onViewInitialized() {
        super.onViewInitialized();

        for (PlayerEventListener listener : mEventListeners) {
            listener.onInit();
        }
    }


    public void openVideo(String videoId) {
        openVideo(videoId, false, -1);
    }

    /**
     * Opens video item from splash view
     */
    public void openVideo(String videoId, boolean finishOnEnded, long timeMs) {
        if (videoId == null) return;

        Video video = Video.from(videoId);
        video.finishOnEnded = finishOnEnded;
        video.pendingPosMs = timeMs;

        openVideo(video);
    }

    public void openVideo(Video video) {
        if (video == null) return;

        onNewVideo(video);

        getViewManager().startView(PlaybackFragment2.class);
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
    public void setView(PlaybackFragment2 view) {
        super.setView(view);
        mPlayer = new WeakReference<>(view);

        // Fix playing the previous video when switching between embed and fullscreen players.
        // E.g. when the user pressed back on the Channel content screen
        if (view != null && view.getVideo() != null && mIsEmbedPlayerStarted) {
            mVideo = new WeakReference<>(view.getVideo());
            Queue.add(view.getVideo()); // don't show queue
        }
    }

    public PlaybackFragment2 getPlayer() {
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
        for (PlayerEventListener listener : mEventListeners) {
            listener.onNewVideo(video);
        }
        mVideo = new WeakReference<>(video);
        mIsEmbedPlayerStarted = true;
    }

    @Override
    public void onFinish() {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onFinish();
        }
    }

    @Override
    public void onInit() {
        // NOP. Internal event.
    }

    @Override
    public void onMetadata(MediaItemMetadata metadata) {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onMetadata(metadata);
        }
    }

    // Common events

    @Override
    public void onViewCreated() {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onViewCreated();
        }
    }

    @Override
    public void onViewDestroyed() {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onViewDestroyed();
        }
    }

    @Override
    public void onViewPaused() {
        super.onViewPaused();

        for (PlayerEventListener listener : mEventListeners) {
            listener.onViewPaused();
        }
    }

    @Override
    public void onViewResumed() {
        super.onViewResumed();

        for (PlayerEventListener listener : mEventListeners) {
            listener.onViewResumed();
        }
    }

    // End common events

    // Start engine events

    @Override
    public void onSourceChanged(Video item) {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onSourceChanged(item);
        }
    }

    @Override
    public void onEngineInitialized() {
        getTickleManager().addListener(this);

        for (PlayerEventListener listener : mEventListeners) {
            listener.onEngineInitialized();
        }
    }

    @Override
    public void onEngineReleased() {
        getTickleManager().removeListener(this);

        for (PlayerEventListener listener : mEventListeners) {
            listener.onEngineReleased();
        }
    }

    @Override
    public void onEngineError(int type, int rendererIndex, Throwable error) {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onEngineError(type, rendererIndex, error);
        }
    }

    @Override
    public void onPlay() {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onPlay();
        }
    }

    @Override
    public void onPause() {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onPause();
        }
    }

    @Override
    public void onPlayClicked() {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onPlayClicked();
        }
    }

    @Override
    public void onPauseClicked() {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onPauseClicked();
        }
    }

    @Override
    public void onSeekEnd() {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onSeekEnd();
        }
    }

    @Override
    public void onSeekPositionChanged(long positionMs) {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onSeekPositionChanged(positionMs);
        }
    }

    @Override
    public void onSpeedChanged(float speed) {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onSpeedChanged(speed);
        }
    }

    @Override
    public void onPlayEnd() {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onPlayEnd();
        }
    }

    @Override
    public void onBuffering() {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onBuffering();
        }
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
        for (PlayerEventListener listener : mEventListeners) {
            listener.onVideoLoaded(item);
        }
    }

    @Override
    public void onTickle() {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onTickle();
        }
    }

    // End engine events

    // Start UI events

    @Override
    public void onSuggestionItemClicked(Video item) {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onSuggestionItemClicked(item);
        }
    }

    @Override
    public void onSuggestionItemLongClicked(Video item) {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onSuggestionItemLongClicked(item);
        }
    }

    @Override
    public void onScrollEnd(Video item) {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onScrollEnd(item);
        }
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
        for (PlayerEventListener listener : mEventListeners) {
            listener.onTrackSelected(track);
        }
    }

    @Override
    public void onControlsShown(boolean shown) {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onControlsShown(shown);
        }
    }

    @Override
    public void onTrackChanged(FormatItem track) {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onTrackChanged(track);
        }
    }

    @Override
    public void onButtonClicked(int buttonId, int buttonState) {

        //super.onButtonClicked(buttonId, buttonState);

        for (PlayerEventListener listener : mEventListeners) {
            listener.onButtonClicked(buttonId, buttonState);
        }
        
    }

    @Override
    public void onButtonLongClicked(int buttonId, int buttonState) {
        for (PlayerEventListener listener : mEventListeners) {
            listener.onButtonLongClicked(buttonId, buttonState);
        }
    }

    // End UI events
}
