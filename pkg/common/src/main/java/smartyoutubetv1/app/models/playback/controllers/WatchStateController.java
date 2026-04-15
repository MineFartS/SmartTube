package smartyoutubetv1.app.models.playback.controllers;

import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;
import com.liskovsoft.sharedutils.mylogger.Log;
import smartyoutubetv1.app.models.data.Video;
import smartyoutubetv1.app.models.playback.BasePlayerController;
import smartyoutubetv1.app.views.PlaybackView;

import com.liskovsoft.mediaserviceinterfaces.SignInService;
import smartyoutubetv1.misc.MediaServiceManager;

public class WatchStateController extends BasePlayerController {
    private static final String TAG = WatchStateController.class.getSimpleName();
    private static final long BEGIN_THRESHOLD_MS = 10_000;
    private static final int HISTORY_UPDATE_INTERVAL_MINUTES = 3;
    private boolean mIsPlayEnabled;
    private boolean mIncognito;
    private int mTickleLeft;
    private final Runnable mUpdateHistory = this::updateHistory;
    private long mNewVideoTimeMs;
    private boolean mUploaded = false;

    @Override
    public void onNewVideo(Video item) {
        if (getPlayer() != null && getPlayer().containsMedia()) {
            mTickleLeft = 0;
            saveState();
        }

        if (!item.equals(getVideo())) {
            mNewVideoTimeMs = System.currentTimeMillis();
        }

        setPlayEnabled(true);
        enableIncognitoIfNeeded(item);
        mUploaded = false;
    }

    @Override
    public boolean onPreviousClicked() {
        if (getPlayer() != null && getPlayer().getPositionMs() > BEGIN_THRESHOLD_MS) {
            saveState();
            getPlayer().setPositionMs(100);
            return true;
        }
        return false;
    }

    @Override
    public boolean onNextClicked() {
        setPlayEnabled(true);
        saveState();
        return false;
    }

    @Override
    public void onEngineInitialized() {
        mTickleLeft = 0;
        if (!getPlayEnabled()) {
            getPlayer().showOverlay(true);
        }
    }

    @Override
    public void onEngineReleased() {
        if (getPlayer() == null) return;
        if (getPlayer().containsMedia()) {
            setPlayEnabled(getPlayer().getPlayWhenReady());
            saveState();
        }
    }

    @Override
    public void onTickle() {
        if (getPlayer() == null || !getPlayer().isEngineInitialized()) return;
        if (++mTickleLeft > HISTORY_UPDATE_INTERVAL_MINUTES && getPlayer().isPlaying()) {
            mTickleLeft = 0;
            saveState();
        }
    }

    @Override
    public void onMetadata(MediaItemMetadata metadata) {
        restoreSpeedAndPositionIfNeeded();
        saveState();
    }

    @Override
    public void onEngineError(int type, int rendererIndex, Throwable error) {
        if (getPlayer() == null) return;
        if (getPlayer().getPositionMs() > 1000) {
            saveState();
        }
    }

    @Override
    public void onVideoLoaded(Video item) {
        restoreState();
    }

    @Override
    public void onPlay() {
        setPlayEnabled(true);
    }

    @Override
    public void onPause() {
        setPlayEnabled(false);
    }

    @Override
    public void onPlayEnd() {
        saveState();
    }

    @Override
    public void onBuffering() {
        restoreSpeedAndPositionIfNeeded();
    }

    @Override
    public void onFinish() {
        mIncognito = false;
        mUploaded = false;
    }

    private void saveState() {
        savePosition();
        updateHistory();
    }

    private void savePosition() {
        Video video = getVideo();
        PlaybackView player = getPlayer();
        if (video == null || player == null || !player.containsMedia()) return;
        long durationMs = player.getDurationMs();
        long positionMs = player.getPositionMs();
        long remainsMs = durationMs - positionMs;
        boolean isPositionActual = remainsMs > 1000;
        boolean isLiveBroken = video.isLive && durationMs <= 30000;
        if (isPositionActual && !isLiveBroken) {
            video.percentWatched = (positionMs * 100f) / durationMs;
        } else {
            video.markFullyViewed();
        }
    }

    private void updateHistory() {
        Video video = getVideo();
        PlaybackView player = getPlayer();
        if (video == null || mIncognito || player == null || !player.containsMedia()) return;

        SignInService signIn = getSignInService();
        if (!signIn.isSigned()) {
            Log.d(TAG, "Not signed in, skipping watch state upload");
            return;
        }

        long positionMs = Math.max(player.getPositionMs(), 3000L);
        Log.d(TAG, String.format("Updating remote watch state: video=%s positionMs=%d", video.videoId, positionMs));
        MediaServiceManager.instance().updateHistory(video, positionMs);
    }

    private void restoreState() {
        if (getPlayer() == null) return;
        restorePosition();
        restoreSpeedAndPositionIfNeeded();
        getPlayer().setPlayWhenReady(getPlayEnabled());
    }

    private void restorePosition() {
        Video item = getVideo();
        if (item == null) return;
        if (item.pendingPosMs > 0) {
            getPlayer().setPositionMs(item.pendingPosMs);
            item.pendingPosMs = 0;
        }
    }

    private void restoreSpeedAndPositionIfNeeded() {
        Video item = getVideo();
        if (item != null && item.getPositionMs() > 0) {
            getPlayer().setPositionMs(item.getPositionMs());
        }
    }

    private void setPlayEnabled(boolean enabled) {
        mIsPlayEnabled = enabled;
    }

    private boolean getPlayEnabled() {
        return mIsPlayEnabled;
    }

    private void enableIncognitoIfNeeded(Video item) {
        // Skip incognito (no field in Video)
    }

    private long getLiveThreshold() {
        return getLiveBuffer() + 5000;
    }

    private long getLiveBuffer() {
        return 15000; // default
    }
}
