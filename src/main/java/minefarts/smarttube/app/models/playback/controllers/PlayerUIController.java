package minefarts.smarttube.app.models.playback.controllers;

import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import minefarts.smarttube.utils.MediaItemService;
import minefarts.smarttube.utils.service.data.MediaGroup;
import minefarts.smarttube.utils.data.MediaItem;
import minefarts.smarttube.utils.service.data.MediaItemMetadata;
import minefarts.smarttube.utils.data.NotificationState;
import minefarts.smarttube.utils.data.PlaylistInfo;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.helpers.KeyHelpers;
import minefarts.smarttube.utils.helpers.MessageHelpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.rx.RxHelper;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.data.VideoGroup;
import minefarts.smarttube.app.models.playback.BasePlayerController;
import minefarts.smarttube.ui.playback.PlaybackFragment2;
import minefarts.smarttube.app.models.playback.ui.OptionCategory;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.app.presenters.ChannelPresenter;
import minefarts.smarttube.app.presenters.SearchPresenter;
import minefarts.smarttube.app.presenters.dialogs.menu.VideoMenuPresenter;
import minefarts.smarttube.app.presenters.dialogs.menu.VideoMenuPresenter.VideoMenuCallback;
import minefarts.smarttube.exoplayer.selector.FormatItem;
import minefarts.smarttube.exoplayer.selector.track.SubtitleTrack;
import minefarts.smarttube.prefs.SearchData;
import minefarts.smarttube.utils.AppDialogUtil;
import minefarts.smarttube.utils.Utils;
import minefarts.smarttube.utils.SignInService;
import minefarts.smarttube.ui.playback.actions.SubscribeAction;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.util.function.Function;
import java.util.ArrayList;
import java.util.List;

public class PlayerUIController extends BasePlayerController {

    private static final String TAG = PlayerUIController.class.getSimpleName();
    
    private static final long SUGGESTIONS_RESET_TIMEOUT_MS = 500;
    
    private final Handler mHandler;
    private final MediaItemService mMediaItemService;
    private List<PlaylistInfo> mPlaylistInfos;

    private boolean mEngineReady;
    private boolean mIsMetadataLoaded;
    private long mOverlayHideTimeMs;
    
    private final Runnable mSuggestionsResetHandler = () -> {
        if (getPlayer() == null) return;
        getPlayer().resetSuggestedPosition();
    };

    private final Runnable mUiAutoHideHandler = () -> {
        if (getPlayer() == null) return;

        // Playing the video and dialog overlay isn't shown
        if (getPlayer().isPlaying() && !getAppDialogPresenter().isDialogShown()) {
            if (getPlayer().isControlsShown()) { // don't hide when suggestions is shown
                getPlayer().showOverlay(false);
                mOverlayHideTimeMs = System.currentTimeMillis();
            }
        } else {
            // in seeking state? doing recheck...
            enableUiAutoHideTimeout();
        }
    };

    private final Runnable mSetSubtitleButtonState = this::setSubtitleButtonState;
    private final Runnable mSetPlaylistAddButtonState = this::setPlaylistAddButtonState;

    public PlayerUIController() {
        mHandler = new Handler(Looper.getMainLooper());

        mMediaItemService = getMediaItemService();
    }

    @Override
    public void onInit() {
        if (getPlayer() != null)
            getPlayer().mVideoSurfaceRoot.setZoomPercents(100);        
    }

    @Override
    public void onNewVideo(Video item) {
        enableUiAutoHideTimeout();

        if (item != null && !item.equals(getVideo())) {
            mIsMetadataLoaded = false; // metadata isn't loaded yet at this point
            resetButtonStates();
        }
    }

    @Override
    public void onControlsShown(boolean shown) {
        disableUiAutoHideTimeout();

        if (shown) {
            enableUiAutoHideTimeout();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode) {

        disableUiAutoHideTimeout();
        disableSuggestionsResetTimeout();

        if (getPlayer() == null) {
            return false;
        }

        boolean isHandled = 
            handleBackKey(keyCode) 
            || handleMenuKey(keyCode) 
            || handleConfirmKey(keyCode) 
            || handleStopKey(keyCode) 
            || handleNumKeys(keyCode) 
            || handlePlayPauseKey(keyCode);

        if (isHandled) {
            return true; // don't show UI
        } else {
            enableUiAutoHideTimeout();
            return false;
        }

    }

    private void onSubtitleClicked(boolean enabled) {

        // Only default in the list
        if (getPlayer().getSubtitleFormats() == null || getPlayer().getSubtitleFormats().size() == 1) return;

        FormatItem matchedFormat = null;

        // First run
        if (FormatItem.SUBTITLE_NONE == getPlayerData().getLastSubtitleFormat()) {

            for (FormatItem format : getPlayer().getSubtitleFormats()) {

                String lang = format.getLanguage();

                if (lang != null && lang.contains("english")) {
                    matchedFormat = format;
                    break;
                }

            }

        } else {

            for (FormatItem item : getPlayerData().getLastSubtitleFormats()) {
                if (getPlayer().getSubtitleFormats().contains(item)) {
                    matchedFormat = item;
                    break;
                }
            }

        }

        // Match found
        if (matchedFormat != null) {
            
            FormatItem format = enabled ? FormatItem.SUBTITLE_NONE : matchedFormat;
            getPlayer().setFormat(format);
            getPlayerData().setFormat(format);

            getPlayer().setButtonState(
                R.id.lb_control_closed_captioning, 
                !FormatItem.SUBTITLE_NONE.equals(matchedFormat) && !enabled ? 1 : 0
            );

        } else {
            // Match not found
            onSubtitleLongClicked();
        }
    }

    private void onSubtitleLongClicked() {
        if (getPlayer() == null) return;

        fitVideoIntoDialog();

        AppDialogPresenter settingsPresenter = getAppDialogPresenter();

        settingsPresenter.appendSingleButton(
            UiOptionItem.from(
                "Subtitle Language", 
                optionItem -> {
                    
                    List<FormatItem> subtitleFormats = getPlayer().getSubtitleFormats();
                
                    reorderSubtitles(subtitleFormats);
                    
                    settingsPresenter.appendRadioCategory(
                        "Subtitle Language",
                        UiOptionItem.from(
                            subtitleFormats,
                            option -> {
                                FormatItem format = UiOptionItem.toFormat(option);
                                getPlayer().setFormat(format);
                                getPlayerData().setFormat(format);
                            },
                            "Subtitles disabled"
                        )
                    );
                    settingsPresenter.showDialog();
                }
            )
        );

        settingsPresenter.showDialog("Subtitles", mSetSubtitleButtonState);
    }

    private void onPlaylistAddClicked() {
        fitVideoIntoDialog();

        if (mPlaylistInfos == null) {
            AppDialogUtil.showAddToPlaylistDialog(getContext(), getVideo(),
                    null);
        } else {
            AppDialogUtil.showAddToPlaylistDialog(getContext(), getVideo(),
                    null, mPlaylistInfos, mSetPlaylistAddButtonState);
        }
    }

    @Override
    public void onEngineInitialized() {

        mEngineReady = true;

        if (getAppDialogPresenter().isDialogShown()) {
            getPlayer().showOverlay(true);
        }
        
    }

    @Override
    public void onSeekEnd() {
        if (getPlayer() == null) return;
    }

    @Override
    public void onViewResumed() {
        if (getPlayer() == null) return;

        getPlayer().showSubtitles(true);

        // Maybe dialog just closed. Reset timeout just in case.
        enableUiAutoHideTimeout();
    }

    private void resetButtonStates() {
        if (getPlayer() == null) return;

        getPlayer().setChannelIcon(null);

        getPlayer().setButtonState(R.id.action_thumbs_up, 0);
        getPlayer().setButtonState(R.id.action_thumbs_down, 0);
        getPlayer().setButtonState(R.id.action_playlist_add, 0);
        getPlayer().setButtonState(R.id.lb_control_closed_captioning, 0);
        getPlayer().setButtonState(R.id.action_video_speed, 0);
        getPlayer().setButtonState(R.id.action_chat, 0);
        getPlayer().setButtonState(R.id.action_subscribe, 0);
    }

    @Override
    public void onEngineReleased() {
        Log.d(TAG, "Engine released. Disabling all callbacks...");
        mEngineReady = false;

        disableUiAutoHideTimeout();
        disableSuggestionsResetTimeout();
    }

    @Override
    public void onMetadata(MediaItemMetadata metadata) {

        mIsMetadataLoaded = true;

        getPlayer().loadStoryboard();
        
        getPlayer().setButtonState(
            R.id.action_thumbs_up, 
            metadata.getLikeStatus() == MediaItemMetadata.LIKE_STATUS_LIKE ? 1 : 0
        );
        
        getPlayer().setButtonState(
            R.id.action_thumbs_down, 
            metadata.getLikeStatus() == MediaItemMetadata.LIKE_STATUS_DISLIKE ? 1 : 0
        );

        getPlayer().setChannelIcon(metadata.getAuthorImageUrl());

        setPlaylistAddButtonStateCached();
        setSubtitleButtonState();

        getPlayer().setButtonState(
            R.id.action_subscribe, 
            metadata.isSubscribed() ? 1 : 0
        );

    }

    @Override
    public void onSuggestionItemLongClicked(Video item) {}

    private void onDislikeClicked(boolean dislike) {

        if (getPlayer() == null) return;

        getPlayer().setButtonState(
            R.id.action_thumbs_down, 
            dislike ? 0 : 1
        );

        if (!SignInService.instance().isSigned()) {
            getPlayer().setButtonState(R.id.action_thumbs_down, 0);
            MessageHelpers.showMessage(getContext(), R.string.msg_signed_users_only);
            return;
        }

        if (!dislike) {
            callMediaItemObservable(mMediaItemService::setDislikeObserve);
        } else {
            callMediaItemObservable(mMediaItemService::removeDislikeObserve);
        }
    }

    private void onLikeClicked(boolean like) {

        getPlayer().setButtonState(
            R.id.action_thumbs_up, 
            like ? 0 : 1
        );

        if (!SignInService.instance().isSigned()) {

            getPlayer().setButtonState(R.id.action_thumbs_up, 0);
            
            MessageHelpers.showMessage(getContext(), R.string.msg_signed_users_only);
            return;
        }

        if (!like) {
            callMediaItemObservable(mMediaItemService::setLikeObserve);
        } else {
            callMediaItemObservable(mMediaItemService::removeLikeObserve);
        }
    }

    private void onVideoInfoClicked() {
        fitVideoIntoDialog();

        Video video = getVideo();

        if (video == null) return;

        String description = video.description;

        if (description == null || description.isEmpty()) {
            MessageHelpers.showMessage(getContext(), R.string.description_not_found);
            return;
        }

        AppDialogPresenter dialogPresenter = getAppDialogPresenter();

        String title = String.format("%s - %s", video.getTitleFull(), video.getAuthor());

        dialogPresenter.appendLongTextCategory(title, UiOptionItem.from(description));

        dialogPresenter.showDialog(title);
    }

    @Override
    public void onButtonClicked(int buttonId, int buttonState) {

        super.onButtonClicked(buttonId, buttonState);

        if (!mIsMetadataLoaded) {
            MessageHelpers.showMessage(getContext(), R.string.wait_data_loading);
            return;
        }
        
        if (buttonId == R.id.action_subscribe) {

            SubscribeAction.toggle(getVideo());

            getPlayer().setButtonState(
                R.id.action_subscribe, 
                getVideo().isSubscribed ? 1 : 0
            );

        } else if (buttonId == R.id.action_repeat) {
            int nextMode = getNextRepeatMode(buttonState);
            getPlayerData().setPlaybackMode(nextMode);
            getPlayer().setButtonState(R.id.action_repeat, nextMode);

        } else if (buttonId == R.id.action_channel) {
            ChannelPresenter.instance(getContext()).openChannel(getVideo());

        } else if (buttonId == R.id.action_info) {
            onVideoInfoClicked();

        } else if (buttonId == R.id.action_playlist_add) {
            onPlaylistAddClicked();

        } else if (buttonId == R.id.lb_control_closed_captioning) {
            onSubtitleClicked(buttonState == 1);

        } else if (buttonId == R.id.action_thumbs_down) {
            onDislikeClicked(buttonState == 1);
            
        } else if (buttonId == R.id.action_thumbs_up) {
            onLikeClicked(buttonState == 1);
        }
        
    }

    @Override
    public void onButtonLongClicked(int buttonId, int buttonState) {
        
        if (buttonId == R.id.action_subscribe || buttonId == R.id.action_channel) {
            showNotificationsDialog(buttonState);

        } else if (buttonId == R.id.action_repeat) {
            showPlaybackModeDialog(buttonState);

        } else if (buttonId == R.id.lb_control_closed_captioning) {
            onSubtitleLongClicked();
        }

    }

    private void disableUiAutoHideTimeout() {
        Log.d(TAG, "Stopping auto hide ui timer...");
        mHandler.removeCallbacks(mUiAutoHideHandler);
    }

    private void enableUiAutoHideTimeout() {
        Log.d(TAG, "Starting auto hide ui timer...");
        disableUiAutoHideTimeout();
        if (mEngineReady) {
            mHandler.postDelayed(mUiAutoHideHandler, 3_000L);
        }
    }

    private void disableSuggestionsResetTimeout() {
        Log.d(TAG, "Stopping reset position timer...");
        mHandler.removeCallbacks(mSuggestionsResetHandler);
    }

    private void enableSuggestionsResetTimeout() {
        Log.d(TAG, "Starting reset position timer...");
        disableSuggestionsResetTimeout();
        if (mEngineReady) {
            mHandler.postDelayed(mSuggestionsResetHandler, SUGGESTIONS_RESET_TIMEOUT_MS);
        }
    }

    private void callMediaItemObservable(Function<MediaItem, Observable<Void>> callable) {
        Video video = getVideo();

        if (video == null) {
            Log.w(TAG, "Seems that video isn't initialized yet. Cancelling...");
            return;
        }

        Observable<Void> observable = callable.apply(
            video.mediaItem != null ? video.mediaItem : video.toMediaItem()
        );

        RxHelper.execute(observable);
    }

    private boolean handleBackKey(int keyCode) {
        if (KeyHelpers.isBackKey(keyCode)) {
            enableSuggestionsResetTimeout();

            // Close unplayable videos with single back click
            // Cause the background playback bugs if not to check for upcoming or unplayable!!!
            // To reproduce the bug:
            // 1) Set bg black to "Only audio when pressing HOME"
            // 2) Enable "keep finished activities"
            // 3) Close the video when it fully finished and ready to skip to the next
            if (getVideo() != null && getPlayer() != null &&
                    (getVideo().isUnplayable || getVideo().isUpcoming) && getPlayer().isControlsShown()) {
                getPlayer().finish();
            }

            // Back key cooling
            if (System.currentTimeMillis() - mOverlayHideTimeMs < 1_000) {
                return true;
            }
        }

        return false;
    }

    private boolean handleMenuKey(int keyCode) {
        boolean controlsShown = getPlayer().isOverlayShown();
        boolean suggestionsShown = getPlayer().isSuggestionsShown();

        if (KeyHelpers.isMenuKey(keyCode) && !suggestionsShown) {
            getPlayer().showOverlay(!controlsShown);

            if (controlsShown) {
                enableSuggestionsResetTimeout();
            }
        }

        return false;
    }

    private boolean handleConfirmKey(int keyCode) {
        
        boolean isConfirmKey = KeyHelpers.isConfirmKey(keyCode);
        boolean controlsHidden = !getPlayer().isOverlayShown();

        if (isConfirmKey && controlsHidden) {
            getPlayer().showOverlay(true);
            return true; // don't show ui
        }

        return false;

    }

    private boolean handleStopKey(int keyCode) {
        if (KeyHelpers.isStopKey(keyCode)) {
            getPlayer().finish();
            return true;
        }

        return false;
    }

    private boolean handleNumKeys(int keyCode) {
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            if (getPlayer() != null && getPlayer().getDurationMs() > 0) {
                float seekPercent = (keyCode - KeyEvent.KEYCODE_0) / 10f;
                getPlayer().setPositionMs((long)(getPlayer().getDurationMs() * seekPercent));
            }
        }

        return false;
    }

    private boolean handlePlayPauseKey(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE && getPlayer() != null) {
            getPlayer().setPlayWhenReady(!getPlayer().getPlayWhenReady());
            enableUiAutoHideTimeout(); // TODO: move out somehow
            return true;
        }

        return false;
    }

    private void setPlaylistAddButtonStateCached() {
        if (getVideo() == null) return;

        String videoId = getVideo().videoId;
        mPlaylistInfos = null;
        Disposable playlistsInfoAction = getMediaItemService().getPlaylistsInfoObserve(videoId).subscribe(
            videoPlaylistInfos -> {
                mPlaylistInfos = videoPlaylistInfos;
                setPlaylistAddButtonState();
            },
            error -> Log.e(TAG, "Add to recent playlist error: %s", error.getMessage())
        );
    }

    private void setPlaylistAddButtonState() {
        if (mPlaylistInfos == null || getPlayer() == null) return;

        boolean isSelected = false;
        for (PlaylistInfo playlistInfo : mPlaylistInfos) {
            if (playlistInfo.isSelected()) {
                isSelected = true;
                break;
            }
        }

        getPlayer().setButtonState(
            R.id.action_playlist_add, 
            isSelected ? 1 : 0
        );

    }

    private void setSubtitleButtonState() {
        if (getPlayer() == null) return;

        int selected = 0;

        List<FormatItem> subtitleFormats = getPlayer().getSubtitleFormats();

        if (subtitleFormats != null) {
            for (FormatItem subtitle : subtitleFormats) {
                if (subtitle.isSelected() && !subtitle.isDefault()) {
                    selected = 1;
                    break;
                }
            }
        }

        getPlayer().setButtonState(
            R.id.lb_control_closed_captioning, 
            selected
        );

    }

    private void showPlaybackModeDialog(int buttonState) {
        OptionCategory category = AppDialogUtil.createPlaybackModeCategory(
                getContext(), () -> {
                    getPlayer().setButtonState(
                        R.id.action_repeat, 
                        getPlayerData().getPlaybackMode()
                    );
                });

        AppDialogPresenter settingsPresenter = getAppDialogPresenter();
        settingsPresenter.appendRadioCategory(category.title, category.options);
        settingsPresenter.showDialog();
    }

    private int getNextRepeatMode(int buttonState) {
        Integer[] modeList = {PlaybackFragment2.PLAYBACK_MODE_ALL, PlaybackFragment2.PLAYBACK_MODE_ONE, PlaybackFragment2.PLAYBACK_MODE_SHUFFLE,
                PlaybackFragment2.PLAYBACK_MODE_LIST, PlaybackFragment2.PLAYBACK_MODE_REVERSE_LIST, PlaybackFragment2.PLAYBACK_MODE_PAUSE, PlaybackFragment2.PLAYBACK_MODE_CLOSE};
        int nextMode = Helpers.getNextValue(modeList, buttonState);
        return nextMode;
    }

    private void reorderSubtitles(List<FormatItem> subtitleFormats) {
        if (subtitleFormats == null || subtitleFormats.isEmpty()) return;

        // Move last format to the top
        int begin = subtitleFormats.get(0).isDefault() ? 1 : 0;
        List<FormatItem> topSubtitles = new ArrayList<>();
        for (FormatItem item : getPlayerData().getLastSubtitleFormats()) {
            if (item == null || item.getLanguage() == null) { // skip empty formats
                continue;
            }
            int index = 0;
            while (index != -1) {
                index = subtitleFormats.indexOf(item);
                if (index != -1) {
                    topSubtitles.add(subtitleFormats.remove(index));
                }
            }
        }
        subtitleFormats.addAll(subtitleFormats.size() < begin ? 0 : begin, topSubtitles);
    }

    private void showNotificationsDialog(int buttonState) {
        if (getVideo() == null || getVideo().notificationStates == null) return;

        AppDialogPresenter settingsPresenter = getAppDialogPresenter();

        List<UiOptionItem> items = new ArrayList<>();

        for (NotificationState item : getVideo().notificationStates) {
            items.add(UiOptionItem.from(item.getTitle(), optionItem -> {
                if (optionItem.isSelected()) {
                    setNotificationState(item, error -> MessageHelpers.showMessage(getContext(), error.getLocalizedMessage()));
                    getVideo().isSubscribed = true;
                    getPlayer().setButtonState(R.id.action_subscribe, 1);
                }
            }, item.isSelected()));
        }

        settingsPresenter.appendRadioCategory(getContext().getString(R.string.header_notifications), items);
        settingsPresenter.showDialog(getContext().getString(R.string.header_notifications));
    }

}
