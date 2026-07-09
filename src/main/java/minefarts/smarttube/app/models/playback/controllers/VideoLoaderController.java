package minefarts.smarttube.app.models.playback.controllers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;

import androidx.core.content.ContextCompat;
import androidx.annotation.Nullable;

import minefarts.smarttube.utils.MediaItemService;
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
import minefarts.smarttube.CacheManager;
import minefarts.smarttube.utils.service.ContentService;
import minefarts.smarttube.utils.data.ChapterItem;
import minefarts.smarttube.utils.service.data.MediaGroup;
import minefarts.smarttube.app.models.playback.ui.SeekBarSegment;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.utils.BrowseProcessorManager;
import minefarts.smarttube.prefs.GeneralData;
import minefarts.smarttube.app.models.data.DislikesResult;
import minefarts.smarttube.google.common.helpers.RetrofitHelper;
import minefarts.smarttube.utils.videoinfo.V2.VideoInfoService;
import minefarts.smarttube.utils.videoinfo.V2.VideoInfoApi;
import minefarts.smarttube.google.youtubedata3.YouTubeDataServiceInt;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.util.function.Consumer;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class VideoLoaderController extends BasePlayerController {
    
    private static final String TAG = VideoLoaderController.class.getSimpleName();

    private final Context mContext;
    private final PlaybackPresenter mPlaybackPresenter;
    private final ChannelUploadsPresenter mChannelUploadsPresenter;
    private final SearchPresenter mSearchPresenter;
    private final MediaItemService mMediaItemService;
    private final VideoStateController mVideoStateController;
    
    public VideoLoaderController(PlaybackPresenter playbackPresenter) {
        mContext = getContext();
        mPlaybackPresenter = playbackPresenter;
        mChannelUploadsPresenter = ChannelUploadsPresenter.instance(mContext);
        mSearchPresenter = SearchPresenter.instance(mContext);
        mMediaItemService = getMediaItemService();
        mVideoStateController = new VideoStateController();
    }

    private BrowseProcessorManager mBrowseProcessor;
    private ContentService mContentService;
    private VideoInfoApi mVideoInfoApi;

    @Override
    public void onInit() {
        mBrowseProcessor = new BrowseProcessorManager(
            getContext(), 
            PlaybackPresenter.instance(getContext())::syncItem
        );
        mContentService = getContentService();
        mVideoInfoApi = VideoInfoService.instance().mVideoInfoApi;
    }

    private final List<Disposable> mActions = new ArrayList<>();

    private Video mNextSectionVideo;
    private int mFocusCount;
    private int mNextRetryCount;

    private List<ChapterItem> mChapters;

    private static final int MAX_PLAYLIST_CONTINUATIONS = 20;
    
    private Video mPendingVideo;
    private int mLastErrorType = -1;
    private Disposable mFormatInfoAction;
    private Disposable mMpdStreamAction;

    private final Runnable mReloadVideo = () -> getMainController().onNewVideo(getVideo());
    
    private final Runnable mRestartEngine = () -> {
        if (getPlayer() != null)
            getPlayer().restartEngine();
    };

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
    public void onEngineInitialized() {
        if (getPlayer() == null) return;

        loadVideo(Helpers.firstNonNull(mPendingVideo, getVideo()));
        getPlayer().setButtonState(R.id.action_repeat, getPlayerData().getPlaybackMode());
        mPendingVideo = null;
    }


    @Override
    public void onEngineReleased() {

        RxHelper.disposeActions(mFormatInfoAction, mMpdStreamAction);
        Utils.removeCallbacks(mReloadVideo, this::onNextClicked, mRestartEngine);

        RxHelper.disposeActions(mActions);
        mChapters = null;
        mNextSectionVideo = null;
        if (mBrowseProcessor != null)
            mBrowseProcessor.dispose();

    }

    @Override
    public void onFinish() { onEngineReleased(); }

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
                scheduleReloadVideoTimer(1_000);
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void onVideoLoaded(Video video) {

        loadSuggestions(video);

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

        openVideoInt(getPrevious());

        getPlayer().showOverlay(true);

        return true;
    }

    @Override
    public boolean onNextClicked() {
        if (getPlayer() == null || getVideo() == null) return true;

        savePosition();

        Video next = getNext();

        if (next != null) {
            next.isShuffled = getVideo().isShuffled;
            openVideoInt(next);
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

        List<Video> afterCurrent = Queue.getAllAfterCurrent();

        if (afterCurrent != null && afterCurrent.contains(item))
            item.fromQueue = true;

        openVideoInt(item);

        if (getPlayer() != null)
            getPlayer().showControls(false);
        
    }

    @Override
    public boolean onKeyDown(int keyCode) {
        if (getPlayer() == null) return false;

        Utils.removeCallbacks(mRestartEngine);

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
        if (getPlayer() == null || item == null) return;
            
        mFormatInfoAction = null;
        mMpdStreamAction = null;

        CacheManager.clear();
        
        Queue.setCurrent(item);

        mMediaItemService.getMetadataObserve(item.videoId).subscribe(mim -> {

            item.sync(mim);
        
            getPlayer().setVideo(item);
            getPlayer().resetPlayerState();
            getPlayer().showProgressBar(true);

            mFormatInfoAction = mMediaItemService.getFormatInfoObserve(item.videoId).subscribe(
                this::processFormatInfo,
                this::runFormatErrorAction
            );

        });

    }

    private void processFormatInfo(MediaItemFormatInfo formatInfo) {
        PlaybackFragment2 player = getPlayer();

        if (player == null || getVideo() == null) return;

        String bgImageUrl = null;

        getVideo().sync(formatInfo);

        if (formatInfo.isUnplayable()) {

            player.setTitle(formatInfo.getPlayabilityStatus());
            player.showProgressBar(false);
            loadSuggestions(getVideo());
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

        } else if (formatInfo.isLive() && formatInfo.containsDashUrl()) {
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
            
            if (mLastErrorType == PlayerEventListener.ERROR_TYPE_SOURCE)
                Collections.reverse(urlList);

            player.openUrlList(urlList);
            
        } else {
            Log.d(TAG, "Empty format info received. Seems future live translation. No video data to pass to the player.");
            player.setTitle(formatInfo.getPlayabilityStatus());
            player.showProgressBar(false);
            loadSuggestions(getVideo());
            bgImageUrl = getVideo().getBackgroundUrl();
            scheduleReloadVideoTimer(30_000);
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

    public void openVideo(Video item) {
        
        if (item.hasVideo() && !item.isPlaylistInChannel()) {
            mPlaybackPresenter.openVideo(item);
        
        } else if (item.hasChannel() || item.belongsToChannelUploads()) {
            chooseChannelPresenter(mContext, item);
        
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
        
        // Handle V8 signature challenge errors
        if ((message != null && message.contains("V8SignatureChallengeError")) ||
            (error.getCause() != null && error.getCause().getMessage() != null && 
             error.getCause().getMessage().contains("jsc is not a function"))) {
            Log.e(TAG, "V8 signature challenge failed - clearing cache and retrying");
            CacheManager.clear();
            applyNoPlaybackFix();
            scheduleReloadVideoTimer(5_000);
            return;
        }
        
        // ...rest of existing error handling...
    }
    
    private void runEngineErrorAction(int type, int rendererIndex, Throwable error) {

        if (getVideo() != null && getVideo().isLiveEnd) {
            // Url no longer works (e.g. live stream ended)
            getMainController().onPlayEnd();
            return;
        }

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
                applyNoPlaybackFix();
            
            } else if (getPlayer() != null && !FormatItem.SUBTITLE_NONE.equals(getPlayer().getSubtitleFormat())) {
                getPlayerData().setFormat(FormatItem.SUBTITLE_NONE); // Response code: 429
            
            } else {
                applyNoPlaybackFix(); // Response code: 403
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

        if (showMessage)
            MessageHelpers.showLongMessage(getContext(), errorMessage);

        if (restartEngine) {
            scheduleRestartEngineTimer(1_000);
        } else {
            scheduleReloadVideoTimer(1_000);
        }

    }

    private void applyNoPlaybackFix() {
        CacheManager.clear();
        getVideoInfoService().switchNextFormat();
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
                break;

        }
    }

    private boolean acceptAdaptiveFormats(MediaItemFormatInfo formatInfo) {

        boolean isLive = formatInfo.isLive();

        boolean atStart = (formatInfo.getStartTimeMs() == 0);

        return !(isLive && atStart);
    }

    @Override
    public void onMetadata(MediaItemMetadata metadata) {

        if (getPlayer() == null || getPlayerData() == null || getVideo() == null || getVideo().playlistInfo == null ||
                getPlayerData().getPlaybackMode() != PlaybackFragment2.PLAYBACK_MODE_SHUFFLE) return;

        if (getVideo().playlistInfo.getSize() != -1) {

            Video video = new Video();
            video.playlistId = getVideo().playlistId;
            video.playlistIndex = Utils.getRandomIndex(getVideo().playlistInfo.getCurrentIndex(), getVideo().playlistInfo.getSize());
            loadMetadata(video, randomMetadata -> {
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

    @Override
    public void onScrollEnd(Video item) {
        if (item == null) {
            Log.e(TAG, "Can't scroll. Video is null.");
            return;
        }

        VideoGroup group = item.getGroup();

        continueGroup(group, null, true);
    }

    @Override
    public void onControlsShown(boolean shown) {
        if (shown) focusCurrentChapter();
    }

    @Override
    public void onSeekEnd() {
        if (getPlayer() == null || !getPlayer().isControlsShown()) return;
        focusCurrentChapter();
    }

    @Override
    public void onSeekPositionChanged(long positionMs) {
        if (getPlayer() == null || !getPlayer().isControlsShown()) return;

        Pair<ChapterItem, Integer> currentChapter = getCurrentChapter(positionMs);

        if (currentChapter != null)
            getPlayer().setSeekPreviewTitle(currentChapter.first.getTitle());
        
    }

    @Override
    public void onTickle() {
        if (getPlayer() == null) return;

        Video video = getPlayer().getVideo();

        if (video == null || !video.isLive || RxHelper.isAnyActionRunning(mActions)) return;

        loadMetadata2(video, metadata -> syncCurrentVideo(metadata, video));
    
    }

    private void continueGroup(
        VideoGroup group, 
        Consumer<VideoGroup> callback, 
        boolean showLoading
    ) {
        if (group == null) return;

        Log.d(TAG, "continueGroup: start continue group: " + group.getTitle());

        if (showLoading)
            getPlayer().showProgressBar(true);

        MediaGroup mediaGroup = group.getMediaGroup();

        Disposable continueAction = mContentService.continueGroupObserve(mediaGroup).subscribe(
            
            continueMediaGroup -> {
                getPlayer().showProgressBar(false);

                VideoGroup videoGroup = VideoGroup.from(group, continueMediaGroup);
                getPlayer().updateSuggestions(videoGroup);
                mBrowseProcessor.process(videoGroup);

                if (callback == null) {
                    continueGroupIfNeeded(videoGroup);
                } else {
                    callback.accept(videoGroup);
                }
            },
            
            error -> {
                Log.e(TAG, "continueGroup error: %s", error.getMessage());
                if (getPlayer() != null)
                    getPlayer().showProgressBar(false);
            }

        );

        mActions.add(continueAction);
    }

    private void syncCurrentVideo(MediaItemMetadata mediaItemMetadata, Video video) {

        // NOTE: Skip upcoming or unplayable (no media) because default title more informative (e.g. has scheduled time).
        // NOTE: Upcoming videos metadata wrongly reported as live
        if (!getPlayer().containsMedia()) return;

        video.sync(mediaItemMetadata);
        getPlayer().setVideo(video);

        getPlayer().setNextTitle(getNext());

        if (video == null) return;

        Disposable dislikeAction = RxHelper.fromCallable(() -> getDislikeData(video.videoId)).subscribe(
            dislikeData -> {
                video.sync(dislikeData);
                getPlayer().setVideo(video);
            }
        );

        mActions.add(dislikeAction);
    }

    public void loadSuggestions(Video video) {
        if (video == null || getPlayer() == null) return;

        // Frees a lot of memory
        if (video.isRemote || !getPlayer().isSuggestionsShown()) {
            getPlayer().clearSuggestions();
        }
        
        loadMetadata2(video, metadata -> updateSuggestions(metadata, video));
    }

    private void loadMetadata2(
        Video video, 
        Consumer<MediaItemMetadata> callback
    ) {
        
        onEngineReleased();

        if (video == null) {
            Log.e(TAG, "loadSuggestions: video is null");
            return;
        }
        // NOTE: Load suggestions from mediaItem isn't robust. Because playlistId may be initialized from RemoteControlManager.
        // Video might be loaded from Channels section (has playlistParams)
        Observable<MediaItemMetadata> observable = mMediaItemService.getMetadataObserve(
            video.videoId, 
            video.getPlaylistId(), 
            video.playlistIndex, 
            video.playlistParams
        );

        Disposable metadataAction = observable.subscribe(
            callback::accept,
            e -> e.printStackTrace()
        );

        mActions.add(metadataAction);
    }

    public Video getNext() {
        Video next = Queue.getNext();
        Video current = getPlayer() != null ? getPlayer().getVideo() : null;

        if (next != null) {
            next.fromQueue = true;
            return next;
        }

        if (mNextSectionVideo != null) {
            return mNextSectionVideo;
        }

        if (current != null && current.nextMediaItem != null) {
            return Video.from(current.nextMediaItem);
        }

        // Fallback: suggestions/metadata might not be loaded yet.
        // Compute the next item from the current VideoGroup ordering.
        if (current == null) return null;

        VideoGroup group = current.getGroup();
        if (group == null || group.isEmpty()) return null;

        List<Video> videos = group.getVideos();
        if (videos == null || videos.isEmpty()) return null;

        int currentIndex = videos.indexOf(current);
        if (currentIndex < 0) {
            // If the current video isn't found in group ordering, try first upcoming-safe pick.
            for (Video v : videos) {
                if (v != null && v.hasVideo() && !v.isUpcoming) return v;
            }
            return null;
        }

        if (getPlaybackMode() == PlaybackFragment2.PLAYBACK_MODE_SHUFFLE) {
            // Random pick within the group, but prefer a non-upcoming item.
            int maxTries = Math.min(20, Math.max(1, videos.size()));
            int size = videos.size();

            for (int i = 0; i < maxTries; i++) {
                int randomIndex = Utils.getRandomIndex(currentIndex, size);
                if (randomIndex < 0 || randomIndex >= size) continue;

                Video candidate = videos.get(randomIndex);
                if (candidate != null && candidate.hasVideo() && !candidate.isUpcoming) {
                    return candidate;
                }
            }

            // As a last resort, pick the first valid item after current.
            for (int i = currentIndex + 1; i < size; i++) {
                Video candidate = videos.get(i);
                if (candidate != null && candidate.hasVideo() && !candidate.isUpcoming) return candidate;
            }

            return null;
        }

        // Non-shuffle: first playable item after current.
        for (int i = currentIndex + 1; i < videos.size(); i++) {
            Video candidate = videos.get(i);
            if (candidate != null && candidate.hasVideo() && !candidate.isUpcoming) {
                return candidate;
            }
        }

        return null;
    }

    


    public Video getPrevious() {
        Video current = getPlayer().getVideo();

        Video result = null;

        if (current != null) {
            VideoGroup group = current.getGroup();

            if (group != null && !group.isEmpty()) {
                Video previous = null;

                for (Video item : group.getVideos()) {
                    if (item.equals(current)) {
                        result = previous;
                        break;
                    }

                    if (item.hasVideo() && !item.isUpcoming) {
                        previous = item;
                    }
                }
            }
        }

        if (result == null) {
            Video previous = Queue.getPrevious();

            if (previous != null) {
                previous.fromQueue = true;
                result = previous;
            }
        }

        if (result == null) {
            Video previous = Queue.getPrevious();

            if (previous != null) {
                previous.fromQueue = true;
                result = previous;
            }
        }

        return result;
    }

    private void updateSuggestions(
        MediaItemMetadata mediaItemMetadata, 
        Video video
    ) {
        
        syncCurrentVideo(mediaItemMetadata, video);

        if (video != null 
            && getPlayer() != null 
            && video.isRemote 
            && !getPlayer().isSuggestionsShown()
        ) {

            getPlayer().clearSuggestions(); // clear previous videos

            mChapters = mediaItemMetadata.getChapters();
        
            if (mChapters != null) {

                List<SeekBarSegment> result = new ArrayList<>();
                long markLengthMs = getPlayer().getDurationMs() / 10000;

                for (ChapterItem chapter : mChapters) {

                    if (chapter.getStartTimeMs() == 0) continue;

                    SeekBarSegment seekBarSegment = new SeekBarSegment();
                    float startRatio = (float) chapter.getStartTimeMs() / getPlayer().getDurationMs(); // Range: [0, 1]
                    float endRatio = (float) (chapter.getStartTimeMs() + markLengthMs) / getPlayer().getDurationMs(); // Range: [0, 1]
                    seekBarSegment.startProgress = startRatio;
                    seekBarSegment.endProgress = endRatio;
                    seekBarSegment.color = ContextCompat.getColor(getContext(), R.color.black);
                    result.add(seekBarSegment);
                }
            
                getPlayer().setSeekBarSegments(result);
            
                VideoGroup videoGroup = VideoGroup.fromChapters(
                    mChapters, 
                    getContext().getString(R.string.chapters)
                );

                getPlayer().updateSuggestions(videoGroup);
            
            }
            
            focusCurrentChapter();

            if (video.isSectionPlaylistEnabled(getContext())) {
                getPlayer().updateSuggestions(video.getGroup());
                focusAndContinueIfNeeded(
                    video.getGroup(), 
                    () -> findNextSectionVideoIfNeeded(video)
                );
            } else {
                // Important fix. Gives priority to playlist or suggestion.
                mNextSectionVideo = null;
                return;
            }

            List<MediaGroup> suggestions = mediaItemMetadata.getSuggestions();

            if (suggestions == null) {
                Log.e(TAG, "loadSuggestions: Can't obtain suggestions for video: "+video.getTitle());
            } else {

                int groupIndex = -1;
                int suggestRows = -1;

                for (MediaGroup group : suggestions) {
                    groupIndex++;

                    if (groupIndex == suggestRows) break;

                    // Remove duplicated playlist
                    if (groupIndex == 0 && video.isSectionPlaylistEnabled(getContext()) && video.belongsToSamePlaylistGroup())
                        continue;
                    
                    if (group != null && !group.isEmpty()) {
                        VideoGroup videoGroup = VideoGroup.from(group);

                        if (TextUtils.isEmpty(videoGroup.getTitle())) {
                            videoGroup.setTitle(getContext().getString(R.string.suggestions));
                        }

                        getPlayer().updateSuggestions(videoGroup);
                        mBrowseProcessor.process(videoGroup);

                        if (groupIndex == 0) {
                            focusAndContinueIfNeeded(videoGroup, () -> {});
                        } else {
                            continueGroupIfNeeded(videoGroup);
                        }
                    }
                }

            }
        }

        if (mediaItemMetadata != null)
            getMainController().onMetadata(mediaItemMetadata);
    
    }

    private void focusCurrentChapter() {
        if (getPlayer() == null || !getPlayer().isControlsShown()) return;

        VideoGroup group = getPlayer().getSuggestionsByIndex(0);

        if (group == null || group.isEmpty() || !group.getVideos().get(0).isChapter) return;

        Pair<ChapterItem, Integer> currentChapter = getCurrentChapter(getPlayer().getPositionMs());

        if (currentChapter != null) {
            getPlayer().focusSuggestedItem(currentChapter.second);
            getPlayer().setSeekPreviewTitle(currentChapter.first.getTitle());
        }
    }

    /**
     * Most tiny ui has 8 cards in a row or 24 in grid.
     */
    private void continueGroupIfNeeded(VideoGroup group) {
        if (getPlayer() == null) return;

        if (shouldContinueRowGroup(getContext(), group)) {
            continueGroup(
                group, null,
                getPlayer().isSuggestionsShown()
            );
        }
    }

    private void focusAndContinueIfNeeded(VideoGroup group, Runnable onDone) {
        if (getPlayer() == null) return;

        Video video = getPlayer().getVideo();

        if (group == null || group.isEmpty() || video == null || !video.hasVideo()) return;

        int index = group.getVideos().indexOf(video);

        if (index >= 0) { // continuation group starts with zero index
            Log.d(TAG, "Found current video index: %s", index);
            Video found = group.getVideos().get(index);
            if (!found.isMix() || video.isSectionPlaylistEnabled(getContext())) {
                getPlayer().focusSuggestedItem(found);
            }
            mFocusCount = 0; // Stop the continuation loop
            onDone.run();
        } else if (mFocusCount > MAX_PLAYLIST_CONTINUATIONS || !video.hasPlaylist()) {
            // Stop the continuation loop. Maybe the video isn't there.
            mFocusCount = 0;
            onDone.run();
        } else {
            // load more and repeat
            continueGroup(
                group, 
                newGroup -> focusAndContinueIfNeeded(newGroup, onDone), 
                getPlayer().isSuggestionsShown()
            );
            mFocusCount++;
        }
    }

    private void findNextSectionVideoIfNeeded(Video video) {
        
        mNextSectionVideo = null;

        VideoGroup group = video.getGroup();
        if (group == null || group.isEmpty()) return;

        if (getPlayerData().getPlaybackMode() == PlaybackFragment2.PLAYBACK_MODE_SHUFFLE) {

            int currentIdx = group.indexOf(video);

            int nextIdx = Utils.getRandomIndex(currentIdx, group.getSize());

            mNextSectionVideo = group.get(nextIdx);
            getPlayer().setNextTitle(mNextSectionVideo);

        } else {

            List<Video> videos = group.getVideos();
            boolean found = false;

            for (Video current : videos) {
                if (found && current.hasVideo() && !current.isUpcoming) {
                    mNextRetryCount = 0;
                    mNextSectionVideo = current;
                    getPlayer().setNextTitle(mNextSectionVideo);
                    return;
                }

                if (current.equals(video)) {
                    found = true;
                }
            }

            if (mNextRetryCount > 0) {
                mNextRetryCount = 0;
            } else {
                continueGroup(
                    group, 
                    ig -> findNextSectionVideoIfNeeded(video), 
                    getPlayer().isSuggestionsShown()
                );
                mNextRetryCount++;
            }
        }

    }

    private Pair<ChapterItem, Integer> getCurrentChapter(long positionMs) {
        if (mChapters == null) return null;

        ChapterItem currentChapter = null;
        int idx = -1;

        for (ChapterItem chapter : mChapters) {
            
            if (chapter.getStartTimeMs() > (positionMs + 3_000)) break;
            
            currentChapter = chapter;
            idx++;
        }

        return currentChapter != null ? new Pair<>(currentChapter, idx) : null;
    }

    @Nullable
    public DislikesResult getDislikeData(@Nullable String videoId) {
        if (videoId == null) return null;

        Call<DislikesResult> wrapper = mVideoInfoApi.getDislikes(videoId);

        return (DislikesResult) RetrofitHelper.get(wrapper);
    }
    
}
