package smartyoutubetv1.app.models.playback.controllers;

import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import com.liskovsoft.mediaserviceinterfaces.MediaItemService;
import com.liskovsoft.mediaserviceinterfaces.ServiceManager;
import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItem;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;
import com.liskovsoft.mediaserviceinterfaces.data.NotificationState;
import com.liskovsoft.mediaserviceinterfaces.data.PlaylistInfo;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.KeyHelpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import smartyoutubetv1.R;
import smartyoutubetv1.app.models.data.Video;
import smartyoutubetv1.app.models.data.VideoGroup;
import smartyoutubetv1.app.models.playback.BasePlayerController;
import smartyoutubetv1.app.models.playback.manager.PlayerConstants;
import smartyoutubetv1.app.models.playback.manager.PlayerUI;
import smartyoutubetv1.app.models.playback.ui.OptionCategory;
import smartyoutubetv1.app.models.playback.ui.OptionItem;
import smartyoutubetv1.app.models.playback.ui.UiOptionItem;
import smartyoutubetv1.app.presenters.AppDialogPresenter;
import smartyoutubetv1.app.presenters.ChannelPresenter;
import smartyoutubetv1.app.presenters.SearchPresenter;
import smartyoutubetv1.app.presenters.dialogs.menu.VideoMenuPresenter;
import smartyoutubetv1.app.presenters.dialogs.menu.VideoMenuPresenter.VideoMenuCallback;
import smartyoutubetv1.exoplayer.selector.FormatItem;
import smartyoutubetv1.exoplayer.selector.track.SubtitleTrack;
import smartyoutubetv1.misc.MediaServiceManager;
import smartyoutubetv1.prefs.PlayerData;
import smartyoutubetv1.prefs.SearchData;
import smartyoutubetv1.utils.AppDialogUtil;
import smartyoutubetv1.utils.Utils;
import com.liskovsoft.youtubeapi.service.YouTubeServiceManager;
import com.liskovsoft.youtubeapi.service.YouTubeSignInService;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.util.ArrayList;
import java.util.List;

public class PlayerUIController extends BasePlayerController {
    private static final String TAG = PlayerUIController.class.getSimpleName();
    private static final long SUGGESTIONS_RESET_TIMEOUT_MS = 500;
    private final Handler mHandler;
    private final MediaItemService mMediaItemService;
    private SuggestionsController mSuggestionsController;
    private List<PlaylistInfo> mPlaylistInfos;

    private boolean mEngineReady;
    private boolean mIsMetadataLoaded;
    private long mOverlayHideTimeMs;
    private final Runnable mSuggestionsResetHandler = () -> {
        if (getPlayer() == null) {
            return;
        }
        getPlayer().resetSuggestedPosition();
    };
    private final Runnable mUiAutoHideHandler = () -> {
        if (getPlayer() == null) {
            return;
        }

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

        ServiceManager service = YouTubeServiceManager.instance();
        mMediaItemService = service.getMediaItemService();
    }

    @Override
    public void onInit() {
        mSuggestionsController = getController(SuggestionsController.class);

        if (getPlayer() != null) {
            // Could be set once per activity creation (view layout stuff)
            getPlayer().setResizeMode(getPlayerData().getResizeMode());
            getPlayer().setZoomPercents(getPlayerData().getZoomPercents());
            getPlayer().setRotationAngle(getPlayerData().getRotationAngle());
        }
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
        if (getPlayer().getSubtitleFormats() == null || getPlayer().getSubtitleFormats().size() == 1) {
            return;
        }

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
            setSubtitleFormat(format);

            getPlayer().setButtonState(
                R.id.lb_control_closed_captioning, 
                !FormatItem.SUBTITLE_NONE.equals(matchedFormat) && !enabled ? PlayerUI.BUTTON_ON : PlayerUI.BUTTON_OFF
            );

        } else {
            // Match not found
            onSubtitleLongClicked();
        }
    }

    private void setSubtitleFormat(FormatItem format) {
        getPlayer().setFormat(format);
        getPlayerData().setFormat(format);
    }

    private void onSubtitleLongClicked() {
        if (getPlayer() == null) {
            return;
        }

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
                                setSubtitleFormat(format);
                            },
                            getContext().getString(R.string.subtitles_disabled)
                        )
                    );
                    settingsPresenter.showDialog();
                }
            )
        );

        OptionCategory stylesCategory = AppDialogUtil.createSubtitleStylesCategory(getContext());
        settingsPresenter.appendRadioCategory(stylesCategory.title, stylesCategory.options);

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

        if (isEmbedPlayer()) {
            return;
        }

        if (getAppDialogPresenter().isDialogShown()) {
            getPlayer().showOverlay(true);
        }
        
    }

    @Override
    public void onSeekEnd() {
        if (getPlayer() == null) {
            return;
        }
    }

    @Override
    public void onViewResumed() {
        if (getPlayer() == null) {
            return;
        }

        // Reset temp mode.
        getSearchData().setTempBackgroundModeClass(null);

        getPlayer().showSubtitles(true);

        // Maybe dialog just closed. Reset timeout just in case.
        enableUiAutoHideTimeout();
    }

    @Override
    public void onViewPaused() {
        if (getPlayer() != null && getPlayer().isInPIPMode()) {
            // UI couldn't be properly displayed in PIP mode
            getPlayer().showOverlay(false);
            getPlayer().showSubtitles(false);
        }
    }

    private void resetButtonStates() {
        if (getPlayer() == null) {
            return;
        }

        getPlayer().setButtonState(R.id.action_thumbs_up, PlayerUI.BUTTON_OFF);
        getPlayer().setButtonState(R.id.action_thumbs_down, PlayerUI.BUTTON_OFF);
        getPlayer().setChannelIcon(null);
        getPlayer().setButtonState(R.id.action_playlist_add, PlayerUI.BUTTON_OFF);
        getPlayer().setButtonState(R.id.lb_control_closed_captioning, PlayerUI.BUTTON_OFF);
        getPlayer().setButtonState(R.id.action_video_speed, PlayerUI.BUTTON_OFF);
        getPlayer().setButtonState(R.id.action_chat, PlayerUI.BUTTON_OFF);
        getPlayer().setButtonState(R.id.action_subscribe, PlayerUI.BUTTON_OFF);
    }

    @Override
    public void onEngineReleased() {
        Log.d(TAG, "Engine released. Disabling all callbacks...");
        mEngineReady = false;

        disposeTimeouts();
    }

    @Override
    public void onMetadata(MediaItemMetadata metadata) {

        mIsMetadataLoaded = true;

        getPlayer().loadStoryboard();
        
        getPlayer().setButtonState(R.id.action_thumbs_up, metadata.getLikeStatus() == MediaItemMetadata.LIKE_STATUS_LIKE ? PlayerUI.BUTTON_ON : PlayerUI.BUTTON_OFF);
        getPlayer().setButtonState(R.id.action_thumbs_down, metadata.getLikeStatus() == MediaItemMetadata.LIKE_STATUS_DISLIKE ? PlayerUI.BUTTON_ON : PlayerUI.BUTTON_OFF);

        getPlayer().setChannelIcon(metadata.getAuthorImageUrl());

        setPlaylistAddButtonStateCached();
        setSubtitleButtonState();

        getPlayer().setButtonState(R.id.action_rotate, getPlayerData().getRotationAngle() == 0 ? PlayerUI.BUTTON_OFF : PlayerUI.BUTTON_ON);
        getPlayer().setButtonState(R.id.action_subscribe, metadata.isSubscribed() ? PlayerUI.BUTTON_ON : PlayerUI.BUTTON_OFF);

    }

    @Override
    public void onSuggestionItemLongClicked(Video item) {
        VideoMenuPresenter.instance(getContext()).showMenu(item, (videoItem, action) -> {
            if (getPlayer() == null || item.getGroup() == null)
                return;

            if (action == VideoMenuCallback.ACTION_REMOVE_FROM_QUEUE
                    || action == VideoMenuCallback.ACTION_REMOVE_FROM_PLAYLIST
                    || action == VideoMenuCallback.ACTION_REMOVE) {
                int id = item.getGroup().getId();
                VideoGroup group = VideoGroup.from(videoItem);
                group.setId(id);
                getPlayer().removeSuggestions(group);
            } else if (action == VideoMenuCallback.ACTION_ADD_TO_QUEUE || action == VideoMenuCallback.ACTION_PLAY_NEXT) {
                String title = getContext().getString(R.string.action_playback_queue);
                int id = title.hashCode();
                Video newItem = videoItem.copy();
                VideoGroup group = VideoGroup.from(newItem, 0);
                group.setTitle(title);
                group.setId(id);
                group.setType(MediaGroup.TYPE_PLAYBACK_QUEUE);
                newItem.setGroup(group);
                if (action == VideoMenuCallback.ACTION_PLAY_NEXT) {
                    group.setAction(VideoGroup.ACTION_PREPEND);
                }
                getPlayer().updateSuggestions(group);
                getPlayer().setNextTitle(mSuggestionsController.getNext());
            }
        });
    }

    private void onDislikeClicked(boolean dislike) {
        if (getPlayer() == null)
            return;

        getPlayer().setButtonState(R.id.action_thumbs_down, !dislike ? PlayerUI.BUTTON_ON : PlayerUI.BUTTON_OFF);

        if (!mIsMetadataLoaded) {
            MessageHelpers.showMessage(getContext(), R.string.wait_data_loading);
            return;
        }

        if (!YouTubeSignInService.instance().isSigned()) {
            getPlayer().setButtonState(R.id.action_thumbs_down, PlayerUI.BUTTON_OFF);
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
        getPlayer().setButtonState(R.id.action_thumbs_up, !like ? PlayerUI.BUTTON_ON : PlayerUI.BUTTON_OFF);

        if (!mIsMetadataLoaded) {
            MessageHelpers.showMessage(getContext(), R.string.wait_data_loading);
            return;
        }

        if (!YouTubeSignInService.instance().isSigned()) {
            getPlayer().setButtonState(R.id.action_thumbs_up, PlayerUI.BUTTON_OFF);
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

        if (!mIsMetadataLoaded) {
            MessageHelpers.showMessage(getContext(), R.string.wait_data_loading);
            return;
        }

        Video video = getVideo();

        if (video == null) {
            return;
        }

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

    private void onSearchClicked() {
        startTempBackgroundMode(SearchPresenter.class);
        SearchPresenter.instance(getContext()).startSearch(null);
    }
    
    private void onPipClicked() {
        getPlayer().showOverlay(false);
        getPlayer().blockEngine(true);
        getPlayer().finish();
    }

    @Override
    public void onButtonClicked(int buttonId, int buttonState) {
        
        if (buttonId == R.id.action_rotate) {
            onRotate();

        } else if (buttonId == R.id.action_flip) {
            onFlip();

        } else if (buttonId == R.id.action_subscribe) {
            onSubscribe(buttonState);

        } else if (buttonId == R.id.action_repeat) {
            applyRepeatMode(buttonState);

        } else if (buttonId == R.id.action_channel) {
            openChannel();

        } else if (buttonId == R.id.action_playback_queue) {
            AppDialogUtil.showPlaybackQueueDialog(getContext(), item -> getMainController().onNewVideo(item));

        } else if (buttonId == R.id.action_info) {
            onVideoInfoClicked();

        } else if (buttonId == R.id.action_pip) {
            onPipClicked();

        } else if (buttonId == R.id.action_search) {
            onSearchClicked();

        } else if (buttonId == R.id.action_playlist_add) {
            onPlaylistAddClicked();

        } else if (buttonId == R.id.lb_control_closed_captioning) {
            onSubtitleClicked(buttonState == PlayerUI.BUTTON_ON);

        } else if (buttonId == R.id.action_thumbs_down) {
            onDislikeClicked(buttonState == PlayerUI.BUTTON_ON);
            
        } else if (buttonId == R.id.action_thumbs_up) {
            onLikeClicked(buttonState == PlayerUI.BUTTON_ON);
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

    private void disposeTimeouts() {
        disableUiAutoHideTimeout();
        disableSuggestionsResetTimeout();
    }

    private void callMediaItemObservable(MediaItemObservable callable) {
        Video video = getVideo();

        if (video == null) {
            Log.e(TAG, "Seems that video isn't initialized yet. Cancelling...");
            return;
        }

        Observable<Void> observable = callable.call(video.mediaItem != null ? video.mediaItem : video.toMediaItem());

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

    private interface MediaItemObservable {
        Observable<Void> call(MediaItem item);
    }

    private void setPlaylistAddButtonStateCached() {
        if (getVideo() == null) {
            return;
        }

        String videoId = getVideo().videoId;
        mPlaylistInfos = null;
        Disposable playlistsInfoAction =
                YouTubeServiceManager.instance().getMediaItemService().getPlaylistsInfoObserve(videoId)
                        .subscribe(
                                videoPlaylistInfos -> {
                                    mPlaylistInfos = videoPlaylistInfos;
                                    setPlaylistAddButtonState();
                                },
                                error -> Log.e(TAG, "Add to recent playlist error: %s", error.getMessage())
                        );
    }

    private void setPlaylistAddButtonState() {
        if (mPlaylistInfos == null || getPlayer() == null) {
            return;
        }

        boolean isSelected = false;
        for (PlaylistInfo playlistInfo : mPlaylistInfos) {
            if (playlistInfo.isSelected()) {
                isSelected = true;
                break;
            }
        }

        getPlayer().setButtonState(R.id.action_playlist_add, isSelected ? PlayerUI.BUTTON_ON : PlayerUI.BUTTON_OFF);
    }

    private void setSubtitleButtonState() {
        
        if (getPlayer() != null) {

            getPlayer().setButtonState(
                R.id.lb_control_closed_captioning, 
                isSubtitleSelected() ? PlayerUI.BUTTON_ON : PlayerUI.BUTTON_OFF
            );

        }

    }

    private void startTempBackgroundMode(Class<?> clazz) {
        SearchData searchData = getSearchData();
        if (searchData.isTempBackgroundModeEnabled()) {
            searchData.setTempBackgroundModeClass(clazz);
            onPipClicked();
        }
    }

    private boolean isSubtitleSelected() {
        if (getPlayer() == null) {
            return false;
        }

        List<FormatItem> subtitleFormats = getPlayer().getSubtitleFormats();

        if (subtitleFormats == null) {
            return false;
        }

        boolean isSelected = false;

        for (FormatItem subtitle : subtitleFormats) {
            if (subtitle.isSelected() && !subtitle.isDefault()) {
                isSelected = true;
                break;
            }
        }

        return isSelected;
    }

    private void onRotate() {
        if (getPlayer() == null) {
            return;
        }

        int oldRotation = getPlayerData().getRotationAngle();
        int rotation = oldRotation == 0 ? 90 : oldRotation == 90 ? 180 : oldRotation == 180 ? 270 : 0;
        getPlayer().setRotationAngle(rotation);
        getPlayer().setButtonState(R.id.action_rotate, rotation == 0 ? PlayerUI.BUTTON_OFF : PlayerUI.BUTTON_ON);
        getPlayerData().setRotationAngle(rotation);
    }

    private void onFlip() {
        if (getPlayer() == null) {
            return;
        }

        boolean flipEnabled = getPlayerData().isVideoFlipEnabled();
        boolean newFlipEnabled = !flipEnabled;
        getPlayer().setVideoFlipEnabled(newFlipEnabled);
        getPlayer().setButtonState(R.id.action_flip, newFlipEnabled ? PlayerUI.BUTTON_ON : PlayerUI.BUTTON_OFF);
        getPlayerData().setVideoFlipEnabled(newFlipEnabled);
    }

    private void onSubscribe(int buttonState) {
        if (getVideo() == null) {
            return;
        }

        if (!mIsMetadataLoaded) {
            MessageHelpers.showMessage(getContext(), R.string.wait_data_loading);
            return;
        }

        if (buttonState == PlayerUI.BUTTON_OFF) {
            callMediaItemObservable(mMediaItemService::subscribeObserve);
        } else {
            callMediaItemObservable(mMediaItemService::unsubscribeObserve);
        }

        getVideo().isSubscribed = buttonState == PlayerUI.BUTTON_OFF;
        getPlayer().setButtonState(R.id.action_subscribe, buttonState == PlayerUI.BUTTON_OFF ? PlayerUI.BUTTON_ON : PlayerUI.BUTTON_OFF);
    }

    private void applyRepeatMode(int buttonState) {
        int nextMode = getNextRepeatMode(buttonState);

        getPlayerData().setPlaybackMode(nextMode);
        getPlayer().setButtonState(R.id.action_repeat, nextMode);
    }

    private void showPlaybackModeDialog(int buttonState) {
        OptionCategory category = AppDialogUtil.createPlaybackModeCategory(
                getContext(), () -> {
                    getPlayer().setButtonState(R.id.action_repeat, getPlayerData().getPlaybackMode());
                });

        AppDialogPresenter settingsPresenter = getAppDialogPresenter();
        settingsPresenter.appendRadioCategory(category.title, category.options);
        settingsPresenter.showDialog();
    }

    private int getNextRepeatMode(int buttonState) {
        Integer[] modeList = {PlayerConstants.PLAYBACK_MODE_ALL, PlayerConstants.PLAYBACK_MODE_ONE, PlayerConstants.PLAYBACK_MODE_SHUFFLE,
                PlayerConstants.PLAYBACK_MODE_LIST, PlayerConstants.PLAYBACK_MODE_REVERSE_LIST, PlayerConstants.PLAYBACK_MODE_PAUSE, PlayerConstants.PLAYBACK_MODE_CLOSE};
        int nextMode = Helpers.getNextValue(modeList, buttonState);
        return nextMode;
    }

    private void reorderSubtitles(List<FormatItem> subtitleFormats) {
        if (subtitleFormats == null || subtitleFormats.isEmpty()) {
            return;
        }

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
        if (getVideo() == null || getVideo().notificationStates == null) {
            return;
        }

        AppDialogPresenter settingsPresenter = getAppDialogPresenter();

        List<OptionItem> items = new ArrayList<>();

        for (NotificationState item : getVideo().notificationStates) {
            items.add(UiOptionItem.from(item.getTitle(), optionItem -> {
                if (optionItem.isSelected()) {
                    MediaServiceManager.instance().setNotificationState(item, error -> MessageHelpers.showMessage(getContext(), error.getLocalizedMessage()));
                    getVideo().isSubscribed = true;
                    getPlayer().setButtonState(R.id.action_subscribe, PlayerUI.BUTTON_ON);
                }
            }, item.isSelected()));
        }

        settingsPresenter.appendRadioCategory(getContext().getString(R.string.header_notifications), items);
        settingsPresenter.showDialog(getContext().getString(R.string.header_notifications));
    }

    private void openChannel() {
        startTempBackgroundMode(ChannelPresenter.class);
        ChannelPresenter.instance(getContext()).openChannel(getVideo());
    }
}
