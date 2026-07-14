package minefarts.smarttube.app.presenters.dialogs.menu;

import android.content.Context;

import minefarts.smarttube.utils.MediaItemService;
import minefarts.smarttube.app.models.playback.BasePlayerController;
import minefarts.smarttube.utils.data.PlaylistInfo;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.helpers.MessageHelpers;
import minefarts.smarttube.utils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.Queue;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.playback.controllers.CommentsController;
import minefarts.smarttube.app.models.playback.service.VideoStateService;
import minefarts.smarttube.app.models.playback.service.State;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.app.presenters.BrowsePresenter;
import minefarts.smarttube.app.presenters.ChannelPresenter;
import minefarts.smarttube.app.presenters.ChannelUploadsPresenter;
import minefarts.smarttube.app.presenters.PlaybackPresenter;
import minefarts.smarttube.app.presenters.dialogs.menu.providers.ContextMenuManager;
import minefarts.smarttube.app.presenters.dialogs.menu.providers.ContextMenuProvider;
import minefarts.smarttube.app.views.ChannelUploadsView;
import minefarts.smarttube.prefs.GeneralData;
import minefarts.smarttube.prefs.MainUIData;
import minefarts.smarttube.utils.AppDialogUtil;
import minefarts.smarttube.ui.playback.actions.SubscribeAction;
import minefarts.smarttube.app.models.playback.controllers.VideoStateController;
import minefarts.smarttube.utils.service.data.MediaGroup;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoMenuPresenter extends BaseMenuPresenter {

    private static final String TAG = VideoMenuPresenter.class.getSimpleName();
    
    private final MediaItemService mMediaItemService;
    private final AppDialogPresenter mDialogPresenter;
    private final VideoStateService mVideoStateService;
    private final VideoStateController mVideoStateController;

    private Disposable mAddToPlaylistAction;
    private Disposable mNotInterestedAction;
    private Disposable mPlaylistsInfoAction;
    private Video mVideo;
    
    public static WeakReference<Video> sVideoHolder = new WeakReference<>(null);
    
    private boolean mIsNotInterestedButtonEnabled;
    private boolean mIsNotRecommendChannelEnabled;
    private boolean mIsRemoveFromHistoryButtonEnabled;
    private boolean mIsRemoveFromSubscriptionsButtonEnabled;
    private boolean mIsOpenChannelButtonEnabled;
    private boolean mIsOpenChannelUploadsButtonEnabled;
    private boolean mIsSubscribeButtonEnabled;
    private boolean mIsShareLinkButtonEnabled;
    private boolean mIsShareQRLinkButtonEnabled;
    private boolean mIsShareEmbedLinkButtonEnabled;
    private boolean mIsAddToPlaylistButtonEnabled;
    private boolean mIsAddToRecentPlaylistButtonEnabled;
    private boolean mIsOpenPlaylistButtonEnabled;
    private boolean mIsAddToPlaybackQueueButtonEnabled;
    private boolean mIsPlayNextButtonEnabled;
    private boolean mIsOpenDescriptionButtonEnabled;
    private boolean mIsOpenCommentsButtonEnabled;
    private boolean mIsPlayVideoButtonEnabled;
    private boolean mIsPlaylistOrderButtonEnabled;
    private boolean mIsMarkAsWatchedButtonEnabled;

    private VideoMenuCallback mCallback;
    private List<PlaylistInfo> mPlaylistInfos;
    private final Map<Long, MenuAction> mMenuMapping = new HashMap<>();

    public interface VideoMenuCallback {
        int ACTION_UNDEFINED = 0;
        int ACTION_UNSUBSCRIBE = 1;
        int ACTION_REMOVE = 2;
        int ACTION_REMOVE_FROM_PLAYLIST = 3;
        int ACTION_REMOVE_FROM_QUEUE = 4;
        int ACTION_ADD_TO_QUEUE = 5;
        int ACTION_PLAY_NEXT = 6;
        int ACTION_REMOVE_AUTHOR = 7;
        void onItemAction(Video videoItem, int action);
    }

    public static class MenuAction {
        private final Runnable mAction;
        private final boolean mIsAuth;

        public MenuAction(Runnable action, boolean isAuth) {
            this.mAction = action;
            this.mIsAuth = isAuth;
        }

        public void run() {
            mAction.run();
        }

        public boolean isAuth() {
            return mIsAuth;
        }
    }

    private VideoMenuPresenter(Context context) {
        super();

        setContext(context);        
        updateEnabledMenuItems();
        
        mMediaItemService = BasePlayerController.getMediaItemService();
        mDialogPresenter = AppDialogPresenter.instance(context);
        mVideoStateService = VideoStateService.instance(context);
        mVideoStateController = new VideoStateController();

        mMenuMapping.put(
            MainUIData.MENU_ITEM_PLAY_VIDEO, 
            new MenuAction(this::appendPlayVideoButton, false)
        );
        
        mMenuMapping.put(
            MainUIData.MENU_ITEM_REMOVE_FROM_HISTORY, 
            new MenuAction(this::appendRemoveFromHistoryButton, false)
        );
        
        mMenuMapping.put(
            MainUIData.MENU_ITEM_RECENT_PLAYLIST, 
            new MenuAction(this::appendAddToRecentPlaylistButton, false)
        );
        
        mMenuMapping.put(
            MainUIData.MENU_ITEM_ADD_TO_PLAYLIST, 
            new MenuAction(this::appendAddToPlaylistButton, false)
        );
        
        mMenuMapping.put(
            MainUIData.MENU_ITEM_CREATE_PLAYLIST, 
            new MenuAction(this::appendCreatePlaylistButton, false)
        );
        
        mMenuMapping.put(
            MainUIData.MENU_ITEM_RENAME_PLAYLIST, 
            new MenuAction(this::appendRenamePlaylistButton, false)
        );
        
        mMenuMapping.put(
            MainUIData.MENU_ITEM_ADD_TO_NEW_PLAYLIST, 
            new MenuAction(this::appendAddToNewPlaylistButton, false)
        );
        
        mMenuMapping.put(
            MainUIData.MENU_ITEM_NOT_INTERESTED, 
            new MenuAction(this::appendNotInterestedButton, true)
        );

        mMenuMapping.put(
            MainUIData.MENU_ITEM_NOT_RECOMMEND_CHANNEL, 
            new MenuAction(this::appendNotRecommendChannelButton, true)
        );
        
        mMenuMapping.put(
            MainUIData.MENU_ITEM_REMOVE_FROM_SUBSCRIPTIONS, 
            new MenuAction(() -> { appendRemoveFromSubscriptionsButton(); appendRemoveFromNotificationsButton(); }, true)
        );
        
        mMenuMapping.put(
            MainUIData.MENU_ITEM_MARK_AS_WATCHED, 
            new MenuAction(this::appendMarkAsWatchedButton, false)
        );
        
        mMenuMapping.put(
            MainUIData.MENU_ITEM_PLAYLIST_ORDER, 
            new MenuAction(this::appendPlaylistOrderButton, true)
        );
        
        mMenuMapping.put(
            MainUIData.MENU_ITEM_ADD_TO_QUEUE, 
            new MenuAction(this::appendAddToPlaybackQueueButton, false)
        );
        
        mMenuMapping.put(
            MainUIData.MENU_ITEM_PLAY_NEXT, 
            new MenuAction(this::appendPlayNextButton, false)
        );
        
        mMenuMapping.put(
            MainUIData.MENU_ITEM_OPEN_CHANNEL, 
            new MenuAction(this::appendOpenChannelButton, false)
        );
        
        mMenuMapping.put(
            MainUIData.MENU_ITEM_OPEN_PLAYLIST, 
            new MenuAction(this::appendOpenPlaylistButton, false)
        );

        mMenuMapping.put(
            MainUIData.MENU_ITEM_SUBSCRIBE, 
            new MenuAction(this::appendSubscribeButton, false)
        );

        mMenuMapping.put(
            MainUIData.MENU_ITEM_EXCLUDE_FROM_CONTENT_BLOCK, 
            new MenuAction(this::appendToggleExcludeFromContentBlockButton, false)
        );

        mMenuMapping.put(
            MainUIData.MENU_ITEM_PIN_TO_SIDEBAR, 
            new MenuAction(this::appendTogglePinVideoToSidebarButton, false)
        );

        mMenuMapping.put(
            MainUIData.MENU_ITEM_SAVE_REMOVE_PLAYLIST, 
            new MenuAction(this::appendSaveRemovePlaylistButton, false)
        );

        mMenuMapping.put(
            MainUIData.MENU_ITEM_OPEN_DESCRIPTION, 
            new MenuAction(this::appendOpenDescriptionButton, false)
        );

        mMenuMapping.put(
            MainUIData.MENU_ITEM_SHARE_LINK, 
            new MenuAction(this::appendShareLinkButton, false)
        );

        mMenuMapping.put(
            MainUIData.MENU_ITEM_SHARE_QR_LINK, 
            new MenuAction(this::appendShareQRLinkButton, false)
        );

        mMenuMapping.put(
            MainUIData.MENU_ITEM_SHARE_EMBED_LINK, 
            new MenuAction(this::appendShareEmbedLinkButton, false)
        );

        mMenuMapping.put(
            MainUIData.MENU_ITEM_SELECT_ACCOUNT, 
            new MenuAction(this::appendAccountSelectionButton, false)
        );
        
        mMenuMapping.put(
            MainUIData.MENU_ITEM_OPEN_COMMENTS, 
            new MenuAction(this::appendOpenCommentsButton, false)
        );

        for (ContextMenuProvider provider : new ContextMenuManager(getContext()).getProviders()) {
            
            if (provider.getMenuType() == ContextMenuProvider.MENU_TYPE_VIDEO) {
             
                mMenuMapping.put(
                    provider.getId(), 
                    new MenuAction(
                        () -> appendContextMenuItem(provider),
                        false
                    )
                );

            }

        }

    }

    public static VideoMenuPresenter instance(Context context) {
        return new VideoMenuPresenter(context);
    }

    @Override
    protected Video getVideo() {
        return mVideo;
    }

    @Override
    protected AppDialogPresenter getDialogPresenter() {
        return mDialogPresenter;
    }

    @Override
    protected VideoMenuCallback getCallback() {
        return mCallback;
    }

    public void showMenu(Video video, VideoMenuCallback callback) {
        mCallback = callback;
        showMenu(video);
    }

    public void showMenu(Video video) {
        if (video == null) return;

        mVideo = video;
        sVideoHolder = new WeakReference<>(video);

        BasePlayerController.authCheck(
            
            () -> {
                mPlaylistInfos = null;
                RxHelper.disposeActions(mPlaylistsInfoAction);
                if (isAddToRecentPlaylistButtonEnabled()) {
                    mPlaylistsInfoAction = mMediaItemService.getPlaylistsInfoObserve(mVideo.videoId).subscribe(
                        videoPlaylistInfos -> {
                            mPlaylistInfos = videoPlaylistInfos;
                            prepareAndShowDialogSigned();
                        },
                        error -> Log.e(TAG, "Add to recent playlist error: %s", error.getMessage())
                    );
                } else {
                    prepareAndShowDialogSigned();
                }
            }, 
            
            () -> {
                if (getContext() == null) return;

                for (Long menuItem : MainUIData.instance(getContext()).getMenuItemsOrdered()) {
                    MenuAction menuAction = mMenuMapping.get(menuItem);
                    if (menuAction != null && !menuAction.isAuth()) {
                        menuAction.run();
                    }
                }

                if (!mDialogPresenter.isEmpty()) {
                    String title = mVideo != null ? mVideo.getTitle() : null;
                    mDialogPresenter.showDialog(title);
                }
            }
        
        );
    }

    private void prepareAndShowDialogSigned() {

        if (getContext() == null) return;

        for (Long menuItem : MainUIData.instance(getContext()).getMenuItemsOrdered()) {
            MenuAction menuAction = mMenuMapping.get(menuItem);
            if (menuAction != null) {
                menuAction.run();
            }
        }

        if (!mDialogPresenter.isEmpty()) {
            String title = mVideo != null ? mVideo.getTitle() : null;
            // No need to add author because: 1) This could be a channel card. 2) This info isn't so important.
            mDialogPresenter.showDialog(title);
        }

    }

    private void appendAddToPlaylistButton() {
        if (!mIsAddToPlaylistButtonEnabled) return;

        if (mVideo == null || !mVideo.hasVideo() || mVideo.isPlaylistAsChannel()) return;

        getDialogPresenter().appendSingleButton(
                UiOptionItem.from(
                        getContext().getString(R.string.dialog_add_to_playlist),
                        optionItem -> AppDialogUtil.showAddToPlaylistDialog(getContext(), mVideo, mCallback)
                ));
    }

    private void appendAddToRecentPlaylistButton() {
        if (!isAddToRecentPlaylistButtonEnabled()) return;

        String playlistId = GeneralData.instance(getContext()).getLastPlaylistId();
        String playlistTitle = GeneralData.instance(getContext()).getLastPlaylistTitle();

        if (playlistId == null || playlistTitle == null) return;

        appendSimpleAddToRecentPlaylistButton(playlistId, playlistTitle);
    }

    private boolean isAddToRecentPlaylistButtonEnabled() {
        return mIsAddToPlaylistButtonEnabled && mIsAddToRecentPlaylistButtonEnabled && mVideo != null && mVideo.hasVideo();
    }

    private void appendSimpleAddToRecentPlaylistButton(String playlistId, String playlistTitle) {
        
        if (mPlaylistInfos == null) return;

        boolean isSelected = false;
        for (PlaylistInfo playlistInfo : mPlaylistInfos) {
            if (playlistInfo.getPlaylistId().equals(playlistId)) {
                isSelected = playlistInfo.isSelected();
                break;
            }
        }
        boolean add = !isSelected;
        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(getContext().getString(
                        add ? R.string.dialog_add_to : R.string.dialog_remove_from, playlistTitle),
                        optionItem -> addRemoveFromPlaylist(playlistId, playlistTitle, add)
                )
        );
    }

    private void appendOpenChannelButton() {

        if (!mIsOpenChannelButtonEnabled) return;

        if (!ChannelPresenter.canOpenChannel(mVideo)) return;

        // Prepare to special type of channels that work as playlist
        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(getContext().getString(
                        mVideo.isPlaylistAsChannel() ? R.string.open_playlist : R.string.open_channel), optionItem -> {
                    BasePlayerController.chooseChannelPresenter(getContext(), mVideo);
                    mDialogPresenter.closeDialog();
                }));
    }

    private void appendOpenPlaylistButton() {

        if (!mIsOpenPlaylistButtonEnabled) return;

        // Check view to allow open playlist in grid
        if (mVideo == null || !mVideo.hasPlaylist() || (getViewManager().getTopView() == ChannelUploadsView.class && mVideo.belongsToSamePlaylistGroup())) return;

        // Prepare to special type of channels that work as playlist
        if (mVideo.isPlaylistAsChannel() && ChannelPresenter.canOpenChannel(mVideo)) return;

        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(getContext().getString(R.string.open_playlist), optionItem -> ChannelUploadsPresenter.instance(getContext()).openChannel(mVideo)));
    }

    private void appendNotInterestedButton() {

        if (mVideo == null || mVideo.mediaItem == null || mVideo.mediaItem.getFeedbackToken() == null) return;

        if (!mVideo.belongsToHome() || !mIsNotInterestedButtonEnabled) return;

        RxHelper.disposeActions(mNotInterestedAction);

        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(getContext().getString(R.string.not_interested), optionItem -> {
                    mNotInterestedAction = mMediaItemService.markAsNotInterestedObserve(mVideo.mediaItem.getFeedbackToken())
                            .subscribe(
                                    var -> {},
                                    error -> Log.e(TAG, "Mark as 'not interested' error: %s", error.getMessage()),
                                    () -> {
                                        if (mCallback != null) {
                                            mCallback.onItemAction(mVideo, VideoMenuCallback.ACTION_REMOVE);
                                        } else {
                                            MessageHelpers.showMessage(getContext(), R.string.you_wont_see_this_video);
                                        }
                                    }
                            );
                    mDialogPresenter.closeDialog();
                }));
    }

    private void appendNotRecommendChannelButton() {

        if (mVideo == null || mVideo.mediaItem == null || mVideo.mediaItem.getFeedbackToken2() == null) return;

        if (!mVideo.belongsToHome() || !mIsNotRecommendChannelEnabled) return;

        RxHelper.disposeActions(mNotInterestedAction);

        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(getContext().getString(R.string.not_recommend_channel), optionItem -> {
                    mNotInterestedAction = mMediaItemService.markAsNotInterestedObserve(mVideo.mediaItem.getFeedbackToken2())
                            .subscribe(
                                    var -> {},
                                    error -> Log.e(TAG, "Mark as 'not interested' error: %s", error.getMessage()),
                                    () -> {
                                        if (mCallback != null) {
                                            mCallback.onItemAction(mVideo, VideoMenuCallback.ACTION_REMOVE);
                                        } else {
                                            MessageHelpers.showMessage(getContext(), R.string.you_wont_see_this_video);
                                        }
                                    }
                            );
                    mDialogPresenter.closeDialog();
                }));
    }

    private void appendRemoveFromHistoryButton() {
        if (mVideo == null || !mVideo.belongsToHistory() || !mIsRemoveFromHistoryButtonEnabled) return;

        RxHelper.disposeActions(mNotInterestedAction);

        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(getContext().getString(R.string.remove_from_history), optionItem -> {
                    if (mVideo.mediaItem == null || mVideo.mediaItem.getFeedbackToken() == null) {
                        onRemoveFromHistoryDone();
                    } else {
                        mNotInterestedAction = mMediaItemService.markAsNotInterestedObserve(mVideo.mediaItem.getFeedbackToken())
                                .subscribe(
                                        var -> {},
                                        error -> Log.e(TAG, "Remove from history error: %s", error.getMessage()),
                                        this::onRemoveFromHistoryDone
                                );
                    }
                    mDialogPresenter.closeDialog();
                }));
    }

    private void onRemoveFromHistoryDone() {

        if (mCallback != null) {
            mCallback.onItemAction(mVideo, VideoMenuCallback.ACTION_REMOVE);
        } else {
            MessageHelpers.showMessage(getContext(), R.string.removed_from_history);
        }
        
        mVideoStateService.removeByVideoId(mVideo.videoId);
    
    }

    private void appendRemoveFromSubscriptionsButton() {
        if (mVideo == null || mVideo.mediaItem == null || mVideo.mediaItem.getFeedbackToken() == null) return;

        if (!mVideo.belongsToSubscriptions() || !mIsRemoveFromSubscriptionsButtonEnabled) return;

        RxHelper.disposeActions(mNotInterestedAction);

        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(getContext().getString(R.string.remove_from_subscriptions), optionItem -> {
                    mNotInterestedAction = mMediaItemService.markAsNotInterestedObserve(mVideo.mediaItem.getFeedbackToken())
                            .subscribe(
                                    var -> {},
                                    error -> Log.e(TAG, "Remove from subscriptions error: %s", error.getMessage()),
                                    () -> {
                                        if (mCallback != null) {
                                            mCallback.onItemAction(mVideo, VideoMenuCallback.ACTION_REMOVE);
                                        }
                                    }
                            );
                    mDialogPresenter.closeDialog();
                }));
    }

    private void appendRemoveFromNotificationsButton() {
        if (mVideo == null || mVideo.mediaItem == null) return;

        if (!mVideo.belongsToNotifications() || !mIsRemoveFromSubscriptionsButtonEnabled) return;

        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(getContext().getString(R.string.remove_from_subscriptions), optionItem -> {
                    BasePlayerController.hideNotification(mVideo);
                    if (mCallback != null) {
                        mCallback.onItemAction(mVideo, VideoMenuCallback.ACTION_REMOVE);
                    }
                    mDialogPresenter.closeDialog();
                }));
    }

    private void appendMarkAsWatchedButton() {
        if (mVideo == null || !mVideo.hasVideo() || !mIsMarkAsWatchedButtonEnabled) return;

        mDialogPresenter.appendSingleButton(UiOptionItem.from(
            getContext().getString(R.string.mark_as_watched), 
            optionItem -> {
                mVideoStateController.updateHistory(
                    mVideo, 
                    mVideo.getDurationMs() - 1000L
                );
                mDialogPresenter.closeDialog();
            }
        ));
        
    }

    private void appendShareLinkButton() {
        if (!mIsShareLinkButtonEnabled) return;

        AppDialogUtil.appendShareLinkDialogItem(getContext(), mDialogPresenter, mVideo);
    }

    private void appendShareQRLinkButton() {
        if (!mIsShareQRLinkButtonEnabled) return;

        AppDialogUtil.appendShareQRLinkDialogItem(getContext(), mDialogPresenter, mVideo);
    }

    private void appendShareEmbedLinkButton() {
        if (!mIsShareEmbedLinkButtonEnabled) return;

        AppDialogUtil.appendShareEmbedLinkDialogItem(getContext(), mDialogPresenter, mVideo);
    }

    private void appendOpenDescriptionButton() {
        if (!mIsOpenDescriptionButtonEnabled || mVideo == null) return;

        if (mVideo.videoId == null) return;

        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(getContext().getString(R.string.action_video_info),
                        optionItem -> {
                            MessageHelpers.showMessage(getContext(), R.string.wait_data_loading);
                            BasePlayerController.loadMetadata(mVideo, metadata -> {
                                String description = metadata.getDescription();
                                if (description != null) {
                                    showLongTextDialog(description);
                                } else {
                                    BasePlayerController.loadFormatInfo(mVideo, formatInfo -> {
                                        String newDescription = formatInfo.getDescription();
                                        if (newDescription != null) {
                                            showLongTextDialog(newDescription);
                                        } else {
                                            MessageHelpers.showMessage(getContext(), R.string.description_not_found);
                                        }
                                    });
                                }
                            });
                        }
                ));
    }

    private void appendOpenCommentsButton() {
        if (!mIsOpenCommentsButtonEnabled 
            || mVideo == null
            || mVideo.videoId == null 
            || mVideo.isLive 
            || mVideo.isUpcoming
        ) return;

        mDialogPresenter.appendSingleButton(UiOptionItem.from(
            getContext().getString(R.string.open_comments),
            optionItem -> {
                MessageHelpers.showMessage(getContext(), R.string.wait_data_loading);
                BasePlayerController.loadMetadata(mVideo, metadata -> {
                    CommentsController controller = new CommentsController();
                    controller.onInit();
                    controller.onMetadata(metadata);
                    controller.onButtonClicked(R.id.action_chat, 1);
                });
            }
        ));
    }

    private void appendPlayVideoButton() {
        if (!mIsPlayVideoButtonEnabled || mVideo == null || mVideo.videoId == null) return;

        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(getContext().getString(R.string.play_video),
                        optionItem -> {
                            PlaybackPresenter.instance(getContext()).openVideo(mVideo);
                            mDialogPresenter.closeDialog();
                        }
                ));
    }

    private void showLongTextDialog(String description) {
        mDialogPresenter.appendLongTextCategory(mVideo.getTitle(), UiOptionItem.from(description));
        mDialogPresenter.showDialog(mVideo.getTitle());
    }

    private void appendSubscribeButton() {

        if (!mIsSubscribeButtonEnabled 
            || mVideo == null 
            || mVideo.isPlaylistAsChannel() 
            || (!mVideo.isChannel() && !mVideo.hasVideo())
        ) return;

        SubscribeAction.refresh(mVideo);

        mDialogPresenter.appendSingleButton(UiOptionItem.from(
            mVideo.isSubscribed ? "Unsubscribe" : "Subscribe",
            oi -> SubscribeAction.toggle(mVideo)
        ));

    }

    private void appendAddToPlaybackQueueButton() {
        if (!mIsAddToPlaybackQueueButtonEnabled) return;

        if (mVideo == null || !mVideo.hasVideo()) return;

        // Toggle between add/remove while dialog is opened
        boolean containsVideo = Queue.contains(mVideo);

        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(
                        getContext().getString(containsVideo ? R.string.remove_from_playback_queue : R.string.add_to_playback_queue),
                        optionItem -> {
                            if (containsVideo) {
                                Queue.remove(mVideo);
                                if (mCallback != null) {
                                    mCallback.onItemAction(mVideo, VideoMenuCallback.ACTION_REMOVE_FROM_QUEUE);
                                }
                            } else {
                                mVideo.fromQueue = true;
                                Queue.add(mVideo);
                                if (mCallback != null) {
                                    mCallback.onItemAction(mVideo, VideoMenuCallback.ACTION_ADD_TO_QUEUE);
                                }
                            }

                            closeDialog();
                            MessageHelpers.showMessage(getContext(), String.format("%s: %s",
                                    mVideo.getAuthor(),
                                    getContext().getString(containsVideo ? R.string.removed_from_playback_queue : R.string.added_to_playback_queue))
                            );
                        }));
    }

    private void appendPlayNextButton() {

        if (!mIsPlayNextButtonEnabled) return;

        if (mVideo == null || !mVideo.hasVideo()) return;

        if (Helpers.equals(Queue.getNext(), mVideo)) return;

        mDialogPresenter.appendSingleButton(UiOptionItem.from(
            getContext().getString(R.string.play_next),
            optionItem -> {
                mVideo.fromQueue = true;
                Queue.next(mVideo);
                if (mCallback != null) {
                    mCallback.onItemAction(mVideo, VideoMenuCallback.ACTION_PLAY_NEXT);
                }

                closeDialog();
                MessageHelpers.showMessage(getContext(), String.format("%s: %s",
                    mVideo.getAuthor(),
                    getContext().getString(R.string.play_next))
                );
            }
        ));

    }

    private void appendPlaylistOrderButton() {
        if (!mIsPlaylistOrderButtonEnabled) return;

        BrowsePresenter presenter = BrowsePresenter.instance(getContext());

        if (mVideo == null || !(presenter.isPlaylistsSection() && presenter.inForeground())) return;

        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(getContext().getString(
                        R.string.playlist_order),
                        optionItem -> AppDialogUtil.showPlaylistOrderDialog(getContext(), mVideo, mDialogPresenter::closeDialog)
                ));
    }

    private void addRemoveFromPlaylist(String playlistId, String playlistTitle, boolean add) {
        RxHelper.disposeActions(mAddToPlaylistAction);
        if (add) {
            Observable<Void> editObserve = mVideo.mediaItem != null ?
                    mMediaItemService.addToPlaylistObserve(playlistId, mVideo.mediaItem) : mMediaItemService.addToPlaylistObserve(playlistId, mVideo.videoId);
            // Handle error: Maximum playlist size exceeded (> 5000 items)
            mAddToPlaylistAction = RxHelper.execute(editObserve, error -> MessageHelpers.showLongMessage(getContext(), error.getMessage()));
            mDialogPresenter.closeDialog();
            MessageHelpers.showMessage(getContext(),
                    getContext().getString(R.string.added_to, playlistTitle));
        } else {
            // Check that the current video belongs to the right section
            if (mCallback != null && Helpers.equals(mVideo.playlistId, playlistId)) {
                mCallback.onItemAction(mVideo, VideoMenuCallback.ACTION_REMOVE_FROM_PLAYLIST);
            }
            Observable<Void> editObserve = mMediaItemService.removeFromPlaylistObserve(playlistId, mVideo.videoId);
            mAddToPlaylistAction = RxHelper.execute(editObserve);
            mDialogPresenter.closeDialog();
            MessageHelpers.showMessage(getContext(),
                    getContext().getString(R.string.removed_from, playlistTitle));
        }
    }

    @Override
    protected void updateEnabledMenuItems() {
        
        super.updateEnabledMenuItems();

        MainUIData mainUIData = MainUIData.instance(getContext());

        mIsOpenChannelUploadsButtonEnabled = true;
        mIsOpenPlaylistButtonEnabled = true;
        mIsOpenChannelButtonEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_OPEN_CHANNEL);
        mIsAddToRecentPlaylistButtonEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_RECENT_PLAYLIST);
        mIsAddToPlaybackQueueButtonEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_ADD_TO_QUEUE);
        mIsPlayNextButtonEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_PLAY_NEXT);
        mIsAddToPlaylistButtonEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_ADD_TO_PLAYLIST);
        mIsShareLinkButtonEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_SHARE_LINK);
        mIsShareQRLinkButtonEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_SHARE_QR_LINK);
        mIsShareEmbedLinkButtonEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_SHARE_EMBED_LINK);
        mIsNotInterestedButtonEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_NOT_INTERESTED);
        mIsNotRecommendChannelEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_NOT_RECOMMEND_CHANNEL);
        mIsRemoveFromHistoryButtonEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_REMOVE_FROM_HISTORY);
        mIsRemoveFromSubscriptionsButtonEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_REMOVE_FROM_SUBSCRIPTIONS);
        mIsOpenDescriptionButtonEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_OPEN_DESCRIPTION);
        mIsPlayVideoButtonEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_PLAY_VIDEO);
        mIsSubscribeButtonEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_SUBSCRIBE);
        mIsPlaylistOrderButtonEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_PLAYLIST_ORDER);
        mIsMarkAsWatchedButtonEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_MARK_AS_WATCHED);
        mIsOpenCommentsButtonEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_OPEN_COMMENTS);

    }

    private void appendContextMenuItem(ContextMenuProvider provider) {
        MainUIData mainUIData = MainUIData.instance(getContext());
        if (mainUIData.isMenuItemEnabled(provider.getId()) && provider.isEnabled(getVideo())) {
            mDialogPresenter.appendSingleButton(
                    UiOptionItem.from(getContext().getString(provider.getTitleResId()), optionItem -> provider.onClicked(getVideo(), getCallback()))
            );
        }
    }
}
