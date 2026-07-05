package minefarts.smarttube.app.models.playback.controllers;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import minefarts.smarttube.utils.MediaItemService;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;
import com.liskovsoft.mediaserviceinterfaces.data.SponsorSegmentImpl;
import com.liskovsoft.mediaserviceinterfaces.data.SponsorSegment;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.helpers.MessageHelpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.playback.BasePlayerController;
import minefarts.smarttube.app.models.playback.ui.SeekBarSegment;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.app.presenters.settings.ContentBlockSettingsPresenter;
import minefarts.smarttube.prefs.ContentBlockData;
import minefarts.smarttube.utils.rx.RxHelper;
import minefarts.smarttube.prefs.PlayerTweaksData;
import minefarts.smarttube.utils.Utils;
import minefarts.smarttube.utils.block.SponsorBlockApi;
import minefarts.smarttube.utils.prefs.GlobalPreferences;
import minefarts.smarttube.utils.block.data.SegmentList;
import com.liskovsoft.googlecommon.common.helpers.RetrofitHelper;
import com.liskovsoft.googlecommon.common.helpers.ServiceHelper;
import minefarts.smarttube.utils.block.data.Segment;

import retrofit2.Call;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Set;

public class ContentBlockController extends BasePlayerController {

    private static final String TAG = ContentBlockController.class.getSimpleName();
        
    private MediaItemService mMediaItemService;
    private SponsorBlockApi mSponsorBlockApi;

    private List<SponsorSegment> mOriginalSegments;
    private List<SponsorSegment> mActiveSegments;
    private long mLastSkipPosMs;
    private boolean mSkipExclude;
    private Disposable mSegmentsAction;

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
        mMediaItemService = getMediaItemService();
        mSponsorBlockApi = RetrofitHelper.create(SponsorBlockApi.class);
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
        
        onEngineReleased();

        if (getPlayer() == null
            || !getContentBlockData().isSponsorBlockEnabled()
            || item == null
            || isChannelExcluded(item.channelId)
        ) return;
        
        if (item == null 
            || item.videoId == null 
            || item.isLive 
            || getContentBlockData().getEnabledCategories().isEmpty()
        ) {
            mActiveSegments = null;
            mOriginalSegments = null;
            return;
        }
            
        mVideoId = item.videoId;

        mSegmentsAction = RxHelper.fromCallable(this::loadSegments)
            .flatMap(this::startSponsorWatcher)
            .subscribe(
                this::skipSegment,
                error -> Log.d(TAG, "It's ok. Nothing to block in this video. Error msg: %s", error.getMessage())
            );
        
    }

    private List<SponsorSegment> loadSegments() {

        SegmentList segments;

        try {
            
            Call<SegmentList> wrapper = mSponsorBlockApi.getSegments(
                mVideoId,
                ServiceHelper.toJsonArrayString(
                    getContentBlockData().getEnabledCategories()
                )
            );
            
            segments = RetrofitHelper.get(wrapper);
            
        } catch (Throwable e) {
            segments = null;
        }

        if (segments == null || segments.mSegments == null)
            return null;

        List<SponsorSegment> result = new ArrayList<>();

        for (Segment segment : segments.mSegments) {
            SponsorSegment sponsorSegment = new SponsorSegmentImpl();
            ((SponsorSegmentImpl) sponsorSegment).mStartMs = (long) (segment.mStart * 1_000);
            ((SponsorSegmentImpl) sponsorSegment).mEndMs = (long) (segment.mEnd * 1_000);
            ((SponsorSegmentImpl) sponsorSegment).mCategory = segment.mCategory;
            ((SponsorSegmentImpl) sponsorSegment).mAction = segment.mActionType;
            result.add(sponsorSegment);
        }

        return result;
    }

    @Override
    public void onMetadata(MediaItemMetadata metadata) {
        // Disable sponsor for the live streams.
        // Fix when using remote control.
        if (!getContentBlockData().isSponsorBlockEnabled() 
            || getPlayer().getVideo() == null
            || isChannelExcluded(metadata.getChannelId())
        ) onEngineReleased();
        
    }

    @Override
    public void onEngineReleased() {
        RxHelper.disposeActions(mSegmentsAction);

        // Reset previously found segment (fix no dialog popup)
        mLastSkipPosMs = 0;
    }

    private Observable<Long> startSponsorWatcher(List<SponsorSegment> segments) {

        if (segments == null || segments.isEmpty()) {
            mActiveSegments = null;
            mOriginalSegments = null;
            return Observable.empty();
        }

        mOriginalSegments = segments;

        mActiveSegments = new ArrayList<>(segments);

        if (getContentBlockData().isColorMarkersEnabled())
            getPlayer().setSeekBarSegments(toSeekBarSegments(segments));

        return RxHelper.interval(1_000, TimeUnit.MILLISECONDS);
    }

    private void skipSegment(long interval) {
        if (mActiveSegments == null 
            || mActiveSegments.isEmpty() 
            || getVideo() == null 
            || !Helpers.equals(mVideoId, getVideo().videoId)
        ) {
            onEngineReleased();
            return;
        }

        // Fix looping messages at the end of the video (playback mode: pause at the end of the video)
        if (!getPlayer().isPlaying()) return;

        long positionMs = getPlayer().getPositionMs();

        List<SponsorSegment> foundSegments = null;

        if (mActiveSegments != null) {

            for (SponsorSegment segment : mActiveSegments) {
                int action = getContentBlockData().getAction(segment.getCategory());
                boolean isSkipAction = (action == ContentBlockData.ACTION_SKIP_WITH_TOAST);

                if (foundSegments == null) {
                    if (isPositionInsideSegment(positionMs, segment)) {
                        foundSegments = new ArrayList<>();
                        foundSegments.add(segment);

                        if (!isSkipAction) break; // Action grouping aren't supported for dialogs
                    }
                } else {
                    SponsorSegment lastSegment = foundSegments.get(foundSegments.size() - 1);
                    if (isSkipAction && isPositionInsideSegment(lastSegment.getEndMs()+3_000, segment)) {
                        foundSegments.add(segment);
                    }
                }
            }

        }

        applyActions(foundSegments);

        // Skip each segment only once
        if (foundSegments != null)
            mActiveSegments.removeAll(foundSegments);
        
    }

    private boolean isPositionInsideSegment(long positionMs, SponsorSegment segment) {
        return positionMs >= segment.getStartMs() 
            && positionMs <= Math.min(
                segment.getStartMs() + (long) (2_000 * getPlayer().getSpeed()), 
                segment.getEndMs()
            );
    }

    private List<SeekBarSegment> toSeekBarSegments(List<SponsorSegment> segments) {
        if (segments == null) return null;

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

    private void applyActions(List<SponsorSegment> foundSegments) {
        if (foundSegments == null) {
            mLastSkipPosMs = 0;
            return;
        }

        SponsorSegment lastSegment = foundSegments.get(foundSegments.size() - 1);

        int type = getContentBlockData().getAction(lastSegment.getCategory());

        long skipPosMs = lastSegment.getEndMs();
        // Fix infinite skip loop by ignoring short segments. TextureView has a seek bug.
        long skipDurationMs = Math.min(skipPosMs, getPlayer().getDurationMs()) - getPlayer().getPositionMs();

        if (skipDurationMs >= 10_000 && mLastSkipPosMs != skipPosMs) {

            MessageHelpers.showMessage(
                getContext(),
                getContext().getString(
                    R.string.msg_skipping_segment, 
                    lastSegment.getCategory()
                )
            );
        
            setPositionMs(skipPosMs);

            AppDialogPresenter dialogPresenter = AppDialogPresenter.instance(getContext());

            if (dialogPresenter.isDialogShown() && dialogPresenter.getId() == 144)
                dialogPresenter.closeDialog();

        }

        mLastSkipPosMs = skipPosMs;
    }

    private boolean isChannelExcluded(String channelId) {
        return !mSkipExclude && getContentBlockData().isChannelExcluded(channelId);
    }

}
