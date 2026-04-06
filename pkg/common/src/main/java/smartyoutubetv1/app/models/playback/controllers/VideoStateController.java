package smartyoutubetv1.app.models.playback.controllers;

import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import smartyoutubetv1.R;
import smartyoutubetv1.app.models.data.Playlist;
import smartyoutubetv1.app.models.data.Video;
import smartyoutubetv1.app.models.playback.BasePlayerController;
import smartyoutubetv1.app.models.playback.manager.PlayerUI;
import smartyoutubetv1.app.models.playback.service.VideoStateService.State;
import smartyoutubetv1.app.presenters.AppDialogPresenter;
import smartyoutubetv1.exoplayer.selector.FormatItem;
import smartyoutubetv1.misc.MediaServiceManager;
import smartyoutubetv1.prefs.GeneralData;
import smartyoutubetv1.utils.AppDialogUtil;
import smartyoutubetv1.utils.Utils;
import com.liskovsoft.youtubeapi.service.internal.MediaServiceData;

public class VideoStateController extends BasePlayerController {

    private static final String TAG = VideoStateController.class.getSimpleName();

    private static final long MUSIC_VIDEO_MAX_DURATION_MS = 6 * 60 * 1000;
    private static final long RESTORE_LIVE_BUFFER_MS = 60_000;
    private static final long OFFICIAL_LIVE_BUFFER_MS = 15_000; // Official app buffer
    private static final long LIVE_BUFFER_MS = OFFICIAL_LIVE_BUFFER_MS;
    private static final long BEGIN_THRESHOLD_MS = 10_000;
    private static final long EMBED_THRESHOLD_MS = 30_000;

    private boolean mIsPlayEnabled;
    private boolean mIsPlayBlocked;
    private long mNewVideoTimeMs;

    /**
     * Fired after user clicked on video in browse activity<br/>
     * or video is opened from the intent
     */
    @Override
    public void onNewVideo(Video item) {

        // Ensure that we aren't running on presenter init stage
        if (getPlayer() != null && getPlayer().containsMedia()) {
            saveState();
        }

        if (!item.equals(getVideo())) {
            mNewVideoTimeMs = System.currentTimeMillis();
        }

        setPlayEnabled(true); // video just added

        getPlayerData().setTempVideoFormat(null);

        // Don't do reset on videoLoaded state because this will influences minimized music videos.
        resetPositionIfNeeded(item);
        resetGlobalSpeedIfNeeded();

    }

    @Override
    public boolean onPreviousClicked() {
        // Seek to the start on prev
        if (getPlayer() != null && getPlayer().getPositionMs() > BEGIN_THRESHOLD_MS) {
            saveState(); // in case the user wants to go to previous video
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
                && getVideo().isLive && (getPlayer().getDurationMs() - getPlayer().getPositionMs() > getLiveThreshold())) {
            getPlayer().setPositionMs(getPlayer().getDurationMs() - getLiveBuffer());
            return true;
        }

        setPlayEnabled(true);

        saveState();

        clearStateOfNextVideo();

        return false;
    }

    @Override
    public void onSuggestionItemClicked(Video item) {
        setPlayEnabled(true); // autoplay video from suggestions

        saveState();
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
        if (getPlayer() == null) {
            return;
        }

        // Save previous state
        if (getPlayer().containsMedia()) {
            setPlayEnabled(getPlayer().getPlayWhenReady());
            saveState();
        }
    }

    @Override
    public void onTickle() {
        if (getPlayer() == null || !getPlayer().isEngineInitialized()) {
            return;
        }

        saveState();

    }

    @Override
    public void onMetadata(MediaItemMetadata metadata) {
        // Channel info should be loaded at this point
        restoreSubtitleFormat();

        // Need to contain channel id
        restoreSpeedAndPositionIfNeeded();

        // NOTE: needed for the restore after oom crash?
        saveState(); // start watching?
    }

    @Override
    public void onEngineError(int type, int rendererIndex, Throwable error) {
        if (getPlayer() == null) {
            return;
        }

        // Oops. Error happens while playing (network lost etc).
        if (getPlayer().getPositionMs() > 1_000) {
            saveState();
        }
    }

    @Override
    public void onVideoLoaded(Video item) {
        // Actual video that match currently loaded one.
        //mVideo = item;

        // Restore formats again.
        // Maybe this could help with Shield format problem.
        // NOTE: produce multi thread exception:
        // Attempt to read from field 'java.util.TreeMap$TreeMapEntry java.util.TreeMap$TreeMapEntry.left' on a null object reference (TrackSelectorManager.java:181)
        //restoreFormats();

        // In this state video length is not undefined.
        restoreState();
    }

    @Override
    public void onPlay() {
        setPlayEnabled(true);
    }

    @Override
    public void onPause() {
        setPlayEnabled(false);
        saveState();
    }

    @Override
    public void onTrackSelected(FormatItem track) {
    }

    @Override
    public void onPlayEnd() {
        saveState();
    }

    @Override
    public void onBuffering() {
        // Restore speed on LIVE end or after seek
        restoreSpeedAndPositionIfNeeded();
    }

    @Override
    public void onSourceChanged(Video item) {
        // At this stage video isn't loaded yet. So format switch isn't take any resources.
        restoreFormats();
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
        if (buttonId == R.id.action_video_speed) {
            onSpeedClicked(buttonState == PlayerUI.BUTTON_ON);
        }
    }

    @Override
    public void onButtonLongClicked(int buttonId, int buttonState) {
        if (buttonId == R.id.action_video_speed) {
            onSpeedLongClicked(buttonState == PlayerUI.BUTTON_ON);
        }
    }

    private void onSpeedClicked(boolean enabled) {
        if (getPlayer() == null || getVideo() == null) {
            return;
        }

        fitVideoIntoDialog();

        float lastSpeed = getPlayerData().getSpeed(getVideo().channelId);
        if (Helpers.floatEquals(lastSpeed, 1.0f)) {
            lastSpeed = getPlayerData().getLastSpeed();
        }
        State state = getStateService().getByVideoId(getVideo() != null ? getVideo().videoId : null);
        if (state != null && getPlayerData().isSpeedPerVideoEnabled()) {
            lastSpeed = !Helpers.floatEquals(1.0f, state.speed) ? state.speed : lastSpeed;
            getStateService().save(new State(state.video, state.positionMs, state.durationMs, enabled ? 1.0f : lastSpeed));
        }

        if (Helpers.floatEquals(lastSpeed, 1.0f)) {
            onSpeedLongClicked(enabled);
        } else {
            getPlayer().setSpeed(enabled ? 1.0f : lastSpeed);
        }
    }

    private void onSpeedLongClicked(boolean enabled) {
        fitVideoIntoDialog();

        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

        settingsPresenter.appendCategory(AppDialogUtil.createSpeedListCategory(getContext(), getPlayer()));

        settingsPresenter.showDialog(getContext().getString(R.string.video_speed), () -> {
            State state = getStateService().getByVideoId(getVideo() != null ? getVideo().videoId : null);
            if (state != null && getPlayerData().isSpeedPerVideoEnabled()) {
                getStateService().save(new State(state.video, state.positionMs, state.durationMs, getPlayerData().getSpeed(getVideo().channelId)));
            }
        });
    }

    @Override
    public void onFinish() {
    }

    private void clearStateOfNextVideo() {
        if (getVideo() != null && getVideo().nextMediaItem != null) {
            resetPosition(Video.from(getVideo().nextMediaItem));
        }
    }

    /**
     * Reset position of currently opened music and live videos.
     */
    private void resetPositionIfNeeded(Video item) {
        if (getStateService() == null || item == null) {
            return;
        }

        State state = getStateService().getByVideoId(item.videoId);

        boolean isVideoEnded = 
            state != null 
            && (state.durationMs - state.positionMs) < 3_000;

        boolean isLive = item.isLive;

        if (getPlayerTweaksData().isRememberPositionOfLiveVideosEnabled() && item.isFullLive()) {
            isLive = false;
        }

        if (isVideoEnded || isLive) {
            resetPosition(item);
        }
        
    }

    private void resetGlobalSpeedIfNeeded() {
        if (!getPlayerData().isAllSpeedEnabled()) {
            getPlayerData().setSpeed(1.0f);
        }
    }

    private void resetPosition(Video video) {
        if (video == null) {
            return;
        }

        video.markNotViewed();
        State state = getStateService().getByVideoId(video.videoId);

        if (state != null) {
            if (getPlayerData().isSpeedPerVideoEnabled()) {
                getStateService().save(new State(video, 0, state.durationMs, state.speed));
            } else {
                getStateService().removeByVideoId(video.videoId);
            }
        }
    }

    private void restoreVideoFormat() {
        if (getPlayer() == null) {
            return;
        }

        if (getPlayerData().getTempVideoFormat() != null) {
            getPlayer().setFormat(getPlayerData().getTempVideoFormat());
        } else {
            getPlayer().setFormat(getPlayerData().getFormat(FormatItem.TYPE_VIDEO));
        }
    }

    private void restoreAudioFormat() {
        getPlayer().setFormat(getPlayerData().getFormat(FormatItem.TYPE_AUDIO));
    }

    private void restoreSubtitleFormat() {

        FormatItem result = getPlayerData().getFormat(FormatItem.TYPE_SUBTITLE);

        getPlayer().setFormat(result);
    
    }

    private void saveState() {
        // Skip mini player, but don't save for the previews (mute enabled)
        if (isMutedEmbed()) {
            return;
        }

        savePosition();

        if (!isBeginEmbed()) {
            updateHistory();
            syncWithPlaylists();
        }
    }

    private void restoreState() {
        if (getPlayer() == null) {
            return;
        }

        restorePosition();
        restorePendingPosition();

        restoreVolume();
        restorePitch();
    }

    private void savePosition() {
        Video video = getVideo();

        if (video == null || getPlayer() == null || !getPlayer().containsMedia()) {
            return;
        }

        // Exceptional cases:
        // 1) Track is ended
        // 2) Pause on end enabled
        // 3) Watching live stream in real time
        long durationMs = getPlayer().getDurationMs();
        long positionMs = getPlayer().getPositionMs();
        long remainsMs = durationMs - positionMs;
        boolean isPositionActual = remainsMs > 1_000;
        boolean isLiveBroken = video.isLive && durationMs <= 30_000; // the live without a history
        if (isPositionActual && !isLiveBroken) { // partially viewed
            State state = new State(video, positionMs, durationMs, getPlayer().getSpeed());
            getStateService().save(state);
            // Sync video. You could safely use it later to restore state.
            video.sync(state);
        } else { // fully viewed
            // Mark video as fully viewed. This could help to restore proper progress marker on the video card later.
            getStateService().save(new State(video, durationMs, durationMs, getPlayer().getSpeed()));
            video.markFullyViewed();
        }

        Playlist.instance().sync(video);
    }

    private void restorePosition() {
        Video item = getVideo();

        if (getPlayer() == null || item == null) {
            return;
        }

        State state = getStateService().getByVideoId(item.videoId);

        boolean stateIsOutdated = isStateOutdated(state, item);
        if (stateIsOutdated) { // check that the user logged in
            // Web state is buggy on short videos (e.g. video clips)
            boolean isLongVideo = getPlayer().getDurationMs() > MUSIC_VIDEO_MAX_DURATION_MS;
            if (isLongVideo) {
                state = new State(item, item.getPositionMs());
            }
        }

        // Set actual position for live videos with uncommon length
        if (item.isLive && (state == null || state.durationMs - state.positionMs < Math.max(RESTORE_LIVE_BUFFER_MS, getLiveThreshold()))) {
            // Add buffer. Should I take into account segment offset???
            state = new State(item, getPlayer().getDurationMs() - getLiveBuffer());
        }

        // Do I need to check that item isn't live? (state != null && !item.isLive)
        if (state != null) {
            getPlayer().setPositionMs(state.positionMs);
        }

        if (!mIsPlayBlocked) {
            getPlayer().setPlayWhenReady(getPlayEnabled());
        }
    }

    private void updateHistory() {
        Video video = getVideo();

        if (
            video == null 
            || getPlayer() == null 
            || !getPlayer().containsMedia()
            || (video.isRemote && getRemoteControlData().isRemoteHistoryDisabled())
        ) {
            return;
        }

        MediaServiceManager.instance().updateHistory(video, Math.max(getPlayer().getPositionMs(), 3_000)); // 0 == fully watched
    }

    /**
     * Restore position from description time code
     */
    private void restorePendingPosition() {
        if (getPlayer() == null || getVideo() == null) {
            return;
        }

        Video item = getVideo();

        if (item.pendingPosMs > 0) {
            getPlayer().setPositionMs(item.pendingPosMs);
            item.pendingPosMs = 0;
        }
    }

    private void restoreSpeedAndPositionIfNeeded() {
        if (getPlayer() == null || getVideo() == null) {
            return;
        }

        Video item = getVideo();

        boolean liveEnd = isLiveEnd();

        // Position
        if (liveEnd) {
            getPlayer().setPositionMs(getPlayer().getDurationMs() - getLiveBuffer());
        }

        // Speed
        if (liveEnd) {
            getPlayer().setSpeed(1.0f);
        } else {
            State state = getStateService().getByVideoId(item.videoId);
            float speed = getPlayerData().getSpeed(item.channelId);
            getPlayer().setSpeed(
                    state != null && getPlayerData().isSpeedPerVideoEnabled() ? state.speed :
                            getPlayerData().isAllSpeedEnabled() || item.channelId != null ? speed : 1.0f
            );
        }
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

    private void restoreVolume() {

        if (getVideo() == null || getPlayer() == null) {
            return;
        }

        float newVolume = getVideo().volume;

        if (getVideo().isShorts) {
            newVolume /= 2;
        }

        getPlayer().setVolume(newVolume);

    }

    private void restorePitch() {
        if (getPlayer() == null) {
            return;
        }

        getPlayer().setPitch(getPlayerData().getPitch());
    }

    private void restoreFormats() {
        restoreVideoFormat();
        restoreAudioFormat();
        // We don't know yet do we really need a subs.
        // NOTE: Some subs can hang the app.
        restoreSubtitleFormat();
    }

    private boolean isStateOutdated(State state, Video item) {
        if (state == null) {
            return true;
        }

        // Web live position is broken. Ignore it.
        if (item.isLive || item.getPositionMs() <= 0) {
            return false;
        }

        float posPercents1 = state.positionMs * 100f / state.durationMs;
        float posPercents2 = item.getPositionMs() * 100f / item.getDurationMs();

        return (posPercents2 != 0 && Math.abs(posPercents1 - posPercents2) > 3) && state.timestamp < item.timestamp;
    }

    private void syncWithPlaylists() {
    }

    private boolean isLiveEnd() {
        if (getPlayer() == null || getVideo() == null || !getVideo().isLive) {
            return false;
        }

        return getPlayer().getDurationMs() - getPlayer().getPositionMs() <= 1_000;
    }

    private long getLiveThreshold() {
        return getLiveBuffer() + 5_000;
    }

    private long getLiveBuffer() {
        return LIVE_BUFFER_MS;
    }

    private boolean isMutedEmbed() {
        return isEmbedPlayer() && getPlayer() != null && Helpers.floatEquals(getPlayer().getVolume(), 0);
    }

    private boolean isBeginEmbed() {
        return isEmbedPlayer() && System.currentTimeMillis() - mNewVideoTimeMs <= EMBED_THRESHOLD_MS &&
                getPlayer() != null && getPlayer().getPositionMs() < getPlayer().getDurationMs();
    }
}
