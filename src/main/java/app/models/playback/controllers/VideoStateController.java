package minefarts.smarttube.app.models.playback.controllers;

import com.liskovsoft.sharedutils.data.MediaItemMetadata;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.Queue;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.playback.BasePlayerController;
import com.liskovsoft.sharedutils.service.data.YouTubeMediaItemFormatInfo;
import com.liskovsoft.sharedutils.videoinfo.models.VideoInfo;
import com.liskovsoft.sharedutils.okhttp.ApiCaller;
import minefarts.smarttube.app.models.playback.service.VideoStateService;
import minefarts.smarttube.app.models.playback.service.State;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.exoplayer.selector.FormatItem;
import minefarts.smarttube.prefs.GeneralData;
import minefarts.smarttube.utils.AppDialogUtil;
import minefarts.smarttube.utils.Utils;
import com.liskovsoft.sharedutils.service.internal.MediaServiceData;

public class VideoStateController extends BasePlayerController {

    private static final String TAG = VideoStateController.class.getSimpleName();

    private static final long RESTORE_LIVE_BUFFER_MS = 60_000;
    private static final long LIVE_BUFFER_MS = 15_000;
    private static final long LIVE_THRESH_MS = LIVE_BUFFER_MS + 5_000;

    private static final long BEGIN_THRESHOLD_MS = 10_000;
    private static final long STATE_SAVE_INTERVAL_MS = 10_000; // Save state every 10 seconds

    private boolean mIsPlayEnabled;
    private boolean mIsPlayBlocked;
    private long mNewVideoTimeMs;

    private static float mPositionSec;
    private static String mVideoId;

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

        if (!getPlayerData().isAllSpeedEnabled()) {
            getPlayerData().setSpeed(1.0f);
        }

    }

    @Override
    public boolean onPreviousClicked() {
        // Seek to the start on prev
        if (getPlayer() != null && getPlayer().getPositionMs() > BEGIN_THRESHOLD_MS) {
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
        
        // Start periodic state saving
        Utils.removeCallbacks(this::saveStatePeriodically);
        Utils.postDelayed(this::saveStatePeriodically, STATE_SAVE_INTERVAL_MS);
    }

    @Override
    public void onEngineReleased() {
        if (getPlayer() == null) return;

        // Stop periodic state saving
        Utils.removeCallbacks(this::saveStatePeriodically);
        
        // Save previous state
        if (getPlayer().containsMedia()) {
            setPlayEnabled(getPlayer().getPlayWhenReady());
            onTickle();
        }
    }

    @Override
    public void onTickle() {
        
        if (getPlayer() == null 
            || !getPlayer().isEngineInitialized()
            || getVideo() == null
            || !getPlayer().containsMedia()
        ) return;

        updateHistory(
            getVideo(), 
            getPlayer().getPositionMs()
        );

    }
            
    private void saveStatePeriodically() {
        if (getPlayer() == null || !getPlayer().isEngineInitialized()) {
            return;
        }
        
        // Only save if video is playing (not paused)
        if (getPlayer().getPlayWhenReady()) {
            onTickle();
        }
        
        // Schedule next save
        Utils.postDelayed(this::saveStatePeriodically, STATE_SAVE_INTERVAL_MS);
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

        } else {

            State state = getStateService().getByVideoId(item.videoId);
            float speed = Math.max(0.25f, Math.min(5.0f, getPlayerData().getSpeed(item.channelId)));
            float stateSpeed = state != null && getPlayerData().isSpeedPerVideoEnabled() ? Math.max(0.25f, Math.min(5.0f, state.speed)) : 1.0f;
            getPlayer().setSpeed(
                    stateSpeed
            );

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
        if (getVideo() == null) {
            return;
        }

        getPlayerData().setSpeed(getVideo().channelId, speed);
    }

    @Override
    public void onButtonClicked(int buttonId, int buttonState) {

        super.onButtonClicked(buttonId, buttonState);

        if (buttonId != R.id.action_video_speed) return;
        
        // Pass through to long click
        onButtonLongClicked(buttonId, buttonState);
        
    }

    @Override
    public void onButtonLongClicked(int buttonId, int buttonState) {

        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

        if (buttonId == R.id.action_video_speed) {

            settingsPresenter.appendCategory(
                AppDialogUtil.createSpeedListCategory(getContext(), getPlayer())
            );

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

        getAccountManager().checkAuth();

        YouTubeMediaItemFormatInfo formatInfo = getMediaItemService().getFormatInfo(video.videoId);

        if (formatInfo == null) {
            Log.e(TAG, "Can't update history for video id %s. formatInfo == null", video.videoId);
            return;
        }

        if (!formatInfo.isAuth() && !formatInfo.isUnplayable() && getSignInService().isSigned()) {
            
            VideoInfo videoInfo = getVideoInfoService().getAuthVideoInfo(
                formatInfo.getVideoId(), 
                formatInfo.getClickTrackingParams()
            );

            formatInfo.sync(YouTubeMediaItemFormatInfo.from(videoInfo));
        
        }

        String videoId = formatInfo.getVideoId();
        float oldPositionSec = mPositionSec;
        float positionSec = positionMs / 1_000f;

        ApiCaller apiTempl = new ApiCaller("https://www.youtube.com/api/stats/playback?ns=yt&ver=2");
        apiTempl.add("docid", videoId);
        apiTempl.add("len", Helpers.parseFloat(formatInfo.getLengthSeconds()));
        apiTempl.add("cpn", getAppService().getClientPlaybackNonce());            
        apiTempl.add("ei", formatInfo.getEventId());
        apiTempl.add("vm", formatInfo.getVisitorMonitoringData());
        apiTempl.add("of", formatInfo.getOfParam());

        ApiCaller api;

        if (mVideoId == null || mVideoId != videoId) {

            getAppService().resetClientPlaybackNonce();
            apiTempl.add("cpn", getAppService().getClientPlaybackNonce());

            mVideoId = videoId;
            mPositionSec = 0;

            api = apiTempl.copy();
            api.add("cmt", oldPositionSec);
            api.call();
        
        }

        api = apiTempl.copy();
        api.add("st", oldPositionSec);
        api.add("et", positionSec);
        api.add("cmt", positionSec);
        api.call();
        
        mPositionSec = positionSec;

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
