package minefarts.smarttube.app.models.playback.controllers;

import android.annotation.SuppressLint;
import android.content.Context;

import minefarts.smarttube.utils.MediaItemService;
import minefarts.smarttube.utils.ServiceManager;
import minefarts.smarttube.utils.service.data.MediaFormat;
import minefarts.smarttube.utils.data.MediaItemFormatInfo;
import minefarts.smarttube.utils.service.data.MediaItemMetadata;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.helpers.MessageHelpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.rx.RxHelper;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.Queue;
import minefarts.smarttube.app.models.data.SimpleMediaItem;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.data.VideoGroup;
import minefarts.smarttube.app.models.playback.BasePlayerController;
import minefarts.smarttube.app.models.playback.PlayerEventListener;
import minefarts.smarttube.ui.playback.PlaybackFragment2;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.exoplayer.selector.FormatItem;
import minefarts.smarttube.prefs.PlayerTweaksData;
import minefarts.smarttube.utils.Utils;
import minefarts.smarttube.app.presenters.ChannelUploadsPresenter;
import minefarts.smarttube.app.presenters.PlaybackPresenter;
import minefarts.smarttube.app.presenters.SearchPresenter;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.utils.LoadingManager;

import io.reactivex.disposables.Disposable;

import java.util.Collections;
import java.util.List;

public class VideoLoaderController extends BasePlayerController {
    
    private static final String TAG = VideoLoaderController.class.getSimpleName();

    private static Context mContext;
    private static PlaybackPresenter mPlaybackPresenter;
    private static ChannelUploadsPresenter mChannelUploadsPresenter;
    private static SearchPresenter mSearchPresenter;
    private static MediaItemService mMediaItemService;
    private final VideoStateController mVideoStateController;

    public VideoLoaderController(PlaybackPresenter playbackPresenter) {
        mContext = getContext();

        mPlaybackPresenter = playbackPresenter;
        mChannelUploadsPresenter = ChannelUploadsPresenter.instance(getContext());
        mSearchPresenter = SearchPresenter.instance(getContext());
        mMediaItemService = ServiceManager.getMediaItemService();
        mVideoStateController = new VideoStateController();
    }
    
    private Video mPendingVideo;
    private int mLastErrorType = -1;
    private SuggestionsController mSuggestionsController;
    private Disposable mFormatInfoAction;
    private Disposable mMpdStreamAction;

    private final Runnable mReloadVideo = () -> getMainController().onNewVideo(getVideo());
    
    private final Runnable mMetadataSync = () -> {
        if (getPlayer() != null) {
            savePosition();
            waitMetadataSync(getVideo(), false);
        }
    };

    private final Runnable mRestartEngine = () -> {
        if (getPlayer() != null) {
            getPlayer().restartEngine(); // properly save position of the current track
        }
    };

    private final Runnable mRebootApp = () -> {
        Video video = getVideo();
        if (getPlayer() != null) {
            Utils.restartTheApp(getContext(), video, getPlayer().getPositionMs());
        }
    };

    @Override
    public void onInit() {
        mSuggestionsController = getController(SuggestionsController.class);
        savePosition();
    }

    @Override
    public void onNewVideo(Video item) {
        if (item == null) return;

        if (!item.fromQueue && !item.belongsToPlaybackQueue()) {
            Queue.add(item);
        } else {
            item.fromQueue = false;
        }

        if (getPlayer() != null && getPlayer().isEngineInitialized()) { // player is initialized
            // Fix improperly resized video after exit from PIP (Device Formuler Z8 Pro)
            loadVideo(item); // force play immediately even the same video
        } else {
            mPendingVideo = item;
        }
    }

    @Override
    public void onSeekEnd() {
        savePosition();
    }

    @Override
    public void onEngineInitialized() {
        if (getPlayer() == null) return;
        
        loadVideo(Helpers.firstNonNull(mPendingVideo, getVideo()));
        getPlayer().setButtonState(R.id.action_repeat, getPlayerData().getPlaybackMode());
        mPendingVideo = null;
    }

    @Override
    public void onEngineReleased() {
        RxHelper.disposeActions(mFormatInfoAction, mMpdStreamAction);
        Utils.removeCallbacks(mReloadVideo, this::onNextClicked, mRestartEngine, mMetadataSync, mRebootApp);
    }

    @Override
    public void onEngineError(int type, int rendererIndex, Throwable error) {
        // IMPORTANT: error recovery may be invoked from a non-UI thread.
        // Never allow UI navigation / Activity finishing to happen here without safety.
        Log.e(TAG, "Player error occurred: %s. Trying to fix…", type);
        Log.e(TAG, "Renderer index: " + rendererIndex + ", error: " + error);

        mLastErrorType = type;
        try {
            runEngineErrorAction(type, rendererIndex, error);
        } catch (Throwable t) {
            Log.e(TAG, "Unhandled exception in onEngineError recovery", t);
            // Avoid silent crashes: attempt safe reload instead of crashing.
            try {
                reloadVideo();
            } catch (Throwable ignored) {
                // last resort: do nothing
            }
        }
    }

    @Override
    public void onVideoLoaded(Video video) {
        if (getPlayer() == null) return;

        mLastErrorType = -1;

        getPlayer().setButtonState(
            R.id.action_repeat, 
            video.finishOnEnded ? PlaybackFragment2.PLAYBACK_MODE_CLOSE : getPlayerData().getPlaybackMode()
        );

    }

    @Override
    public boolean onPreviousClicked() {
        if (getPlayer() == null) return true;

        savePosition();

        openVideoInt(mSuggestionsController.getPrevious());

        getPlayer().showOverlay(true);

        return true;
    }

    @Override
    public boolean onNextClicked() {
        if (getPlayer() == null || getVideo() == null) return true;

        savePosition();

        Video next = mSuggestionsController.getNext();

        if (next != null) {
            next.isShuffled = getVideo().isShuffled;
            openVideoInt(next);
        } else {
            waitMetadataSync(getVideo(), true);
        }

        getPlayer().showOverlay(true);

        return true;
    }

    @Override
    public void onPlayEnd() {
        if (getPlayer() == null) return;

        savePosition();

        // Stop the playback if the user is browsing options or reading comments
        int playbackMode = getPlaybackMode();
        if (getAppDialogPresenter().isDialogShown() && !getAppDialogPresenter().isOverlay() && playbackMode != PlaybackFragment2.PLAYBACK_MODE_ONE) {
            
            getAppDialogPresenter().setOnFinish(() -> {
                if (getPlayer() == null || getPlayer().getPositionMs() < getPlayer().getDurationMs()) return;
                applyPlaybackMode(getPlaybackMode());
            });

        } else {
            applyPlaybackMode(playbackMode);
        }
    }

    @Override
    public void onSuggestionItemClicked(Video item) {
        openVideoInt(item);

        if (getPlayer() != null)
            getPlayer().showControls(false);
    }

    @Override
    public boolean onKeyDown(int keyCode) {
        if (getPlayer() == null) return false;

        Utils.removeCallbacks(mRestartEngine, mRebootApp);

        return false;
    }

    private synchronized void savePosition() {
        if (getPlayer() == null || getVideo() == null) return;

        mVideoStateController.updateHistory(
            getVideo(),
            getPlayer().getPositionMs()
        );
    }

    // Force load and play!
    private void loadVideo(Video item) {
        if (getPlayer() != null && item != null) {
            
            Queue.setCurrent(item);
            
            getPlayer().setVideo(item);
            getPlayer().resetPlayerState();

            getPlayer().showProgressBar(true);
            onEngineReleased();

            mFormatInfoAction = mMediaItemService.getFormatInfoObserve(getVideo().videoId).subscribe(
                this::processFormatInfo,
                error -> {
                    getPlayer().showProgressBar(false);
                    runFormatErrorAction(error);
                }
            );
            
        }
    }

    private void waitMetadataSync(Video current, boolean showLoadingMsg) {
        
        if (current == null) return;

        if (current.nextMediaItem != null) {
            openVideoInt(Video.from(current.nextMediaItem));
        } else if (!current.isSynced) { // Maybe there's nothing left. E.g. when casting from phone
            // Wait in a loop while suggestions have been loaded...
            if (showLoadingMsg) {
                MessageHelpers.showMessageThrottled(getContext(), R.string.wait_data_loading);
            }
            // Short videos next fix (suggestions aren't loaded yet)
            boolean isEnded = getPlayer() != null && Math.abs(getPlayer().getDurationMs() - getPlayer().getPositionMs()) < 100;
            if (isEnded) {
                Utils.postDelayed(mMetadataSync, 1_000);
            }
        }
    }

    private void processFormatInfo(MediaItemFormatInfo formatInfo) {
        PlaybackFragment2 player = getPlayer();

        if (player == null || getVideo() == null) return;

        String bgImageUrl = null;

        getVideo().sync(formatInfo);

        if (formatInfo.isUnplayable()) {

            player.setTitle(formatInfo.getPlayabilityStatus());
            player.showProgressBar(false);
            mSuggestionsController.loadSuggestions(getVideo());
            bgImageUrl = getVideo().getBackgroundUrl();

            // 18+ video or the video is hidden/removed
            if (getPlayer() != null && getPlayer().isEngineInitialized()) {
                getPlayer().showOverlay(true);
                Utils.postDelayed(this::onNextClicked, 5_000);
            }

        } else if (acceptAdaptiveFormats(formatInfo) && formatInfo.containsDashFormats()) {
            Log.d(TAG, "Loading regular video in dash format...");
            player.openDash(formatInfo);
        } else if (acceptAdaptiveFormats(formatInfo) && formatInfo.containsSabrFormats()) {
            Log.d(TAG, "Loading video in sabr format...");
            player.openSabr(formatInfo);
        } else if (acceptDashLive(formatInfo)) {
            String dashUrl = formatInfo.getDashManifestUrl();
            if (dashUrl == null) {
                runFormatErrorAction(new IllegalStateException("Dash manifest url is null"));
                return;
            }
            Log.d(TAG, "Loading live video (current or past live stream) in dash format...");
            player.openDashUrl(dashUrl);
        } else if (formatInfo.isLive() && formatInfo.containsHlsUrl()) {
            String hlsUrl = formatInfo.getHlsManifestUrl();
            if (hlsUrl == null) {
                runFormatErrorAction(new IllegalStateException("Hls manifest url is null"));
                return;
            }
            Log.d(TAG, "Loading live video (current or past live stream) in hls format...");
            player.openHlsUrl(hlsUrl);
        } else if (formatInfo.containsUrlFormats()) {
            List<String> urlList = formatInfo.createUrlList();
            if (urlList == null || urlList.isEmpty()) {
                runFormatErrorAction(new IllegalStateException("Url list is null or empty"));
                return;
            }
            Log.d(TAG, "Loading url list video. This is always LQ...");
            player.openUrlList(applyFix(urlList));
        } else {
            Log.d(TAG, "Empty format info received. Seems future live translation. No video data to pass to the player.");
            player.setTitle(formatInfo.getPlayabilityStatus());
            player.showProgressBar(false);
            mSuggestionsController.loadSuggestions(getVideo());
            bgImageUrl = getVideo().getBackgroundUrl();
            scheduleReloadVideoTimer(30 * 1_000);
        }

        player.showBackground(bgImageUrl); // remove bg (if video playing) or set another bg
    }

    private void scheduleReloadVideoTimer(int delayMs) {
        if (getPlayer() == null || !getPlayer().isEngineInitialized()) return;

        Log.d(TAG, "Reloading the video...");
        getPlayer().showOverlay(true);
        Utils.postDelayed(mReloadVideo, delayMs);
    }

    private void scheduleRestartEngineTimer(int delayMs) {
        if (getPlayer() == null) return;
        Log.d(TAG, "Restarting the engine...");
        getPlayer().showOverlay(true);
        Utils.postDelayed(mRestartEngine, delayMs);
    }

    private void openVideoInt(Video item) {
        if (item == null) return;

        onEngineReleased();

        if (item.hasVideo()) {
            // NOTE: Next clicked: instant playback even a mix
            getMainController().onNewVideo(item);
        } else {
            openVideo(item);
        }

    }

    public static void openVideo(Video item) {
        
        if (item.hasVideo() && !item.isPlaylistInChannel()) {
            mPlaybackPresenter.openVideo(item);
        
        } else if (item.hasChannel() || item.belongsToChannelUploads()) {
            ServiceManager.chooseChannelPresenter(mContext, item);
        
        } else if (item.hasPlaylist() || item.hasNestedItems()) {
            mChannelUploadsPresenter.openChannel(item);
        
        } else if (item.isChapter) {
            mPlaybackPresenter.setPosition(item.startTimeMs);
        
        } else if (item.searchQuery != null ) {
            mSearchPresenter.onSearch(item.searchQuery);
        }
        
    }

    private void runFormatErrorAction(Throwable error) {

        String message = error.getMessage();
        String className = error.getClass().getSimpleName();
        String fullMsg = String.format("loadFormatInfo error: %s: %s", className, Utils.getStackTraceAsString(error));
        Log.e(TAG, fullMsg);

        if (!Helpers.containsAny(message, "fromNullable result is null")) {
            MessageHelpers.showLongMessage(getContext(), fullMsg);
        }

        // Kotlin non-null parameter received null (e.g. `playerUrl`)
        // Treat it as a temporary unavailability instead of an unrecoverable failure.
        boolean isNullPlayerUrl = message != null && (
                (message.contains("parameter specified as non-null is null") && message.contains("playerUrl"))
                        || message.contains("playerUrl")
                        || message.contains("player url")
        );

        if (Helpers.containsAny(message, "Unexpected token", "Syntax error", "invalid argument") || // temporal fix
                Helpers.equalsAny(className, "PoTokenException", "BadWebViewException")) {
            ServiceManager.applyNoPlaybackFix();
            reloadVideo();
        } else if (Helpers.containsAny(message, "is not defined")) {
            ServiceManager.invalidateCache();
            reloadVideo();
        } else if (isNullPlayerUrl) {
            // Avoid tight retry loop when upstream returns partial/empty data.
            Log.e(TAG, "playerUrl is null, scheduling delayed reload...");
            scheduleReloadVideoTimer(3_000);
        } else {
            Log.e(TAG, "Probably no internet connection");
            scheduleReloadVideoTimer(1_000);
        }

    }
    
    private void runEngineErrorAction(int type, int rendererIndex, Throwable error) {

        if (getVideo() != null && getVideo().isLiveEnd) {
            // Url no longer works (e.g. live stream ended)
            getMainController().onPlayEnd();
            return;
        }

        boolean restart = applyEngineErrorAction(type, rendererIndex, error);

        if (restart) {
            restartEngine();
        } else {
            reloadVideo();
        }

    }

    private boolean applyEngineErrorAction(int type, int rendererIndex, Throwable error) {
        
        boolean restartEngine = true;
        boolean showMessage = true;
        String errorContent = error != null ? error.getMessage() : null;
        String errorTitle = getErrorTitle(type, rendererIndex);
        String errorMessage = errorTitle + "\n" + errorContent;

        if (error instanceof OutOfMemoryError || (error != null && error.getCause() instanceof OutOfMemoryError)) {
            
            getPlayerTweaksData().setSectionPlaylistEnabled(false);
            restartEngine = false;

        } else if (Helpers.containsAny(errorContent, "Exception in CronetUrlRequest", "Response code: 503")) {

            if (!(getVideo() != null && !getVideo().isLive)) { // Finished live stream may provoke errors in Cronet
                restartEngine = false;
            }

        } else if (type == PlayerEventListener.ERROR_TYPE_SOURCE && rendererIndex == PlayerEventListener.RENDERER_INDEX_UNKNOWN) {
            // NOTE: Starts with any (url deciphered incorrectly)
            // "Response code: 403" (poToken error, forbidden)
            // "Response code: 404" (not sure whether below helps)
            // "Response code: 503" (not sure whether below helps)
            // "Response code: 400" (not sure whether below helps)
            // "Response code: 429" (subtitle error, too many requests)
            // "Response code: 500" (subtitle error, generic server error)

            // NOTE: Fixing too many requests or network issues
            // NOTE: All these errors have unknown renderer (-1)
            // "Unable to connect to", "Invalid NAL length", "Response code: 421",
            // "Response code: 404", "Response code: 429", "Invalid integer size",
            // "Unexpected ArrayIndexOutOfBoundsException", "Unexpected IndexOutOfBoundsException"
            if (Helpers.startsWithAny(errorContent, "Response code: 403")) {
                ServiceManager.applyNoPlaybackFix();
            
            } else if (getPlayer() != null && !FormatItem.SUBTITLE_NONE.equals(getPlayer().getSubtitleFormat())) {
                getPlayerData().setFormat(FormatItem.SUBTITLE_NONE); // Response code: 429
            
            } else {
                ServiceManager.applyNoPlaybackFix(); // Response code: 403
            }
            
            restartEngine = false;
            showMessage = false;
            
        } else if (type == PlayerEventListener.ERROR_TYPE_RENDERER && rendererIndex == PlayerEventListener.RENDERER_INDEX_SUBTITLE) {
            
            // "Response code: 429" (subtitle error)
            // "Response code: 500" (subtitle error)
            getPlayerData().setFormat(FormatItem.SUBTITLE_NONE);
            restartEngine = false;

        } else if (type == PlayerEventListener.ERROR_TYPE_RENDERER && rendererIndex == PlayerEventListener.RENDERER_INDEX_VIDEO) {
            
            getPlayerData().setFormat(FormatItem.VIDEO_FHD_AVC_30);
            restartEngine = false;
    
        } else if (type == PlayerEventListener.ERROR_TYPE_RENDERER && rendererIndex == PlayerEventListener.RENDERER_INDEX_AUDIO) {
            
            getPlayerData().setFormat(FormatItem.AUDIO_HQ_MP4A);
            restartEngine = false;
            
        } else if (type == PlayerEventListener.ERROR_TYPE_UNEXPECTED) {
            // Hide unknown errors on all devices
            showMessage = false;
        }

        if (showMessage) {
            MessageHelpers.showLongMessage(getContext(), errorMessage);
        }

        return restartEngine;
    }

    @SuppressLint("StringFormatMatches")
    private String getErrorTitle(int type, int rendererIndex) {
        String errorTitle;
        int msgResId;

        switch (type) {
            // Some ciphered data could be outdated.
            // Might happen when the app wasn't used quite a long time.
            case PlayerEventListener.ERROR_TYPE_SOURCE:
                switch (rendererIndex) {
                    case PlayerEventListener.RENDERER_INDEX_VIDEO:
                        msgResId = R.string.msg_player_error_video_source;
                        break;
                    case PlayerEventListener.RENDERER_INDEX_AUDIO:
                        msgResId = R.string.msg_player_error_audio_source;
                        break;
                    case PlayerEventListener.RENDERER_INDEX_SUBTITLE:
                        msgResId = R.string.msg_player_error_subtitle_source;
                        break;
                    default:
                        msgResId = R.string.unknown_source_error;
                }
                errorTitle = getContext().getString(msgResId);
                break;
            case PlayerEventListener.ERROR_TYPE_RENDERER:
                switch (rendererIndex) {
                    case PlayerEventListener.RENDERER_INDEX_VIDEO:
                        msgResId = R.string.msg_player_error_video_renderer;
                        break;
                    case PlayerEventListener.RENDERER_INDEX_AUDIO:
                        msgResId = R.string.msg_player_error_audio_renderer;
                        break;
                    case PlayerEventListener.RENDERER_INDEX_SUBTITLE:
                        msgResId = R.string.msg_player_error_subtitle_renderer;
                        break;
                    default:
                        msgResId = R.string.unknown_renderer_error;
                }
                errorTitle = getContext().getString(msgResId);
                break;
            case PlayerEventListener.ERROR_TYPE_UNEXPECTED:
                errorTitle = getContext().getString(R.string.msg_player_error_unexpected);
                break;
            default:
                errorTitle = getContext().getString(R.string.msg_player_error, type);
                break;
        }

        return errorTitle;
    }

    private void restartEngine() {
        scheduleRestartEngineTimer(1_000);
    }

    private void reloadVideo() {
        scheduleReloadVideoTimer(1_000);
    }

    private List<String> applyFix(List<String> urlList) {
        // Sometimes top url cannot be played
        if (mLastErrorType == PlayerEventListener.ERROR_TYPE_SOURCE) {
            Collections.reverse(urlList);
        }

        return urlList;
    }

    private void applyPlaybackMode(int playbackMode) {
        if (getPlayer() == null) return;

        Video video = getVideo();
        // Fix simultaneous videos loading (e.g. when playback ends and user opens new video)
        if (video == null || RxHelper.isAnyActionRunning(mFormatInfoAction, mMpdStreamAction)) return;

        switch (playbackMode) {
            case PlaybackFragment2.PLAYBACK_MODE_REVERSE_LIST:
                if (video.hasPlaylist() || video.belongsToChannelUploads() || video.belongsToChannel()) {
                    VideoGroup group = video.getGroup();
                    if (group != null && group.indexOf(video) != 0) { // stop after first
                        onPreviousClicked();
                    }
                    break;
                }
            case PlaybackFragment2.PLAYBACK_MODE_ALL:
            case PlaybackFragment2.PLAYBACK_MODE_SHUFFLE:
                onNextClicked();
                break;
            case PlaybackFragment2.PLAYBACK_MODE_ONE:
                getPlayer().setPositionMs(100); // fix frozen image on Android 4?
                break;
            case PlaybackFragment2.PLAYBACK_MODE_CLOSE:
                // Close player if suggestions not shown
                // Except when playing from queue
                if (Queue.getNext() != null) {
                    onNextClicked();
                } else {
                    AppDialogPresenter dialog = getAppDialogPresenter();
                    if (!getPlayer().isSuggestionsShown() && (!dialog.isDialogShown() || dialog.isOverlay())) {
                        dialog.closeDialog();
                        getPlayer().finishReally();
                    }
                }
                break;
            case PlaybackFragment2.PLAYBACK_MODE_PAUSE:
                // Stop player after each video.
                // Except when playing from queue
                if (Queue.getNext() != null) {
                    onNextClicked();
                } else {
                    getPlayer().setPositionMs(getPlayer().getDurationMs());
                    getPlayer().setPlayWhenReady(false);
                    getPlayer().showSuggestions(true);
                }
                break;
            case PlaybackFragment2.PLAYBACK_MODE_LIST:
                // if video has a playlist load next or restart playlist
                if (video.hasNextPlaylist() || Queue.getNext() != null) {
                    onNextClicked();
                } else {
                    restartPlaylistIfNeeded();
                }
                break;
            default:
                Log.e(TAG, "Undetected repeat mode " + playbackMode);
                break;
        }
    }

    private void restartPlaylistIfNeeded() {
        if (getPlayer() == null || getVideo() == null) return;
        
        VideoGroup group = getVideo().getGroup(); // Get the VideoGroup (playlist)

        if (group != null && !group.isEmpty() && getVideo().belongsToSamePlaylistGroup()) {
            openVideoInt(group.get(0));
        } else {
            Log.e(TAG, "VideoGroup is null or empty. Can't restart playlist.");
            getPlayer().setPositionMs(getPlayer().getDurationMs());
            getPlayer().setPlayWhenReady(false);
            getPlayer().showSuggestions(true);
        }
    }

    private boolean acceptAdaptiveFormats(MediaItemFormatInfo formatInfo) {

        boolean isLive = formatInfo.isLive();

        boolean atStart = (formatInfo.getStartTimeMs() == 0);

        return !(isLive && atStart);
    }

    private boolean acceptDashLive(MediaItemFormatInfo formatInfo) {
        return formatInfo.isLive() && formatInfo.containsDashUrl();
    }

    @Override
    public void onMetadata(MediaItemMetadata metadata) {

        ServiceManager.disposeActions();

        if (getPlayer() == null || getPlayerData() == null || getVideo() == null || getVideo().playlistInfo == null ||
                getPlayerData().getPlaybackMode() != PlaybackFragment2.PLAYBACK_MODE_SHUFFLE) return;

        if (getVideo().playlistInfo.getSize() != -1) {

            Video video = new Video();
            video.playlistId = getVideo().playlistId;
            video.playlistIndex = Utils.getRandomIndex(getVideo().playlistInfo.getCurrentIndex(), getVideo().playlistInfo.getSize());
            ServiceManager.loadMetadata(video, randomMetadata -> {
                if (randomMetadata.getNextVideo() == null) {
                    return;
                }

                getVideo().nextMediaItem = SimpleMediaItem.from(randomMetadata);
                getPlayer().setNextTitle(Video.from(getVideo().nextMediaItem));
            });

        } else {

            VideoGroup topRow = getPlayer().getSuggestionsByIndex(0); // the playlist row
            if (topRow != null) {
                int currentIdx = topRow.indexOf(getVideo());
                int randomIndex = Utils.getRandomIndex(currentIdx, topRow.getSize());

                if (randomIndex != -1) {
                    Video nextVideo = topRow.get(randomIndex);
                    getVideo().nextMediaItem = SimpleMediaItem.from(nextVideo);
                    getPlayer().setNextTitle(nextVideo);
                }
            }
            
        }

    }

    private int getPlaybackMode() {
        int playbackMode = getPlayerData().getPlaybackMode();

        Video video = getVideo();
        if (video != null && video.finishOnEnded) {
            playbackMode = PlaybackFragment2.PLAYBACK_MODE_CLOSE;
        } else if (video != null && video.belongsToShortsGroup() && getPlayerTweaksData().isLoopShortsEnabled()) {
            playbackMode = PlaybackFragment2.PLAYBACK_MODE_ONE;
        }
        return playbackMode;
    }

}
