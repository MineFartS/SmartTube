package minefarts.smarttube.app.models.playback;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Format;
import com.liskovsoft.sharedutils.CommentsService;
import com.liskovsoft.sharedutils.service.ContentService;
import com.liskovsoft.sharedutils.MediaItemService;
import com.liskovsoft.sharedutils.NotificationsService;
import com.liskovsoft.sharedutils.SignInService;
import com.liskovsoft.sharedutils.data.MediaItemMetadata;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.playback.PlayerEventListener;
import minefarts.smarttube.app.models.playback.service.VideoStateService;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.app.presenters.PlaybackPresenter;
import minefarts.smarttube.app.presenters.SearchPresenter;
import minefarts.smarttube.app.models.playback.PlayerEngine;
import minefarts.smarttube.app.views.ViewManager;
import minefarts.smarttube.exoplayer.selector.FormatItem;
import minefarts.smarttube.exoplayer.selector.TrackSelectorUtil;
import minefarts.smarttube.misc.ServiceManager;
import minefarts.smarttube.misc.MotherActivity;
import minefarts.smarttube.prefs.ContentBlockData;
import minefarts.smarttube.prefs.GeneralData;
import minefarts.smarttube.prefs.MainUIData;
import minefarts.smarttube.prefs.PlayerData;
import minefarts.smarttube.prefs.PlayerTweaksData;
import minefarts.smarttube.prefs.RemoteControlData;
import minefarts.smarttube.prefs.SearchData;
import com.liskovsoft.sharedutils.service.internal.MediaServiceData;

public abstract class BasePlayerController extends ServiceManager implements PlayerEventListener {
    
    private PlaybackPresenter mMainController;
    private Context mContext;

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
    public PlayerEngine getPlayer() {
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
        
        if (getPlayer() == null) return;

        AppDialogPresenter settingsPresenter = getAppDialogPresenter();

        settingsPresenter.setOnStart(
            () -> getPlayer().setVideoGravity(Gravity.START | Gravity.CENTER_VERTICAL)
        );

        settingsPresenter.setOnFinish(
            () -> getPlayer().setVideoGravity(Gravity.CENTER)
        );

    }

    @Override
    public void onButtonLongClicked(int buttonId, int buttonState) {
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

    protected ViewManager getViewManager() {
        return ViewManager.instance(getContext());
    }

    protected AppDialogPresenter getAppDialogPresenter() {
        return AppDialogPresenter.instance(getContext());
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

}
