package minefarts.smarttube.app.models.playback.controllers;

import minefarts.smarttube.utils.service.data.MediaItemMetadata;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.Queue;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.playback.BasePlayerController;
import minefarts.smarttube.utils.data.MediaItemFormatInfo;
import minefarts.smarttube.utils.videoinfo.models.VideoInfo;
import minefarts.smarttube.app.models.playback.service.VideoStateService;
import minefarts.smarttube.app.models.playback.service.State;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.exoplayer.selector.FormatItem;
import minefarts.smarttube.prefs.GeneralData;
import minefarts.smarttube.utils.AppDialogUtil;
import minefarts.smarttube.utils.Utils;
import minefarts.smarttube.utils.service.internal.MediaServiceData;
import minefarts.smarttube.google.common.helpers.RetrofitHelper;
import minefarts.smarttube.app.models.playback.ui.OptionCategory;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class VideoStateController extends BasePlayerController {

    private static final String TAG = VideoStateController.class.getSimpleName();

    private static final long RESTORE_LIVE_BUFFER_MS = 60_000;
    private static final long LIVE_BUFFER_MS = 15_000;
    private static final long LIVE_THRESH_MS = LIVE_BUFFER_MS + 5_000;

    private boolean mIsPlayEnabled;
    private boolean mIsPlayBlocked;
    private long mNewVideoTimeMs;

    private static float mPositionSec;
    private static String mVideoId;

    private Disposable mFormatInfoUpdateDisposable;

    private final TrackingApi mTrackingApi;

    public VideoStateController() {
        mTrackingApi = RetrofitHelper.create(TrackingApi.class);
    }

    /**
     * Fired after user clicked on video in browse activity<br/>
     * or video is opened from the intent
     */
    @Override
    public void onNewVideo(Video item) {

        // Ensure that we aren't running on presenter init stage
        if (getPlayer() != null && getPlayer().containsMedia()) {
            onTickle();
        }

        if (!item.equals(getVideo())) {
            mNewVideoTimeMs = System.currentTimeMillis();
        }

        setPlayEnabled(true); // video just added

        getPlayerData().setTempVideoFormat(null);

        // Don't do reset on videoLoaded state because this will influences minimized music videos.
        if (getStateService() != null && item != null) {

            State state = getStateService().getByVideoId(item.videoId);

            boolean isVideoEnded = 
                state != null 
                && (state.durationMs - state.positionMs) < 3_000;

            boolean isLive = item.isLive;

            if (getPlayerTweaksData().isRememberPositionOfLiveVideosEnabled() && item.isFullLive()) {
                isLive = false;
            }

            if (isVideoEnded || isLive) {
                item.markNotViewed();
                updateHistory(item, 0);
            }

        }

    }

    @Override
    public boolean onPreviousClicked() {
        // Seek to the start on prev
        if (getPlayer() != null && getPlayer().getPositionMs() > 10_000) {
            onTickle(); // in case the user wants to go to previous video
            getPlayer().setPositionMs(100);
            return true;
        }

        // Pass to others handlers
        return false;
    }

    @Override
    public boolean onNextClicked() {

        // Seek to the actual live position on next
        if (getVideo() != null && getPlayer() != null
                && getVideo().isLive && (getPlayer().getDurationMs() - getPlayer().getPositionMs() > LIVE_THRESH_MS)) {
            getPlayer().setPositionMs(getPlayer().getDurationMs() - LIVE_BUFFER_MS);
            return true;
        }

        setPlayEnabled(true);

        onTickle();

        if (getVideo() != null && getVideo().nextMediaItem != null) {
            
            Video item = Video.from(getVideo().nextMediaItem);
            
            item.markNotViewed();
            updateHistory(item, 0);
        
        }

        return false;
    }

    @Override
    public void onSuggestionItemClicked(Video item) {
        setPlayEnabled(true); // autoplay video from suggestions

        onTickle();
    }

    @Override
    public void onEngineInitialized() {
        // Show user info instead of black screen.
        if (!getPlayEnabled()) {
            getPlayer().showOverlay(true);
        }

    }

    @Override
    public void onEngineReleased() {
        if (getPlayer() == null) return;
        
        // Save previous state
        if (getPlayer().containsMedia()) {
            setPlayEnabled(getPlayer().getPlayWhenReady());
            onTickle();
        }
    }

    @Override
    public void onTickle() {

        if (getPlayer() == null 
            || getVideo() == null
            || getVideo().percentWatched < 1
        ) return;

        updateHistory(
            getVideo(), 
            getPlayer().getPositionMs()
        );

    }
            
    @Override
    public void onMetadata(MediaItemMetadata metadata) {
        // Channel info should be loaded at this point
        restoreSubtitleFormat();

        // Need to contain channel id
        onBuffering();

        // NOTE: needed for the restore after oom crash?
        onTickle(); // start watching?
    }

    @Override
    public void onEngineError(int type, int rendererIndex, Throwable error) {
        if (getPlayer() == null) {
            return;
        }

        // Oops. Error happens while playing (network lost etc).
        if (getPlayer().getPositionMs() > 1_000) {
            onTickle();
        }
    }

    @Override
    public void onVideoLoaded(Video item) {
        // In this state video length is not undefined.
        if (getPlayer() == null|| item == null) return;

        State state = getStateService().getByVideoId(item.videoId);

        // Set actual position for live videos with uncommon length
        if (item.isLive && (state == null || state.durationMs - state.positionMs < Math.max(RESTORE_LIVE_BUFFER_MS, LIVE_THRESH_MS))) {
            // Add buffer. Should I take into account segment offset???
            state = new State(item, getPlayer().getDurationMs() - LIVE_BUFFER_MS);
        }

        // Do I need to check that item isn't live? (state != null && !item.isLive)
        if (state != null) {
            getPlayer().setPositionMs(state.positionMs);
        }

        if (!mIsPlayBlocked) {
            getPlayer().setPlayWhenReady(getPlayEnabled());
        }

        if (item.pendingPosMs > 0) {
            getPlayer().setPositionMs(item.pendingPosMs);
            item.pendingPosMs = 0;
        }

        float newVolume = getVideo().volume;

        if (getVideo().isShorts) {
            newVolume /= 2;
        }

        getPlayer().setVolume(newVolume);

    }

    @Override
    public void onPlay() {
        setPlayEnabled(true);
    }

    @Override
    public void onPause() {
        setPlayEnabled(false);
        onTickle();
    }

    @Override
    public void onTrackSelected(FormatItem track) {
    }

    @Override
    public void onPlayEnd() {
        
        if (getPlayer() == null) return;

        long durMs = getPlayer().getDurationMs();

        if (durMs > 0) {

            updateHistory(
                getVideo(), 
                durMs
            );

        }
        
    }

    @Override
    public void onBuffering() {

        if (getPlayer() == null || getVideo() == null) return;

        Video item = getVideo();

        boolean liveEnd = item.isLive && getPlayer().getDurationMs() - getPlayer().getPositionMs() <= 1_000;

        if (liveEnd) {

            getPlayer().setPositionMs(getPlayer().getDurationMs() - LIVE_BUFFER_MS);

            getPlayer().setSpeed(1.0f);

        }

    }

    @Override
    public void onSourceChanged(Video item) {

        if (getPlayer() == null) return;

        if (getPlayerData().getTempVideoFormat() != null) {
            getPlayer().setFormat(getPlayerData().getTempVideoFormat());
        } else {
            getPlayer().setFormat(getPlayerData().getFormat(FormatItem.TYPE_VIDEO));
        }
        
        getPlayer().setFormat(getPlayerData().getFormat(FormatItem.TYPE_AUDIO));

        // We don't know yet do we really need a subs.
        // NOTE: Some subs can hang the app.
        restoreSubtitleFormat();

    }

    @Override
    public void onSpeedChanged(float speed) {
        if (getVideo() == null) return;

        getPlayer().setSpeed(speed);
    }

    @Override
    public void onButtonClicked(int buttonId, int buttonState) {

        super.onButtonClicked(buttonId, buttonState);

        if (buttonId == R.id.action_video_speed) {
            onButtonLongClicked(buttonId, buttonState);
        }

    }

    private static final float[] SPEED_LIST = new float[] {
        0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.25f, 2.5f, 2.75f, 3.0f, 3.25f, 3.5f, 3.75f, 4.0f
    };

    @Override
    public void onButtonLongClicked(int buttonId, int buttonState) {

        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

        if (buttonId == R.id.action_video_speed) {

            List<UiOptionItem> items = new ArrayList<>();

            for (float speed : SPEED_LIST) {
                
                items.add(UiOptionItem.from(
                    String.valueOf(speed),
                    optionItem -> getPlayer().setSpeed(speed),
                    (getPlayer().getSpeed() == speed)
                ));
            
            }

            String title = "Video Speed";

            settingsPresenter.appendCategory(OptionCategory.from(
                AppDialogUtil.PLAYER_SPEED_LIST_ID, 
                OptionCategory.TYPE_RADIO_LIST, 
                title, items
            ));

            settingsPresenter.showDialog(title);

        }

    }

    public void updateHistory(Video video, long positionMs) {

        State state = new State(
            video,
            positionMs,
            video.getDurationMs()
        );

        video.sync(state);

        Queue.sync(video);

        getStateService().save(state);

        if (mFormatInfoUpdateDisposable != null) {
            mFormatInfoUpdateDisposable.dispose();
            mFormatInfoUpdateDisposable = null;
        }

        Observable<MediaItemFormatInfo> observe = getMediaItemService().getFormatInfoObserve(video.videoId);

        mFormatInfoUpdateDisposable = observe.subscribe((MediaItemFormatInfo formatInfo) -> {

            getSignInService().checkAuth();

            if (formatInfo == null) {
                Log.e(TAG, "Can't update history for video id %s. formatInfo == null", video.videoId);
                return;
            }

            if (!formatInfo.isAuth() && !formatInfo.isUnplayable() && getSignInService().isSigned()) {

                VideoInfo videoInfo = getVideoInfoService().getAuthVideoInfo(
                    formatInfo.getVideoId(),
                    formatInfo.getClickTrackingParams()
                );

                formatInfo.sync(MediaItemFormatInfo.from(videoInfo));

            }

            float positionSec = positionMs / 1_000f;
            float lengthSec = Helpers.parseFloat(formatInfo.getLengthSeconds());

            if (mVideoId == null || !mVideoId.equals(video.videoId)) {

                mVideoId = video.videoId;
                mPositionSec = 0;

                Call<TrackingApi.EmptyResult> wrapper = mTrackingApi.createWatchRecord(
                    video.videoId,
                    lengthSec,
                    mPositionSec,
                    getAppService().getClientPlaybackNonce(),
                    formatInfo.getEventId(),
                    formatInfo.getVisitorMonitoringData(),
                    formatInfo.getOfParam()
                );

                RetrofitHelper.get(wrapper); // execute

            }

            Call<TrackingApi.EmptyResult> wrapper = mTrackingApi.updateWatchTime(
                video.videoId,
                lengthSec,
                mPositionSec,
                positionSec,
                positionSec,
                getAppService().getClientPlaybackNonce(),
                formatInfo.getEventId(),
                formatInfo.getVisitorMonitoringData(),
                formatInfo.getOfParam()
            );

            RetrofitHelper.get(wrapper); // execute

            mPositionSec = positionSec;

        });

    }

    private void restoreSubtitleFormat() {

        FormatItem result = getPlayerData().getFormat(FormatItem.TYPE_SUBTITLE);

        getPlayer().setFormat(result);
    
    }

    public void blockPlay(boolean block) {
        mIsPlayBlocked = block;
    }

    public void setPlayEnabled(boolean isPlayEnabled) {
        Log.d(TAG, "setPlayEnabled %s", isPlayEnabled);
        mIsPlayEnabled = isPlayEnabled;
    }

    public boolean getPlayEnabled() {
        return mIsPlayEnabled;
    }

}
