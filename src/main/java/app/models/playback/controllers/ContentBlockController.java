package SmartTubeApp.app.models.playback.controllers;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.liskovsoft.sharedutils.MediaItemService;
import SmartTubeApp.misc.ServiceManager;
import com.liskovsoft.sharedutils.data.MediaItemMetadata;
import com.liskovsoft.sharedutils.data.SponsorSegment;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import SmartTubeApp.R;
import SmartTubeApp.app.models.data.Video;
import SmartTubeApp.app.models.playback.BasePlayerController;
import SmartTubeApp.app.models.playback.ui.OptionItem;
import SmartTubeApp.app.models.playback.ui.SeekBarSegment;
import SmartTubeApp.app.models.playback.ui.UiOptionItem;
import SmartTubeApp.app.presenters.AppDialogPresenter;
import SmartTubeApp.app.presenters.settings.ContentBlockSettingsPresenter;
import SmartTubeApp.prefs.ContentBlockData;
import com.liskovsoft.sharedutils.rx.RxHelper;
import SmartTubeApp.prefs.PlayerTweaksData;
import SmartTubeApp.utils.Utils;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ContentBlockController extends BasePlayerController {
    private static final String TAG = ContentBlockController.class.getSimpleName();
    private static final long POLL_INTERVAL_MS = 1_000;
    private static final int CONTENT_BLOCK_ID = 144;
    private MediaItemService mMediaItemService;
    private List<SponsorSegment> mOriginalSegments;
    private List<SponsorSegment> mActiveSegments;
    private long mLastSkipPosMs;
    private boolean mSkipExclude;
    private Disposable mSegmentsAction;
    private Observable<List<SponsorSegment>> mCachedSegmentsAction;
    private String mVideoId;

    public static class SegmentAction {
        public String segmentCategory;
        public int actionType;

        public static SegmentAction from(String spec) {
            if (spec == null) {
                return null;
            }

            String[] split = spec.split(",");

            if (split.length != 2) {
                return null;
            }

            String name = Helpers.parseStr(split[0]);
            int action = Helpers.parseInt(split[1]);

            return from(name, action);
        }

        public static SegmentAction from(String name, int action) {
            SegmentAction blockedSegment = new SegmentAction();
            blockedSegment.segmentCategory = name;
            blockedSegment.actionType = action;

            return blockedSegment;
        }

        @NonNull
        @Override
        public String toString() {
            return String.format("%s,%s", segmentCategory, actionType);
        }
    }

    @Override
    public void onInit() {
        mMediaItemService = ServiceManager.getMediaItemService();
    }

    @Override
    public void onNewVideo(Video item) {
        mSkipExclude = false;
        if (getPlayer() != null) {
            getPlayer().setSeekBarSegments(null); // reset colors
        }
    }

    @Override
    public void onVideoLoaded(Video item) {
        
        disposeActions();

        if (getPlayer() == null) return;

        if (getContentBlockData().isSponsorBlockEnabled() && checkVideo(item) && !isChannelExcluded(item.channelId)) {
            updateSponsorSegmentsAndWatch(item);
        }

    }

    @Override
    public void onMetadata(MediaItemMetadata metadata) {
        // Disable sponsor for the live streams.
        // Fix when using remote control.
        if (!getContentBlockData().isSponsorBlockEnabled() || !checkVideo(getPlayer().getVideo())) {
            disposeActions();
        } else if (isChannelExcluded(metadata.getChannelId())) { // got channel id. check the exclusions
            disposeActions();
        }
    }

    @Override
    public void onEngineReleased() {
        disposeActions();
    }

    @Override
    public void onButtonClicked(int buttonId, int buttonState) {}

    @Override
    public void onButtonLongClicked(int buttonId, int buttonState) {}

    private boolean checkVideo(Video video) {

        return video != null;
    }

    private void updateSponsorSegmentsAndWatch(Video item) {
        if (item == null || item.videoId == null || item.isLive || getContentBlockData().getEnabledCategories().isEmpty()) {
            mActiveSegments = mOriginalSegments = null;
            mCachedSegmentsAction = null;
            return;
        }

        if (!Helpers.equals(mVideoId, item.videoId) || mCachedSegmentsAction == null) {
            // NOTE: SponsorBlock (when happened java.net.SocketTimeoutException) could block whole application with Schedulers.io()
            // Because Schedulers.io() reuses blocked threads in RxJava 2: https://github.com/ReactiveX/RxJava/issues/6542
            mCachedSegmentsAction = mMediaItemService.getSponsorSegmentsObserve(item.videoId, getContentBlockData().getEnabledCategories())
                    .cache();
            mVideoId = item.videoId;
        }

        mSegmentsAction = mCachedSegmentsAction
                .flatMap(this::startSponsorWatcher)
                .subscribe(
                        this::skipSegment,
                        error -> Log.d(TAG, "It's ok. Nothing to block in this video. Error msg: %s", error.getMessage())
                );
    }

    private Observable<Long> startSponsorWatcher(List<SponsorSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            mActiveSegments = mOriginalSegments = null;
            return Observable.empty();
        }

        mOriginalSegments = segments;

        mActiveSegments = new ArrayList<>(segments);

        if (getContentBlockData().isColorMarkersEnabled()) {
            getPlayer().setSeekBarSegments(toSeekBarSegments(segments));
        }
        if (getContentBlockData().isActionsEnabled()) {
            // Warn. Try to not access player object here.
            // Or you'll get "Player is accessed on the wrong thread" error.
            return RxHelper.interval(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
        } else {
            return Observable.empty();
        }
    }

    private void disposeActions() {
        RxHelper.disposeActions(mSegmentsAction);

        // Reset previously found segment (fix no dialog popup)
        mLastSkipPosMs = 0;
    }

    private void skipSegment(long interval) {
        if (mActiveSegments == null || mActiveSegments.isEmpty() || getVideo() == null || !Helpers.equals(mVideoId, getVideo().videoId)) {
            disposeActions();
            return;
        }

        // Fix looping messages at the end of the video (playback mode: pause at the end of the video)
        if (!getPlayer().isPlaying()) {
            return;
        }

        long positionMs = getPlayer().getPositionMs();

        List<SponsorSegment> foundSegments = findMatchedSegments(positionMs, mActiveSegments, false);

        applyActions(foundSegments);

        // Skip each segment only once
        if (foundSegments != null) {
            mActiveSegments.removeAll(foundSegments);
        }
    }

    private boolean isPositionInsideSegment(long positionMs, SponsorSegment segment, boolean fullMatch) {

        if (fullMatch) {
            return positionMs >= segment.getStartMs() && positionMs <= segment.getEndMs();
        } else {
            long windowSizeMs = (long) (2_000 * getPlayer().getSpeed());
            return positionMs >= segment.getStartMs() && positionMs <= Math.min(segment.getStartMs() + windowSizeMs, segment.getEndMs());
        }
    }

    private void messageSkip(long skipPosMs, String category) {
        if (mLastSkipPosMs == skipPosMs) return;

        MessageHelpers.showMessage(
            getContext(),
            getContext().getString(
                R.string.msg_skipping_segment, 
                category
            )
        );
    
        setPositionMs(skipPosMs);
        
        closeTransparentDialog();
    
    }

    private List<SeekBarSegment> toSeekBarSegments(List<SponsorSegment> segments) {
        if (segments == null) {
            return null;
        }

        List<SeekBarSegment> result = new ArrayList<>();

        for (SponsorSegment sponsorSegment : segments) {
            if (!getContentBlockData().isColorMarkerEnabled(sponsorSegment.getCategory())) {
                continue;
            }

            SeekBarSegment seekBarSegment = new SeekBarSegment();
            float startRatio = (float) sponsorSegment.getStartMs() / getPlayer().getDurationMs(); // Range: [0, 1]
            float endRatio = (float) sponsorSegment.getEndMs() / getPlayer().getDurationMs(); // Range: [0, 1]
            seekBarSegment.startProgress = startRatio;
            seekBarSegment.endProgress = endRatio;
            seekBarSegment.color = ContextCompat.getColor(getContext(), getContentBlockData().getColorRes(sponsorSegment.getCategory()));
            result.add(seekBarSegment);
        }

        return result;
    }

    /**
     * Sponsor block fix. Position may exceed real media length.
     */
    private void setPositionMs(long positionMs) {
        long durationMs = getPlayer().getDurationMs();

        // Sponsor block fix. Position may exceed real media length.
        getPlayer().setPositionMs(Math.min(positionMs, durationMs));
    }

    /**
     * @param fullMatch Match only the beginning or the full segment length
     */
    private List<SponsorSegment> findMatchedSegments(long positionMs, List<SponsorSegment> segments, boolean fullMatch) {
        if (segments == null) {
            return null;
        }

        List<SponsorSegment> foundSegment = null;

        for (SponsorSegment segment : segments) {
            int action = getContentBlockData().getAction(segment.getCategory());
            boolean isSkipAction = (action == ContentBlockData.ACTION_SKIP_WITH_TOAST);

            if (foundSegment == null) {
                if (isPositionInsideSegment(positionMs, segment, fullMatch)) {
                    foundSegment = new ArrayList<>();
                    foundSegment.add(segment);

                    // Action grouping aren't supported for dialogs
                    if (!isSkipAction) {
                        break;
                    }
                }
            } else {
                SponsorSegment lastSegment = foundSegment.get(foundSegment.size() - 1);
                if (isSkipAction && isPositionInsideSegment(lastSegment.getEndMs() + 3_000, segment, fullMatch)) {
                    foundSegment.add(segment);
                }
            }
        }

        return foundSegment;
    }

    private void applyActions(List<SponsorSegment> foundSegments) {
        if (foundSegments == null) {
            mLastSkipPosMs = 0;
            return;
        }

        SponsorSegment lastSegment = foundSegments.get(foundSegments.size() - 1);

        Integer resId = getContentBlockData().getLocalizedRes(lastSegment.getCategory());
        String skipMessage = resId != null ? getContext().getString(resId) : lastSegment.getCategory();

        int type = getContentBlockData().getAction(lastSegment.getCategory());

        long skipPosMs = lastSegment.getEndMs();
        // Fix infinite skip loop by ignoring short segments. TextureView has a seek bug.
        long skipDurationMs = Math.min(skipPosMs, getPlayer().getDurationMs()) - getPlayer().getPositionMs();

        if (skipDurationMs >= 10_000) {
            messageSkip(skipPosMs, skipMessage);
        }

        mLastSkipPosMs = skipPosMs;
    }

    private void closeTransparentDialog() {
        AppDialogPresenter dialogPresenter = AppDialogPresenter.instance(getContext());

        if (dialogPresenter.isDialogShown() && dialogPresenter.getId() == CONTENT_BLOCK_ID) {
            dialogPresenter.closeDialog();
        }
    }

    private boolean isChannelExcluded(String channelId) {
        return !mSkipExclude && getContentBlockData().isChannelExcluded(channelId);
    }
}
