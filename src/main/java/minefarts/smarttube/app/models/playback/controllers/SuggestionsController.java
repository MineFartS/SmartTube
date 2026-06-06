package minefarts.smarttube.app.models.playback.controllers;

import android.text.TextUtils;
import android.util.Pair;

import androidx.core.content.ContextCompat;

import minefarts.smarttube.utils.service.ContentService;
import minefarts.smarttube.utils.MediaItemService;
import minefarts.smarttube.utils.data.ChapterItem;
import minefarts.smarttube.utils.data.DislikeData;
import minefarts.smarttube.utils.service.data.MediaGroup;
import minefarts.smarttube.utils.service.data.MediaItemMetadata;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.helpers.MessageHelpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.rx.RxHelper;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.Queue;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.data.VideoGroup;
import minefarts.smarttube.app.models.playback.BasePlayerController;
import minefarts.smarttube.ui.playback.PlaybackFragment2;
import minefarts.smarttube.app.models.playback.ui.SeekBarSegment;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.app.presenters.PlaybackPresenter;
import minefarts.smarttube.utils.BrowseProcessorManager;
import minefarts.smarttube.utils.ServiceManager;
import minefarts.smarttube.prefs.GeneralData;
import minefarts.smarttube.utils.Utils;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.util.ArrayList;
import java.util.List;

public class SuggestionsController extends BasePlayerController {

    private static final String TAG = SuggestionsController.class.getSimpleName();

    private final List<Disposable> mActions = new ArrayList<>();

    private MediaItemService mMediaItemService;
    private ContentService mContentService;
    private BrowseProcessorManager mBrowseProcessor;
    private Video mNextSectionVideo;
    private int mFocusCount;
    private int mNextRetryCount;

    private List<ChapterItem> mChapters;

    private static final int MAX_PLAYLIST_CONTINUATIONS = 20;

    private interface OnVideoGroup {
        void onVideoGroup(VideoGroup group);
    }

    private interface OnMetadata {
        void onMetadata(MediaItemMetadata metadata);
    }

    @Override
    public void onInit() {
        mBrowseProcessor = new BrowseProcessorManager(
            getContext(), 
            PlaybackPresenter.instance(getContext())::syncItem
        );
        mMediaItemService = ServiceManager.getMediaItemService();
        mContentService = ServiceManager.getContentService();
    }

    @Override
    public void onNewVideo(Video video) {
        // Remote control fix. Slow network fix. Suggestions may still be loading.
        // This could lead to changing current video info (title, id etc) to wrong one.
        onEngineReleased();
    }

    /**
     * Improve video load time by running a fetch after load event
     */
    @Override
    public void onVideoLoaded(Video item) {
        loadSuggestions(item);
    }

    @Override
    public void onEngineReleased() {
        RxHelper.disposeActions(mActions);
        mChapters = null;
        mNextSectionVideo = null;
        if (mBrowseProcessor != null) {
            mBrowseProcessor.dispose();
        }
    }

    @Override
    public void onFinish() {
        onEngineReleased();
    }

    @Override
    public void onScrollEnd(Video item) {
        if (item == null) {
            Log.e(TAG, "Can't scroll. Video is null.");
            return;
        }

        VideoGroup group = item.getGroup();

        continueGroup(group);
    }

    @Override
    public void onSuggestionItemClicked(Video item) {
        
        List<Video> afterCurrent = Queue.getAllAfterCurrent();

        if (afterCurrent != null && afterCurrent.contains(item)) {
            item.fromQueue = true;
        }

    }

    @Override
    public void onControlsShown(boolean shown) {
        if (shown) {
            focusCurrentChapter();
        }
    }

    @Override
    public void onSeekEnd() {

        if (getPlayer() == null) {
            return;
        }

        if (getPlayer().isControlsShown()) {
            focusCurrentChapter();
        }

    }

    @Override
    public void onSeekPositionChanged(long positionMs) {
        if (getPlayer().isControlsShown()) {
            updateSeekPreviewTitle(positionMs);
        }
    }

    @Override
    public void onTickle() {
        if (getPlayer() == null) return;

        Video video = getPlayer().getVideo();

        if (video == null || !video.isLive || RxHelper.isAnyActionRunning(mActions)) return;

        loadMetadata2(video, metadata -> syncCurrentVideo(metadata, video));
    
    }

    private void continueGroup(VideoGroup group) {
        continueGroup(group, null, true);
    }

    private void continueGroup(VideoGroup group, boolean showLoading) {
        continueGroup(group, null, showLoading);
    }

    private void continueGroup(VideoGroup group, OnVideoGroup callback, boolean showLoading) {
        if (group == null) {
            Log.e(TAG, "Can't continue group. The group is null.");
            return;
        }

        Log.d(TAG, "continueGroup: start continue group: " + group.getTitle());

        if (showLoading) {
            getPlayer().showProgressBar(true);
        }

        MediaGroup mediaGroup = group.getMediaGroup();

        Disposable continueAction = mContentService.continueGroupObserve(mediaGroup)
                .subscribe(
                        continueMediaGroup -> {
                            getPlayer().showProgressBar(false);

                            VideoGroup videoGroup = VideoGroup.from(group, continueMediaGroup);
                            getPlayer().updateSuggestions(videoGroup);
                            mBrowseProcessor.process(videoGroup);

                            if (callback != null) {
                                callback.onVideoGroup(videoGroup);
                            } else {
                                continueGroupIfNeeded(videoGroup);
                            }
                        },
                        error -> {
                            Log.e(TAG, "continueGroup error: %s", error.getMessage());
                            if (getPlayer() != null) {
                                getPlayer().showProgressBar(false);
                            }
                        }
                );

        mActions.add(continueAction);
    }

    private void syncCurrentVideo(MediaItemMetadata mediaItemMetadata, Video video) {

        // NOTE: Skip upcoming or unplayable (no media) because default title more informative (e.g. has scheduled time).
        // NOTE: Upcoming videos metadata wrongly reported as live
        if (!getPlayer().containsMedia()) {
            return;
        }

        video.sync(mediaItemMetadata);
        getPlayer().setVideo(video);

        getPlayer().setNextTitle(getNext());

        appendDislikes(video);
    }

    public void loadSuggestions(Video video) {
        if (isEmbedPlayer()) return;

        clearSuggestionsIfNeeded(video);
        loadMetadata2(video, metadata -> updateSuggestions(metadata, video));
    }

    private void loadMetadata2(Video video, OnMetadata callback) {
        
        onEngineReleased();

        if (video == null) {
            Log.e(TAG, "loadSuggestions: video is null");
            return;
        }

        Observable<MediaItemMetadata> observable;

        // NOTE: Load suggestions from mediaItem isn't robust. Because playlistId may be initialized from RemoteControlManager.
        // Video might be loaded from Channels section (has playlistParams)
        observable = mMediaItemService.getMetadataObserve(video.videoId, video.getPlaylistId(), video.playlistIndex, video.playlistParams);

        Disposable metadataAction = observable
                .subscribe(
                        callback::onMetadata,
                        error -> {
                            // Usual errors here is something with title parsing
                            String message = error.getMessage();
                            Log.e(TAG, "loadSuggestions error: %s", message);
                            if (!Helpers.containsAny(message, "fromNullable result is null")) {
                                MessageHelpers.showLongMessage(getContext(), "loadSuggestions error: %s", message);
                            }
                            error.printStackTrace();
                        }
                );

        mActions.add(metadataAction);
    }

    public Video getNext() {
        Video result = null;
        Video next = Queue.getNext();
        Video current = getPlayer().getVideo();

        if (next != null) {
            next.fromQueue = true;
            result = next;
        } else if (mNextSectionVideo != null) {
            result = mNextSectionVideo;
        } else if (current != null && current.nextMediaItem != null) {
            result = Video.from(current.nextMediaItem);
        }

        return result;
    }

    public Video getPrevious() {
        Video result = getPreviousFromGroup(getPlayer().getVideo());

        if (result == null) {
            Video previous = Queue.getPrevious();

            if (previous != null) {
                previous.fromQueue = true;
                result = previous;
            }
        }

        return result;
    }

    private Video getPreviousFromGroup(Video current) {
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

        return result;
    }

    private void clearSuggestionsIfNeeded(Video video) {
        if (video == null || getPlayer() == null) {
            return;
        }

        // Frees a lot of memory
        if (video.isRemote || !getPlayer().isSuggestionsShown()) {
            getPlayer().clearSuggestions();
        }
    }

    private void updateSuggestions(MediaItemMetadata mediaItemMetadata, Video video) {
        syncCurrentVideo(mediaItemMetadata, video);

        appendSuggestions(video, mediaItemMetadata);

        // After video suggestions
        callListener(mediaItemMetadata);
    }

    private void appendSuggestions(Video video, MediaItemMetadata mediaItemMetadata) {
        if (video == null || getPlayer() == null) {
            return;
        }

        if (!video.isRemote && getPlayer().isSuggestionsShown()) {
            Log.d(TAG, "Suggestions is opened. Seems that user want to stay here.");
            return;
        }

        getPlayer().clearSuggestions(); // clear previous videos

        appendChaptersIfNeeded(mediaItemMetadata);

        appendSectionPlaylistIfNeeded(video);

        List<MediaGroup> suggestions = mediaItemMetadata.getSuggestions();

        if (suggestions == null) {
            String msg = "loadSuggestions: Can't obtain suggestions for video: " + video.getTitle();
            Log.e(TAG, msg);
            return;
        }

        int groupIndex = -1;
        int suggestRows = -1;

        for (MediaGroup group : suggestions) {
            groupIndex++;

            if (groupIndex == suggestRows) {
                break;
            }

            // Remove duplicated playlist
            if (groupIndex == 0 && video.isSectionPlaylistEnabled(getContext()) && video.belongsToSamePlaylistGroup()) {
                continue;
            }

            if (group != null && !group.isEmpty()) {
                VideoGroup videoGroup = VideoGroup.from(group);

                if (TextUtils.isEmpty(videoGroup.getTitle())) {
                    videoGroup.setTitle(getContext().getString(R.string.suggestions));
                }

                getPlayer().updateSuggestions(videoGroup);
                mBrowseProcessor.process(videoGroup);

                if (groupIndex == 0) {
                    focusAndContinueIfNeeded(videoGroup);
                } else {
                    continueGroupIfNeeded(videoGroup);
                }
            }
        }
    }
    private void addChapterMarkersIfNeeded() {
        if (getPlayer() == null || mChapters == null) {
            return;
        }

        getPlayer().setSeekBarSegments(toSeekBarSegments(mChapters));
    }

    private void appendChapterSuggestionsIfNeeded() {
        if (getPlayer() == null || mChapters == null) {
            return;
        }

        VideoGroup videoGroup = VideoGroup.fromChapters(mChapters, getContext().getString(R.string.chapters));

        getPlayer().updateSuggestions(videoGroup);
    }

    private void appendChaptersIfNeeded(MediaItemMetadata mediaItemMetadata) {
        mChapters = mediaItemMetadata.getChapters();
        
        addChapterMarkersIfNeeded();
        appendChapterSuggestionsIfNeeded();
        focusCurrentChapter();
    }

    private void appendSectionPlaylistIfNeeded(Video video) {
        if (getPlayer() == null) {
            return;
        }

        if (!video.isSectionPlaylistEnabled(getContext())) {
            // Important fix. Gives priority to playlist or suggestion.
            mNextSectionVideo = null;
            return;
        }

        getPlayer().updateSuggestions(video.getGroup());
        focusAndContinueIfNeeded(video.getGroup(), () -> findNextSectionVideoIfNeeded(video));
    }

    private void focusCurrentChapter() {
        if (getPlayer() == null || !getPlayer().isControlsShown()) {
            return;
        }

        VideoGroup group = getPlayer().getSuggestionsByIndex(0);

        if (group == null || group.isEmpty() || !group.getVideos().get(0).isChapter) {
            return;
        }

        Pair<ChapterItem, Integer> currentChapter = getCurrentChapter();

        if (currentChapter != null) {
            getPlayer().focusSuggestedItem(currentChapter.second);
            getPlayer().setSeekPreviewTitle(currentChapter.first.getTitle());
        }
    }

    private void updateSeekPreviewTitle(long positionMs) {
        if (getPlayer() == null || !getPlayer().isControlsShown()) {
            return;
        }

        Pair<ChapterItem, Integer> currentChapter = getCurrentChapter(positionMs);

        if (currentChapter != null) {
            getPlayer().setSeekPreviewTitle(currentChapter.first.getTitle());
        }
    }

    private List<SeekBarSegment> toSeekBarSegments(List<ChapterItem> chapters) {
        if (chapters == null) {
            return null;
        }

        List<SeekBarSegment> result = new ArrayList<>();
        long markLengthMs = getPlayer().getDurationMs() / 10000;

        for (ChapterItem chapter : chapters) {
            if (chapter.getStartTimeMs() == 0) {
                continue;
            }

            SeekBarSegment seekBarSegment = new SeekBarSegment();
            float startRatio = (float) chapter.getStartTimeMs() / getPlayer().getDurationMs(); // Range: [0, 1]
            float endRatio = (float) (chapter.getStartTimeMs() + markLengthMs) / getPlayer().getDurationMs(); // Range: [0, 1]
            seekBarSegment.startProgress = startRatio;
            seekBarSegment.endProgress = endRatio;
            seekBarSegment.color = ContextCompat.getColor(getContext(), R.color.black);
            result.add(seekBarSegment);
        }

        return result;
    }

    /**
     * Most tiny ui has 8 cards in a row or 24 in grid.
     */
    private void continueGroupIfNeeded(VideoGroup group) {
        if (getPlayer() == null) {
            return;
        }

        if (ServiceManager.shouldContinueRowGroup(getContext(), group)) {
            continueGroup(group, getPlayer().isSuggestionsShown());
        }
    }

    private void focusAndContinueIfNeeded(VideoGroup group) {
       focusAndContinueIfNeeded(group, () -> {});
    }

    private void focusAndContinueIfNeeded(VideoGroup group, Runnable onDone) {
        if (getPlayer() == null) {
            return;
        }

        Video video = getPlayer().getVideo();

        if (group == null || group.isEmpty() || video == null || !video.hasVideo()) {
            return;
        }

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
            continueGroup(group, newGroup -> focusAndContinueIfNeeded(newGroup, onDone), getPlayer().isSuggestionsShown());
            mFocusCount++;
        }
    }

    private void findNextSectionVideoIfNeeded(Video video) {
        if (getPlayerData().getPlaybackMode() == PlaybackFragment2.PLAYBACK_MODE_SHUFFLE) {
            findRandomSectionVideo(video);
        } else {
            findNextSectionVideo(video);
        }
    }

    private void findRandomSectionVideo(Video video) {
        mNextSectionVideo = null;

        VideoGroup group = video.getGroup();

        if (group == null || group.isEmpty()) {
            return;
        }

        int currentIdx = group.indexOf(video);

        int nextIdx = Utils.getRandomIndex(currentIdx, group.getSize());

        mNextSectionVideo = group.get(nextIdx);
        getPlayer().setNextTitle(mNextSectionVideo);
    }

    private void findNextSectionVideo(Video video) {
        mNextSectionVideo = null;

        VideoGroup group = video.getGroup();

        if (group == null || group.isEmpty()) {
            return;
        }

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
            continueGroup(group, continuation -> findNextSectionVideoIfNeeded(video), getPlayer().isSuggestionsShown());
            mNextRetryCount++;
        }
    }

    private Pair<ChapterItem, Integer> getCurrentChapter() {
        if (getPlayer() == null || mChapters == null) {
            return null;
        }

        return getCurrentChapter(getPlayer().getPositionMs());
    }

    private Pair<ChapterItem, Integer> getCurrentChapter(long positionMs) {
        if (mChapters == null) {
            return null;
        }

        ChapterItem currentChapter = null;
        int idx = -1;

        for (ChapterItem chapter : mChapters) {
            if (chapter.getStartTimeMs() > (positionMs + 3_000)) {
                break;
            }
            currentChapter = chapter;
            idx++;
        }

        return currentChapter != null ? new Pair<>(currentChapter, idx) : null;
    }

    private void callListener(MediaItemMetadata mediaItemMetadata) {
        if (mediaItemMetadata != null) {
            getMainController().onMetadata(mediaItemMetadata);
        }
    }

    private void appendDislikes(Video video) {
        if (video == null) {
            return;
        }

        Observable<DislikeData> dislikeDataObserve = mMediaItemService.getDislikeDataObserve(video.videoId);

        Disposable dislikeAction = dislikeDataObserve.subscribe(
                dislikeData -> {
                    video.sync(dislikeData);
                    getPlayer().setVideo(video);
                },
                error -> Log.e(TAG, "Dislike not working...")
        );

        mActions.add(dislikeAction);
    }
}
