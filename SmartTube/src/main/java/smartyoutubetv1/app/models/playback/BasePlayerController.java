package SmartTubeApp.app.models.playback;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Format;
import com.liskovsoft.sharedutils.CommentsService;
import com.liskovsoft.sharedutils.ContentService;
import com.liskovsoft.sharedutils.MediaItemService;
import com.liskovsoft.sharedutils.NotificationsService;
import com.liskovsoft.sharedutils.SignInService;
import com.liskovsoft.sharedutils.data.MediaItemMetadata;
import SmartTubeApp.app.models.data.Video;
import SmartTubeApp.app.models.playback.listener.PlayerEventListener;
import SmartTubeApp.app.models.playback.service.VideoStateService;
import SmartTubeApp.app.presenters.AppDialogPresenter;
import SmartTubeApp.app.presenters.PlaybackPresenter;
import SmartTubeApp.app.presenters.SearchPresenter;
import SmartTubeApp.app.views.PlaybackView;
import SmartTubeApp.app.views.ViewManager;
import SmartTubeApp.exoplayer.selector.FormatItem;
import SmartTubeApp.exoplayer.selector.TrackSelectorUtil;
import SmartTubeApp.misc.MediaServiceManager;
import SmartTubeApp.misc.MotherActivity;
import SmartTubeApp.prefs.ContentBlockData;
import SmartTubeApp.prefs.GeneralData;
import SmartTubeApp.prefs.MainUIData;
import SmartTubeApp.prefs.PlayerData;
import SmartTubeApp.prefs.PlayerTweaksData;
import SmartTubeApp.prefs.RemoteControlData;
import SmartTubeApp.prefs.SearchData;
import com.liskovsoft.sharedutils.service.YouTubeServiceManager;
import com.liskovsoft.sharedutils.service.internal.MediaServiceData;

public abstract class BasePlayerController implements PlayerEventListener {
    private PlaybackPresenter mMainController;
    private Context mContext;
    
    private final Runnable mFitVideoStart = () -> {
        
        AppDialogPresenter settingsPresenter = getAppDialogPresenter();
        
        if (getPlayer() == null || settingsPresenter.isOverlay()) {
            return;
        }
        
        FormatItem videoFormat = getPlayer().getVideoFormat();
        
        Format format = videoFormat != null && videoFormat.getTrack() != null ? videoFormat.getTrack().format : null;
        
        if (format == null) {
            return;
        }
        
        getPlayer().showControls(false);
        
        // Dialog takes up 37% of the screen space
        float dialogWidth = 37 * getMainUIData().getUIScale();
        float initialZoom = 100;
        float totalZoom = initialZoom - dialogWidth;
        float ratio = format.width / (float) format.height;
        float targetRatio = 16/9f;
        float multiplier = targetRatio / ratio;
        
        if (multiplier > 1) { // skip cinema ratio
            totalZoom *= multiplier;
        }
        
        if (totalZoom > 130) {
            return; // shorts overzoom fix
        }
        
        getPlayer().setZoomPercents(Math.round(totalZoom));
        
        getPlayer().setVideoGravity(
            Gravity.START | Gravity.CENTER_VERTICAL
        );

    };
    private final Runnable mFitVideoFinish = () -> {
        if (getPlayer() != null) {
            getPlayer().setZoomPercents(getPlayerData().getZoomPercents());
            getPlayer().setVideoGravity(Gravity.CENTER);
        };
    };

    public void setMainController(PlaybackPresenter mainController) {
        mMainController = mainController;
    }

    protected PlayerEventListener getMainController() {
        return mMainController;
    }

    protected <T extends PlayerEventListener> T getController(Class<T> clazz) {
        return mMainController != null ? mMainController.getController(clazz) : null;
    }

    @Nullable
    public PlaybackView getPlayer() {
        return mMainController != null ? mMainController.getPlayer() : null;
    }

    @Nullable
    public Video getVideo() {
        return mMainController != null ? mMainController.getVideo() : null;
    }

    protected void setAltContext(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mMainController != null ? mMainController.getContext() : mContext;
    }

    public Activity getActivity() {
        return mMainController != null ? mMainController.getActivity() : null;
    }
    
    @Override
    public void onInit() {
        // NOP
    }

    @Override
    public void onNewVideo(Video item) {
        // NOP
    }

    @Override
    public void onSuggestionItemClicked(Video item) {
        // NOP
    }

    @Override
    public void onSuggestionItemLongClicked(Video item) {
        // NOP
    }

    @Override
    public void onScrollEnd(Video item) {
        // NOP
    }

    @Override
    public boolean onPreviousClicked() {
        // NOP
        return false;
    }

    @Override
    public boolean onNextClicked() {
        // NOP
        return false;
    }

    @Override
    public void onViewCreated() {
        // NOP
    }

    @Override
    public void onViewDestroyed() {
        // NOP
    }

    @Override
    public void onViewPaused() {
        // NOP
    }

    @Override
    public void onViewResumed() {
        // NOP
    }

    @Override
    public void onFinish() {
        // NOP
    }

    @Override
    public void onSourceChanged(Video item) {
        // NOP
    }

    @Override
    public void onVideoLoaded(Video item) {
        // NOP
    }

    @Override
    public void onEngineInitialized() {
        // NOP
    }

    @Override
    public void onEngineReleased() {
        // NOP
    }

    @Override
    public void onEngineError(int type, int rendererIndex, Throwable error) {
        // NOP
    }

    @Override
    public void onPlay() {
        // NOP
    }

    @Override
    public void onPause() {
        // NOP
    }

    @Override
    public void onPlayClicked() {
        // NOP
    }

    @Override
    public void onPauseClicked() {
        // NOP
    }

    @Override
    public void onSeekEnd() {
        // NOP
    }

    @Override
    public void onSeekPositionChanged(long positionMs) {
        // NOP
    }

    @Override
    public void onSpeedChanged(float speed) {
        // NOP
    }

    @Override
    public void onPlayEnd() {
        // NOP
    }

    @Override
    public void onBuffering() {
        // NOP
    }

    @Override
    public void onControlsShown(boolean shown) {
        // NOP
    }

    @Override
    public boolean onKeyDown(int keyCode) {
        // NOP
        return false;
    }

    @Override
    public void onTrackSelected(FormatItem track) {
        // NOP
    }

    @Override
    public void onTrackChanged(FormatItem track) {
        // NOP
    }

    @Override
    public void onButtonClicked(int buttonId, int buttonState) {
        // NOP
    }

    @Override
    public void onButtonLongClicked(int buttonId, int buttonState) {
        // NOP
    }

    @Override
    public void onTickle() {
        // NOP
    }

    @Override
    public void onMetadata(MediaItemMetadata metadata) {
        // NOP
    }

    protected PlayerData getPlayerData() {
        return PlayerData.instance(getContext());
    }

    protected GeneralData getGeneralData() {
        return GeneralData.instance(getContext());
    }

    protected MediaServiceData getMediaServiceData() {
        return MediaServiceData.instance();
    }

    protected PlayerTweaksData getPlayerTweaksData() {
        return PlayerTweaksData.instance(getContext());
    }

    protected RemoteControlData getRemoteControlData() {
        return RemoteControlData.instance(getContext());
    }

    protected VideoStateService getStateService() {
        return VideoStateService.instance(getContext());
    }

    protected ContentBlockData getContentBlockData() {
        return ContentBlockData.instance(getContext());
    }

    protected SearchData getSearchData() {
        return SearchData.instance(getContext());
    }

    protected MainUIData getMainUIData() {
        return MainUIData.instance(getContext());
    }

    protected MediaServiceManager getServiceManager() {
        return MediaServiceManager.instance();
    }

    protected ViewManager getViewManager() {
        return ViewManager.instance(getContext());
    }

    protected AppDialogPresenter getAppDialogPresenter() {
        return AppDialogPresenter.instance(getContext());
    }

    protected CommentsService getCommentsService() {
        return YouTubeServiceManager.instance().getCommentsService();
    }

    protected ContentService getContentService() {
        return YouTubeServiceManager.instance().getContentService();
    }

    protected SignInService getSignInService() {
        return YouTubeServiceManager.instance().getSignInService();
    }

    protected NotificationsService getNotificationsService() {
        return YouTubeServiceManager.instance().getNotificationsService();
    }

    protected MediaItemService getMediaItemService() {
        return YouTubeServiceManager.instance().getMediaItemService();
    }

    protected SearchPresenter getSearchPresenter() {
        return SearchPresenter.instance(getContext());
    }

    protected PlaybackPresenter getPlaybackPresenter() {
        return PlaybackPresenter.instance(getContext());
    }

    protected boolean isEmbedPlayer() {
        return getPlayer() != null && getPlayer().isEmbed();
    }

    protected void fitVideoIntoDialog() {
        if (getPlayer() == null) {
            return;
        }

        AppDialogPresenter settingsPresenter = getAppDialogPresenter();

        settingsPresenter.setOnStart(mFitVideoStart);

        settingsPresenter.setOnFinish(mFitVideoFinish);
    }
}
