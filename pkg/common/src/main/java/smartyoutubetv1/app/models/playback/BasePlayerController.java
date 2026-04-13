package smartyoutubetv1.app.models.playback;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Format;
import com.liskovsoft.mediaserviceinterfaces.CommentsService;
import com.liskovsoft.mediaserviceinterfaces.ContentService;
import com.liskovsoft.mediaserviceinterfaces.MediaItemService;
import com.liskovsoft.mediaserviceinterfaces.NotificationsService;
import com.liskovsoft.mediaserviceinterfaces.SignInService;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;
import smartyoutubetv1.app.models.data.Video;
import smartyoutubetv1.app.models.playback.listener.PlayerEventListener;
import smartyoutubetv1.app.presenters.AppDialogPresenter;
import smartyoutubetv1.app.presenters.PlaybackPresenter;
import smartyoutubetv1.app.presenters.SearchPresenter;
import smartyoutubetv1.app.views.PlaybackView;
import smartyoutubetv1.app.views.ViewManager;
import smartyoutubetv1.exoplayer.selector.FormatItem;
import smartyoutubetv1.exoplayer.selector.TrackSelectorUtil;
import smartyoutubetv1.misc.MediaServiceManager;
import smartyoutubetv1.misc.MotherActivity;
import smartyoutubetv1.prefs.ContentBlockData;
import smartyoutubetv1.prefs.GeneralData;
import smartyoutubetv1.prefs.MainUIData;
import smartyoutubetv1.prefs.PlayerData;
import smartyoutubetv1.prefs.PlayerTweaksData;
import smartyoutubetv1.prefs.RemoteControlData;
import smartyoutubetv1.prefs.SearchData;
import com.liskovsoft.youtubeapi.service.YouTubeServiceManager;
import com.liskovsoft.youtubeapi.service.internal.MediaServiceData;

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
