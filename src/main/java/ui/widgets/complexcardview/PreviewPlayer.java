package minefarts.smarttube.ui.widgets.complexcardview;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.liskovsoft.sharedutils.data.MediaItemFormatInfo;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.data.VideoGroup;
import minefarts.smarttube.app.models.playback.ui.ChatReceiver;
import minefarts.smarttube.app.models.playback.ui.SeekBarSegment;
import minefarts.smarttube.app.presenters.PlaybackPresenter;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.app.models.playback.PlayerEngine;
import minefarts.smarttube.app.views.ViewManager;
import minefarts.smarttube.exoplayer.controller.ExoPlayerController;
import minefarts.smarttube.exoplayer.other.ExoPlayerInitializer;
import minefarts.smarttube.exoplayer.selector.FormatItem;
import minefarts.smarttube.exoplayer.versions.renderer.CustomOverridesRenderersFactory;
import minefarts.smarttube.exoplayer.versions.selector.RestoreTrackSelector;
import minefarts.smarttube.utils.Utils;

import java.io.InputStream;
import java.util.List;

public class PreviewPlayer extends PlayerView implements PlayerEngine {

    public static final int QUALITY_LOW = 0;
    public static final int QUALITY_NORMAL = 1;
    private SimpleExoPlayer mPlayer;
    private ExoPlayerInitializer mPlayerInitializer;
    private ExoPlayerController mExoPlayerController;
    private PlaybackPresenter mPlaybackPresenter;
    private Video mVideo;
    private boolean mIsMute;
    private final Runnable mShowView = this::showView;
    private final Runnable mStopPlayback = this::finish;
    private int mQuality;
    private float mPercentWatched;

    public PreviewPlayer(Context context) {
        super(context);
        hideView();
    }

    public PreviewPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        hideView();
    }

    public PreviewPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        hideView();
    }

    private void hideView() {
        setAlpha(0);
    }

    private void showView() {
        setAlpha(1);
    }

    @Override
    public void updateSuggestions(VideoGroup group) {

    }

    @Override
    public void removeSuggestions(VideoGroup group) {

    }

    @Override
    public Integer getSuggestionsIndex(VideoGroup group) {
        return 0;
    }

    @Override
    public VideoGroup getSuggestionsByIndex(int index) {
        return null;
    }

    @Override
    public void focusSuggestedItem(int index) {

    }

    @Override
    public void focusSuggestedItem(Video video) {

    }

    @Override
    public void resetSuggestedPosition() {

    }

    @Override
    public Boolean isSuggestionsEmpty() {
        return false;
    }

    @Override
    public void clearSuggestions() {

    }

    @Override
    public void showOverlay(boolean show) {

    }

    @Override
    public Boolean isOverlayShown() {
        return false;
    }

    @Override
    public void showSuggestions(boolean show) {

    }

    @Override
    public Boolean isSuggestionsShown() {
        return false;
    }

    @Override
    public void showControls(boolean show) {

    }

    @Override
    public Boolean isControlsShown() {
        return false;
    }

    @Override
    public void setButtonState(int buttonId, int buttonState) {

    }

    @Override
    public void setChannelIcon(String iconUrl) {

    }

    @Override
    public void setSeekPreviewTitle(String title) {

    }

    @Override
    public void setNextTitle(Video nextVideo) {

    }

    @Override
    public void showSubtitles(boolean show) {

    }

    @Override
    public void loadStoryboard() {

    }

    @Override
    public void setTitle(String title) {

    }

    @Override
    public void showProgressBar(boolean show) {
        
    }

    @Override
    public void setSeekBarSegments(List<SeekBarSegment> segments) {

    }

    @Override
    public void setChatReceiver(ChatReceiver chatReceiver) {

    }

    @Override
    public void setVideo(Video item) {
        mVideo = item;

        if (mExoPlayerController != null) {
            mExoPlayerController.setVideo(mVideo);
        }
    }

    @Override
    public Video getVideo() {
        return mVideo;
    }

    @Override
    public void finish() {
        destroyPlayerObjects();
    }

    @Override
    public void finishReally() {
        finish();
    }

    @Override
    public void showBackground(String url) {

    }

    @Override
    public void showBackgroundColor(int colorResId) {

    }

    @Override
    public void resetPlayerState() {

    }

    @Override
    public Boolean isEmbed() {
        return true;
    }

    @Override
    public void openSabr(MediaItemFormatInfo formatInfo) {
        mExoPlayerController.openSabr(formatInfo);
    }

    @Override
    public void openDash(MediaItemFormatInfo formatInfo) {
        mExoPlayerController.openDash(formatInfo);
    }

    @Override
    public void openDash(InputStream dashManifest) {
        mExoPlayerController.openDash(dashManifest);
    }

    @Override
    public void openDashUrl(String dashManifestUrl) {
        mExoPlayerController.openDashUrl(dashManifestUrl);
    }

    @Override
    public void openHlsUrl(String hlsPlaylistUrl) {
        mExoPlayerController.openHlsUrl(hlsPlaylistUrl);
    }

    @Override
    public void openUrlList(List<String> urlList) {
        mExoPlayerController.openUrlList(urlList);
    }

    @Override
    public void openMerged(MediaItemFormatInfo formatInfo, String hlsPlaylistUrl) {
        mExoPlayerController.openMerged(formatInfo, hlsPlaylistUrl);
    }

    @Override
    public void openMerged(InputStream dashManifest, String hlsPlaylistUrl) {
        mExoPlayerController.openMerged(dashManifest, hlsPlaylistUrl);
    }

    @Override
    public Long getPositionMs() {
        return mExoPlayerController.getPositionMs();
    }

    @Override
    public void setPositionMs(long positionMs) {
        mExoPlayerController.setPositionMs(positionMs);
    }

    @Override
    public Long getDurationMs() {
        long durationMs = mExoPlayerController.getDurationMs();

        long liveDurationMs = getVideo() != null ? getVideo().getLiveDurationMs() : 0;

        if (durationMs > Video.MAX_LIVE_DURATION_MS && liveDurationMs != 0) {
            durationMs = liveDurationMs;
        }

        return durationMs;
    }

    @Override
    public void setPlayWhenReady(boolean play) {
        mExoPlayerController.setPlayWhenReady(play);
    }

    @Override
    public Boolean getPlayWhenReady() {
        return mExoPlayerController.getPlayWhenReady();
    }

    @Override
    public Boolean isPlaying() {
        return mExoPlayerController.isPlaying();
    }

    @Override
    public Boolean isLoading() {
        return mExoPlayerController.isLoading();
    }

    @Override
    public List<FormatItem> getVideoFormats() {
        return mExoPlayerController.getVideoFormats();
    }

    @Override
    public List<FormatItem> getAudioFormats() {
        return mExoPlayerController.getAudioFormats();
    }

    @Override
    public List<FormatItem> getSubtitleFormats() {
        return mExoPlayerController.getSubtitleFormats();
    }

    @Override
    public void setFormat(FormatItem option) {
        // NOP. Use internal selection
    }

    @Override
    public FormatItem getVideoFormat() {
        return mExoPlayerController.getVideoFormat();
    }

    @Override
    public FormatItem getAudioFormat() {
        return mExoPlayerController.getAudioFormat();
    }

    @Override
    public FormatItem getSubtitleFormat() {
        return mExoPlayerController.getSubtitleFormat();
    }

    @Override
    public Boolean isEngineInitialized() {
        return mPlayer != null;
    }

    @Override
    public void restartEngine() {

    }

    @Override
    public void reloadPlayback() {

    }

    @Override
    public void blockEngine(boolean block) {

    }

    @Override
    public Boolean isEngineBlocked() {
        return false;
    }

    @Override
    public Boolean containsMedia() {
        if (mExoPlayerController == null) {
            return false;
        }

        return mExoPlayerController.containsMedia();
    }

    @Override
    public void setSpeed(float speed) {
        if (mExoPlayerController != null) {
            mExoPlayerController.setSpeed(speed);
        }
    }

    @Override
    public Float getSpeed() {
        if (mExoPlayerController == null) {
            return 1f;
        }

        return mExoPlayerController.getSpeed();
    }

    @Override
    public void setVolume(float volume) {
        if (!mIsMute && mExoPlayerController != null) {
            mExoPlayerController.setVolume(volume);
        }
    }

    @Override
    public Float getVolume() {
        if (mExoPlayerController == null) {
            return 1f;
        }

        return mExoPlayerController.getVolume();
    }

    @Override
    public void setVideoGravity(int gravity) {
        
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            finish();
        } finally {
            super.finalize();
        }
    }

    public void setQuality(int quality) {
        mQuality = quality;
    }

    public void openVideo(String videoId) {
        openVideo(Video.from(videoId));
    }

    public void openVideo(@Nullable Video video) {
        if (video == null) {
            return;
        }

        if (mPlaybackPresenter == null) {
            mPlaybackPresenter = PlaybackPresenter.instance(getContext());
        }

        // Fullscreen playback is running. Skipping
        PlayerEngine view = mPlaybackPresenter.getView();
        if (view == null || view instanceof PreviewPlayer || !PlaybackPresenter.instance(getContext()).isEngineInitialized()) {
            initPlayer();
            createPlayerObjects();
            mPlaybackPresenter.onNewVideo(video);
            mPercentWatched = video.percentWatched;
        }
    }

    private void initPlayer() {
        if (isEngineInitialized()) {
            mPlaybackPresenter.setView(this);
            return;
        }

        mPlayerInitializer = new ExoPlayerInitializer(getContext());
        mPlaybackPresenter.setView(this);
        mExoPlayerController = new ExoPlayerController(getContext(), mPlaybackPresenter);
        mExoPlayerController.setOnVideoLoaded(this::onVideoLoaded);
        mPlaybackPresenter.onViewInitialized(); // init all controllers

    }

    private void createPlayerObjects() {
        if (isEngineInitialized()) {
            setPlayer(mPlayer);
            return;
        }

        // Use default or pass your bandwidthMeter here: bandwidthMeter = new DefaultBandwidthMeter.Builder(getContext()).build()
        DefaultTrackSelector trackSelector = new RestoreTrackSelector(new AdaptiveTrackSelection.Factory());
        mExoPlayerController.setTrackSelector(trackSelector);

        DefaultRenderersFactory renderersFactory = new CustomOverridesRenderersFactory(getContext());
        mPlayer = mPlayerInitializer.createPlayer(getContext(), renderersFactory, trackSelector);
        mPlayer.setPlayWhenReady(true);
        //mPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);

        mExoPlayerController.setPlayer(mPlayer);
        //mExoPlayerController.setVideo(mVideo);
        mExoPlayerController.selectFormat(mQuality == QUALITY_LOW ? FormatItem.VIDEO_SUB_SD_AVC_30 : FormatItem.VIDEO_SD_AVC_30);
        // Don't use subs! Not efficient. High cpu load. Cause input lags.
        mExoPlayerController.selectFormat(FormatItem.SUBTITLE_NONE);
        if (mIsMute) {
            mExoPlayerController.setVolume(0);
        }

        setPlayer(mPlayer);

        mPlaybackPresenter.onEngineInitialized(); // start playback
    }

    private void destroyPlayerObjects() {
        if (isEngineInitialized()) {
            Utils.removeCallbacks(mShowView);
            Utils.removeCallbacks(mStopPlayback);
            // Don't replace main player!
            if (mPlaybackPresenter.getView() == null || mPlaybackPresenter.getView() == this) {
                mPlaybackPresenter.onEngineReleased();
            }
            mExoPlayerController.setOnVideoLoaded(null);
            // Fix access calls when player isn't initialized
            mExoPlayerController.release();
            mPlayer = null;
            setPlayer(null);
            hideView();
            syncPositionIfNeeded();
        }
    }

    private void syncPositionIfNeeded() {
        if (!mIsMute && isPositionChanged()) {
            BasePresenter<?> presenter = ViewManager.instance(getContext()).getTopPresenter();
            if (presenter != null) {
                presenter.syncItem(mVideo);
            }
        }
    }

    private boolean isPositionChanged() {
        return mVideo != null && Math.abs(mPercentWatched - mVideo.percentWatched) > 20;
    }

    private void onVideoLoaded() {
        // Fix the screen becomes black for a moment
        Utils.postDelayed(mShowView, 1_000);
        if (mIsMute) { // Save bandwidth if the previews are muted
            Utils.postDelayed(mStopPlayback, 5 * 60 * 1_000);
        }
    }

    public void setMute(boolean mute) {
        mIsMute = mute;

        if (mExoPlayerController != null) {
            mExoPlayerController.setVolume(mute ? 0 : 1f);
        }
    }
}
