package com.liskovsoft.smartyoutubetv2.common.app.presenters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Playlist;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.BasePlayerController;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers.AutoFrameRateController;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers.ChatController;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers.CommentsController;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers.ContentBlockController;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers.HQDialogController;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers.PlayerUIController;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers.RemoteController;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers.SuggestionsController;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers.VideoLoaderController;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers.VideoStateController;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.listener.PlayerEventListener;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.listener.PlayerUiEventListener;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.listener.ViewEventListener;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.base.BasePresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.dialogs.menu.VideoMenuPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.views.PlaybackView;
import com.liskovsoft.smartyoutubetv2.common.exoplayer.selector.FormatItem;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils.ChainProcessor;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils.Processor;
import com.liskovsoft.googlecommon.common.helpers.ServiceHelper;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central presenter that orchestrates playback-related controllers and forwards
 * events between the PlaybackView and registered PlayerEventListener controllers.
 *
 * Responsibilities:
 * - Hold singleton instance and application context.
 * - Manage a list of ordered PlayerEventListener controllers (VideoState, UI, Loader, etc).
 * - Provide convenience API for opening videos and manipulating playback from other UI.
 * - Forward lifecycle, engine and UI events to all registered controllers.
 *
 * Notes:
 * - Controllers are added in an order that matters; some rely on previous controllers.
 * - Internally uses CopyOnWriteArrayList to allow safe concurrent iteration from UI threads.
 */
public class PlaybackPresenter extends BasePresenter<PlaybackView> implements PlayerEventListener {
    private static final String TAG = PlaybackPresenter.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static PlaybackPresenter sInstance;

    // Event listeners (controllers). CopyOnWriteArrayList to avoid CME when iterating.
    // When a listener is added we initialize its main controller reference.
    private final List<PlayerEventListener> mEventListeners = new CopyOnWriteArrayList<PlayerEventListener>() {
        @Override
        public boolean add(PlayerEventListener listener) {
            ((BasePlayerController) listener).setMainController(PlaybackPresenter.this);

            return super.add(listener);
        }
    };

    // Weak reference to the currently active Video (to avoid leaking large objects).
    private WeakReference<Video> mVideo;

    // Weak reference to the PlaybackView. View may be destroyed and recreated frequently.
    private WeakReference<PlaybackView> mPlayer = new WeakReference<>(null);

    // Flag used to detect transitions from embed -> fullscreen player flows.
    private boolean mIsEmbedPlayerStarted;

    private PlaybackPresenter(Context context) {
        super(context);

        // NOTE: order matters — controllers may depend on work performed by earlier ones.
        mEventListeners.add(new VideoStateController());
        mEventListeners.add(new SuggestionsController());
        mEventListeners.add(new PlayerUIController());
        mEventListeners.add(new VideoLoaderController());
        mEventListeners.add(new RemoteController(context));
        mEventListeners.add(new ContentBlockController());
        mEventListeners.add(new AutoFrameRateController());
        mEventListeners.add(new HQDialogController());
        mEventListeners.add(new ChatController());
        mEventListeners.add(new CommentsController());
    }

    /**
     * Singleton accessor. Requires a context on first call.
     */
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

        // Re-initialize controllers after view is ready (e.g. after app restart)
        initControllers();
    }

    /**
     * Initialize controllers by dispatching onInit.
     * Called when presenter view is initialized or app restarts.
     */
    private void initControllers() {
        // Re-init after app exit
        process(PlayerEventListener::onInit);
    }

    // Convenience methods to open videos --------------------------------------------------

    public void openVideo(String videoId) {
        openVideo(videoId, false, -1, false);
    }

    /**
     * Open a video by id with optional parameters.
     *
     * @param videoId      video id to open
     * @param finishOnEnded if true the player will finish when playback ends
     * @param timeMs       initial seek position in milliseconds
     * @param incognito    whether to open in incognito mode
     */
    public void openVideo(String videoId, boolean finishOnEnded, long timeMs, boolean incognito) {
        if (videoId == null) {
            return;
        }

        Video video = Video.from(videoId);
        video.finishOnEnded = finishOnEnded;
        video.pendingPosMs = timeMs;
        video.incognito = incognito;
        openVideo(video);
    }

    /**
     * Open a prepared Video instance.
     * Handles switching from embed to fullscreen player and starts PlaybackView.
     */
    public void openVideo(Video video) {
        if (video == null) {
            return;
        }

        if (getView() != null && getView().isEmbed()) { // switching from the embed player to the fullscreen one
            // The embed player doesn't disposed properly — ensure it finishes and detach view.
            getView().finishReally();
            setView(null);
        }

        onNewVideo(video);

        getViewManager().startView(PlaybackView.class);
        mIsEmbedPlayerStarted = false;
    }

    public Video getVideo() {
        return mVideo != null ? mVideo.get() : null;
    }

    // State helpers ----------------------------------------------------------------------

    /**
     * Returns true when playback is running in background (engine initialized, view blocked,
     * player not in foreground and activity is valid).
     */
    public boolean isRunningInBackground() {
        return getView() != null &&
                getView().isEngineBlocked() &&
                getView().isEngineInitialized() &&
                !getViewManager().isPlayerInForeground() &&
                getContext() instanceof Activity && Utils.checkActivity((Activity) getContext());
    }

    public boolean isInPipMode() {
        return getView() != null && getView().isInPIPMode();
    }

    public boolean isOverlayShown() {
        return getView() != null && getView().isOverlayShown();
    }

    public boolean isPlaying() {
        return getView() != null && getView().isPlaying();
    }

    public boolean isEngineBlocked() {
        return getView() != null && getView().isEngineBlocked();
    }

    public boolean isEngineInitialized() {
        return getView() != null && getView().isEngineInitialized();
    }

    // UI helpers -------------------------------------------------------------------------

    public void forceFinish() {
        if (getView() != null) {
            getView().finishReally();
        }
    }

    public void setPosition(String timeCode) {
        setPosition(ServiceHelper.timeTextToMillis(timeCode));
    }

    /**
     * Set playback position. If player is in foreground set directly on the view,
     * otherwise save pending position to the Video and open the player.
     */
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

    // Controller registration & access ---------------------------------------------------

    @Override
    public void setView(PlaybackView view) {
        super.setView(view);
        mPlayer = new WeakReference<>(view);

        // When restoring view after embed -> fullscreen switching, preserve current video and add to playlist.
        if (view != null && view.getVideo() != null && mIsEmbedPlayerStarted) {
            mVideo = new WeakReference<>(view.getVideo());
            Playlist.instance().add(view.getVideo()); // keep playback queue consistent
        }
    }

    /**
     * Returns the PlaybackView even if it has been destroyed (weak ref may be null).
     */
    public PlaybackView getPlayer() {
        return mPlayer.get();
    }

    public Activity getActivity() {
        return getContext() instanceof Activity ? (Activity) getContext() : null;
    }

    /**
     * Lookup a registered controller by class.
     */
    @SuppressWarnings("unchecked")
    public <T extends PlayerEventListener> T getController(Class<T> clazz) {
        for (PlayerEventListener listener : mEventListeners) {
            if (clazz.isInstance(listener)) {
                return (T) listener;
            }
        }

        return null;
    }

    // Core event dispatching ------------------------------------------------------------

    @Override
    public void onNewVideo(Video video) {
        // Propagate to controllers and keep weak reference to current video
        process(listener -> listener.onNewVideo(video));
        mVideo = new WeakReference<>(video);
        mIsEmbedPlayerStarted = true;
    }

    @Override
    public void onFinish() {
        process(PlayerEventListener::onFinish);
    }

    @Override
    public void onInit() {
        // No-op: internal only
    }

    @Override
    public void onMetadata(MediaItemMetadata metadata) {
        process(listener -> listener.onMetadata(metadata));
    }

    // Helpers to apply processors over mEventListeners ----------------------------------

    private boolean chainProcess(ChainProcessor<PlayerEventListener> processor) {
        return Utils.chainProcess(mEventListeners, processor);
    }

    private void process(Processor<PlayerEventListener> processor) {
        Utils.process(mEventListeners, processor);
    }

    // View lifecycle event forwarding ---------------------------------------------------

    @Override
    public void onViewCreated() {
        process(ViewEventListener::onViewCreated);
    }

    @Override
    public void onViewDestroyed() {
        process(ViewEventListener::onViewDestroyed);
    }

    @Override
    public void onViewPaused() {
        super.onViewPaused();
        process(ViewEventListener::onViewPaused);
    }

    @Override
    public void onViewResumed() {
        super.onViewResumed();
        process(ViewEventListener::onViewResumed);
    }

    // Engine related events ------------------------------------------------------------

    @Override
    public void onSourceChanged(Video item) {
        process(listener -> listener.onSourceChanged(item));
    }

    @Override
    public void onEngineInitialized() {
        getTickleManager().addListener(this);
        process(PlayerEventListener::onEngineInitialized);
    }

    @Override
    public void onEngineReleased() {
        getTickleManager().removeListener(this);
        process(PlayerEventListener::onEngineReleased);
    }

    @Override
    public void onEngineError(int type, int rendererIndex, Throwable error) {
        process(listener -> listener.onEngineError(type, rendererIndex, error));
    }

    @Override
    public void onPlay() {
        process(PlayerEventListener::onPlay);
    }

    @Override
    public void onPause() {
        process(PlayerEventListener::onPause);
    }

    @Override
    public void onPlayClicked() {
        process(PlayerEventListener::onPlayClicked);
    }

    @Override
    public void onPauseClicked() {
        process(PlayerEventListener::onPauseClicked);
    }

    @Override
    public void onSeekEnd() {
        process(PlayerEventListener::onSeekEnd);
    }

    @Override
    public void onSeekPositionChanged(long positionMs) {
        process(listener -> listener.onSeekPositionChanged(positionMs));
    }

    @Override
    public void onSpeedChanged(float speed) {
        process(listener -> listener.onSpeedChanged(speed));
    }

    @Override
    public void onPlayEnd() {
        process(PlayerEventListener::onPlayEnd);
    }

    @Override
    public void onBuffering() {
        process(PlayerEventListener::onBuffering);
    }

    @Override
    public boolean onKeyDown(int keyCode) {
        return chainProcess(listener -> listener.onKeyDown(keyCode));
    }

    @Override
    public void onVideoLoaded(Video item) {
        process(listener -> listener.onVideoLoaded(item));
    }

    @Override
    public void onTickle() {
        process(PlayerEventListener::onTickle);
    }

    // UI events forwarded to controllers ------------------------------------------------

    @Override
    public void onSuggestionItemClicked(Video item) {
        process(listener -> listener.onSuggestionItemClicked(item));
    }

    @Override
    public void onSuggestionItemLongClicked(Video item) {
        process(listener -> listener.onSuggestionItemLongClicked(item));
    }

    @Override
    public void onScrollEnd(Video item) {
        process(listener -> listener.onScrollEnd(item));
    }

    @Override
    public boolean onPreviousClicked() {
        return chainProcess(PlayerEventListener::onPreviousClicked);
    }

    @Override
    public boolean onNextClicked() {
        return chainProcess(PlayerEventListener::onNextClicked);
    }

    @Override
    public void onTrackSelected(FormatItem track) {
        process(listener -> listener.onTrackSelected(track));
    }

    @Override
    public void onControlsShown(boolean shown) {
        process(listener -> listener.onControlsShown(shown));
    }

    @Override
    public void onTrackChanged(FormatItem track) {
        process(listener -> listener.onTrackChanged(track));
    }

    @Override
    public void onButtonClicked(int buttonId, int buttonState) {
        process(listener -> listener.onButtonClicked(buttonId, buttonState));
    }

    @Override
    public void onButtonLongClicked(int buttonId, int buttonState) {
        process(listener -> listener.onButtonLongClicked(buttonId, buttonState));
    }

    // End of class
}
