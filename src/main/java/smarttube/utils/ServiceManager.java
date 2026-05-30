package minefarts.smarttube.utils;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import minefarts.smarttube.utils.SignInService;
import minefarts.smarttube.utils.service.YouTubeLiveChatService;
import minefarts.smarttube.utils.service.YouTubeCommentsService;
import minefarts.smarttube.utils.service.YouTubeNotificationsService;
import minefarts.smarttube.utils.videoinfo.models.VideoInfo;
import minefarts.smarttube.utils.videoinfo.V2.VideoInfoService;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.service.ContentService;
import minefarts.smarttube.utils.MediaItemService;
import minefarts.smarttube.utils.NotificationsService;
import minefarts.smarttube.utils.SignInService;
import minefarts.smarttube.utils.SignInService.OnAccountChange;
import minefarts.smarttube.utils.oauth.Account;
import minefarts.smarttube.utils.service.data.MediaGroup;
import minefarts.smarttube.utils.data.MediaItem;
import minefarts.smarttube.utils.data.MediaItemFormatInfo;
import minefarts.smarttube.utils.service.data.MediaItemMetadata;
import minefarts.smarttube.utils.data.NotificationState;
import minefarts.smarttube.utils.data.PlaylistInfo;
import minefarts.smarttube.utils.helpers.MessageHelpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.rx.RxHelper;
import minefarts.smarttube.app.models.data.Queue;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.data.VideoGroup;
import minefarts.smarttube.app.presenters.ChannelPresenter;
import minefarts.smarttube.app.presenters.ChannelUploadsPresenter;
import minefarts.smarttube.prefs.AccountsData;
import minefarts.smarttube.prefs.AppPrefs;
import minefarts.smarttube.prefs.MainUIData;
import minefarts.smarttube.utils.LoadingManager;
import minefarts.smarttube.utils.Utils;
import minefarts.smarttube.utils.playlist.PlaylistService;
import minefarts.smarttube.utils.app.AppService;
import minefarts.smarttube.utils.ChannelGroupService;
import minefarts.smarttube.utils.CommentsService;
import minefarts.smarttube.utils.LiveChatService;
import minefarts.smarttube.utils.RemoteControlService;
import minefarts.smarttube.utils.channelgroups.ChannelGroupServiceImpl;
import minefarts.smarttube.google.common.locale.LocaleManager;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceManager {

    private static final String TAG = ServiceManager.class.getSimpleName();

    private static Disposable mRefreshCoreDataAction;
    private static Disposable mMetadataAction;
    private static Disposable mUploadsAction;
    private static Disposable mRowsAction;
    private static Disposable mSubscribedChannelsAction;
    private static Disposable mFormatInfoAction;
    private static Disposable mPlaylistGroupAction;
    private static Disposable mPlaylistInfosAction;
    
    private static final int MIN_GRID_GROUP_SIZE = 13;
    private static final int MIN_ROW_GROUP_SIZE = 5;

    private static final Map<Integer, Pair<Integer, Long>> mContinuations = new HashMap<>();
    private static final List<AccountChangeListener> mAccountListeners = new CopyOnWriteArrayList<>();

    public static interface OnMetadata {
        void onMetadata(MediaItemMetadata metadata);
    }

    public static interface OnMediaGroup {
        void onMediaGroup(MediaGroup group);
    }

    public static interface OnMediaGroupList {
        void onMediaGroupList(List<MediaGroup> groupList);
    }

    public static interface OnFormatInfo {
        void onFormatInfo(MediaItemFormatInfo formatInfo);
    }

    public static interface OnAccountList {
        void onAccountList(List<Account> accountList);
    }

    public static interface OnPlaylistInfos {
        void onPlaylistInfos(List<PlaylistInfo> playlistInfos);
    }

    public static interface AccountChangeListener {
        void onAccountChanged(Account account);
    }

    public static interface OnError {
        void onError(Throwable error);
    }

    public static void loadMetadata(Video video, OnMetadata onMetadata) {
        if (video == null) return;

        RxHelper.disposeActions(mMetadataAction);

        Observable<MediaItemMetadata> observable;

        // NOTE: Load suggestions from mediaItem isn't robust. Because playlistId may be initialized from RemoteControlManager.
        // Video might be loaded from Channels section (has playlistParams)
        if (video.mediaItem != null) {
            // Use additional data like playlist id
            observable = getMediaItemService().getMetadataObserve(video.mediaItem);
        } else {
            // Simply load
            observable = getMediaItemService().getMetadataObserve(video.videoId, video.getPlaylistId(), video.playlistIndex, video.playlistParams);
        }

        mMetadataAction = observable
                .subscribe(
                        onMetadata::onMetadata,
                        error -> Log.e(TAG, "loadMetadata error: %s", error.getMessage())
                );
    }

    /**
     * NOTE: Load suggestions from MediaItem isn't robust. Because playlistId may be initialized from RemoteControlManager.
     */
    public static void loadMetadata(MediaItem mediaItem, OnMetadata onMetadata) {
        if (mediaItem == null) return;

        RxHelper.disposeActions(mMetadataAction);

        Observable<MediaItemMetadata> observable;

        observable = getMediaItemService().getMetadataObserve(mediaItem);

        mMetadataAction = observable
                .subscribe(
                        onMetadata::onMetadata,
                        error -> Log.e(TAG, "loadMetadata error: %s", error.getMessage())
                );
    }

    public static void loadChannelUploads(Video item, OnMediaGroup onMediaGroup) {
        if (item == null) return;

        loadChannelUploads(item.mediaItem, onMediaGroup);
    }

    public static void loadChannelUploads(MediaItem item, OnMediaGroup onMediaGroup) {
        if (item == null) return;

        RxHelper.disposeActions(mUploadsAction);

        Observable<MediaGroup> observable = getContentService().getGroupObserve(item);

        mUploadsAction = observable
                .subscribe(
                        onMediaGroup::onMediaGroup,
                        error -> {
                            onMediaGroup.onMediaGroup(null);
                            Log.e(TAG, "loadChannelUploads error: %s", error.getMessage());
                        }
                );
    }
    
    public static void loadChannelRows(Video item, OnMediaGroupList onMediaGroupList) {
        loadChannelRows(item, onMediaGroupList, null);
    }

    public static void loadChannelRows(Video item, OnMediaGroupList onMediaGroupList, OnError onError) {
        if (item == null) return;

        RxHelper.disposeActions(mRowsAction);

        Observable<List<MediaGroup>> observable = item.mediaItem != null ?
                getContentService().getChannelObserve(item.mediaItem) : getContentService().getChannelObserve(item.channelId);

        mRowsAction = observable
                .subscribe(
                        onMediaGroupList::onMediaGroupList,
                        error -> {
                            Log.e(TAG, "loadChannelRows error: %s", error.getMessage());
                            if (onError != null) {
                                onError.onError(error);
                            }
                        }
                );
    }

    public static void loadChannelPlaylist(Video item, OnMediaGroup callback) {
        loadChannelRows(
                item,
                mediaGroupList -> callback.onMediaGroup(mediaGroupList.get(0))
        );
    }

    public static void loadFormatInfo(Video item, OnFormatInfo onFormatInfo) {
        if (item == null) return;

        RxHelper.disposeActions(mFormatInfoAction);

        Observable<MediaItemFormatInfo> observable = getMediaItemService().getFormatInfoObserve(item.videoId);

        mFormatInfoAction = observable
                .subscribe(
                        onFormatInfo::onFormatInfo,
                        error -> Log.e(TAG, "loadFormatInfo error: %s", error.getMessage())
                );
    }

    public static void loadPlaylists(Video item, OnMediaGroup onPlaylistGroup) {
        if (item == null) return;

        RxHelper.disposeActions(mPlaylistGroupAction);

        Observable<MediaGroup> observable = getContentService().getPlaylistsObserve();

        mPlaylistGroupAction = observable
                .subscribe(
                        onPlaylistGroup::onMediaGroup,
                        error -> Log.e(TAG, "loadPlaylists error: %s", error.getMessage())
                );
    }

    public static void getPlaylistInfos(OnPlaylistInfos onPlaylistInfos) {
        RxHelper.disposeActions(mPlaylistInfosAction);

        Observable<List<PlaylistInfo>> observable = getMediaItemService().getPlaylistsInfoObserve(null);

        mPlaylistInfosAction = observable
                .subscribe(
                        onPlaylistInfos::onPlaylistInfos,
                        error -> Log.e(TAG, "getPlaylistInfos error: %s", error.getMessage())
                );
    }

    public static void loadAccounts(OnAccountList onAccountList) {
        onAccountList.onAccountList(getSignInService().getAccounts());
    }

    public static void authCheck(Runnable onSuccess, Runnable onError) {
        if (onSuccess == null && onError == null) return;

        if (getSignInService().isSigned()) {
            if (onSuccess != null) {
                RxHelper.runAsync(onSuccess);
            }
        } else if (onError != null) {
            RxHelper.runAsync(onError);
        }
        
    }

    public static void disposeActions() {
        RxHelper.disposeActions(mMetadataAction, mUploadsAction, mRowsAction, mSubscribedChannelsAction);
    }

    /**
     * Most tiny ui has 8 cards in a row or 24 in grid.
     */
    public static void shouldContinueGridGroup(Context context, VideoGroup group, Runnable onNeedContinue) {
        shouldContinueTheGroup(context, group, onNeedContinue, true);
    }

    public static void shouldContinueRowGroup(Context context, VideoGroup group, Runnable onNeedContinue) {
        shouldContinueTheGroup(context, group, onNeedContinue, false);
    }

    /**
     * Most tiny ui has 8 cards in a row or 24 in grid.
     */
    public static void shouldContinueTheGroup(Context context, VideoGroup group, Runnable onNeedContinue, boolean isGrid) {
        if (shouldContinueTheGroup(context, group, isGrid) && onNeedContinue != null) {
            onNeedContinue.run();
        }
    }

    /**
     * Most tiny ui has 8 cards in a row or 24 in grid.
     */
    public static boolean shouldContinueGridGroup(Context context, VideoGroup group) {
        return shouldContinueTheGroup(context, group, true);
    }

    public static boolean shouldContinueRowGroup(Context context, VideoGroup group) {
        return shouldContinueTheGroup(context, group,false);
    }

    /**
     * Most tiny ui has 8 cards in a row or 24 in grid.
     */
    public static boolean shouldContinueTheGroup(Context context, VideoGroup group, boolean isGrid) {

        if (group == null || group.getMediaGroup() == null) {
            return false;
        }

        MediaGroup mediaGroup = group.getMediaGroup();

        Pair<Integer, Long> sizeTimestamp = mContinuations.get(group.getId());

        long currentTimeMillis = System.currentTimeMillis();
        if (sizeTimestamp != null && currentTimeMillis - sizeTimestamp.second > 3_000) { // seems that section is refreshed
            sizeTimestamp = null;
        }

        int prevSize = sizeTimestamp != null ? sizeTimestamp.first : 0;
        int newSize = mediaGroup.getMediaItems() != null ? mediaGroup.getMediaItems().size() : 0;
        int totalSize = prevSize + newSize;

        MainUIData mainUIData = MainUIData.instance(context);

        int minSize = isGrid ? MIN_GRID_GROUP_SIZE : MIN_ROW_GROUP_SIZE;
        
        boolean groupTooSmall = totalSize < minSize;

        mContinuations.put(group.getId(), new Pair<>(groupTooSmall ? totalSize : 0, currentTimeMillis));

        return groupTooSmall;
    }

    public static void clearSearchHistory() {
        RxHelper.runAsyncUser(getContentService()::clearSearchHistory);
    }

    public static void hideNotification(Video item) {
        if (item != null && item.belongsToNotifications()) {
            RxHelper.execute(getNotificationsService().hideNotificationObserve(item.mediaItem));
        }
    }

    public static void setNotificationState(NotificationState state, OnError onError) {
        RxHelper.execute(getNotificationsService().setNotificationStateObserve(state), onError::onError);
    }

    public static void removeFromWatchLaterPlaylist(Video video) {
        removeFromWatchLaterPlaylist(video, null);
    }

    public static void removeFromWatchLaterPlaylist(Video video, Runnable onSuccess) {
        if (video == null || !getSignInService().isSigned()) {
            return;
        }

        Disposable playlistsInfoAction = getMediaItemService().getPlaylistsInfoObserve(video.videoId)
                .subscribe(
                        videoPlaylistInfos -> {
                            PlaylistInfo watchLater = videoPlaylistInfos.get(0);

                            if (watchLater.isSelected()) {
                                Observable<Void> editObserve = getMediaItemService().removeFromPlaylistObserve(watchLater.getPlaylistId(), video.videoId);

                                RxHelper.execute(editObserve, () -> {
                                    if (onSuccess != null) {
                                        onSuccess.run();
                                    }
                                });
                            }
                        },
                        error -> {
                            // Fallback to something on error
                            Log.e(TAG, "Get playlists error: %s", error.getMessage());
                        }
                );
    }

    public static void addAccountListener(AccountChangeListener listener) {
        if (!mAccountListeners.contains(listener)) {
            if (listener instanceof AccountsData ||
                listener instanceof AppPrefs) {
                mAccountListeners.add(0, listener); // data classes should be called before regular listeners
            } else {
                mAccountListeners.add(listener);
            }
        }
    }

    public static void removeAccountListener(AccountChangeListener listener) {
        mAccountListeners.remove(listener);
    }

    public static Account getSelectedAccount() {
        return getSignInService().getSelectedAccount();
    }

    public static String printAccountDebugInfo() {
        return getSignInService().printDebugInfo();
    }


    public static void onAccountChanged(Account account) {
        for (AccountChangeListener listener : mAccountListeners) {
            listener.onAccountChanged(account);
        }
    }

    /**
     * Selecting right presenter for the channel.<br/>
     * Channels could be of two types: regular (usr channel) and playlist channel (contains single row, try search: 'Mon mix')
     */
    public static void chooseChannelPresenter(Context context, Video item) {

        if (item.hasVideo() || item.hasReloadPageKey()) { // an channel item from Channels section
            ChannelPresenter.instance(context).openChannel(item);
            return;
        }

        LoadingManager.showLoading(context, true);

        AtomicInteger atomicIndex = new AtomicInteger(0);

        ServiceManager.loadChannelRows(item, groups -> {
            LoadingManager.showLoading(context, false);

            if (groups == null || groups.isEmpty()) {
                return;
            }

            MediaGroup firstGroup = groups.get(0);
            int type = firstGroup.getType();

            if (type == MediaGroup.TYPE_CHANNEL_UPLOADS) {
                if (atomicIndex.incrementAndGet() == 1) {
                    ChannelUploadsPresenter.instance(context).clear();
                    ChannelUploadsPresenter.instance(context).setChannel(item);
                }
                // NOTE: Crashes RecycleView IndexOutOfBoundsException when doing add immediately after clear
                Utils.postDelayed(() -> ChannelUploadsPresenter.instance(context).update(firstGroup), 100);
            } else if (type == MediaGroup.TYPE_CHANNEL) {
                if (atomicIndex.incrementAndGet() == 1) {
                    ChannelPresenter.instance(context).clear();
                    ChannelPresenter.instance(context).setChannel(item);
                }
                // NOTE: Crashes RecycleView IndexOutOfBoundsException when doing add immediately after clear
                Utils.postDelayed(() -> ChannelPresenter.instance(context).updateRows(groups), 100);
            } else {
                MessageHelpers.showMessage(context, "Unknown type of channel");
            }
        }, error -> LoadingManager.showLoading(context, false));
    }

    public static SignInService getSignInService() {
        return SignInService.instance();
    }

    public static RemoteControlService getRemoteControlService() {
        return RemoteControlService.instance();
    }

    public static YouTubeLiveChatService getLiveChatService() {
        return YouTubeLiveChatService.instance();
    }

    public static YouTubeCommentsService getCommentsService() {
        return YouTubeCommentsService.INSTANCE;
    }

    public static ContentService getContentService() {
        return ContentService.instance();
    }

    public static MediaItemService getMediaItemService() {
        return MediaItemService.instance();
    }

    public static YouTubeNotificationsService getNotificationsService() {
        return YouTubeNotificationsService.INSTANCE;
    }

    public static ChannelGroupServiceImpl getChannelGroupService() {
        return ChannelGroupServiceImpl.INSTANCE;
    }

    public static void invalidateCache() {
        LocaleManager.unhold();
        getSignInService().invalidateCache(); // sections infinite loading fix (request timed out fix)
        getAppService().invalidateCache();
        //AppService.instance().invalidateVisitorData();
        getMediaItemService().invalidateCache();
        getVideoInfoService().resetInfoType();
    }

    public static void refreshCacheIfNeeded() {
        refreshCacheIfNeededInt();
    }

    public static void applyNoPlaybackFix() {
        getMediaItemService().invalidateCache();
        getVideoInfoService().switchNextFormat();
    }

    public static void applySubtitleFix() {
        getMediaItemService().invalidateCache();
        getVideoInfoService().switchNextSubtitle();
    }

    private static void refreshCacheIfNeededInt() {
        if (RxHelper.isAnyActionRunning(mRefreshCoreDataAction)) {
            return;
        }

        mRefreshCoreDataAction = RxHelper.execute(RxHelper.fromRunnable(getAppService()::refreshCacheIfNeeded));
    }

    @NonNull
    public static VideoInfoService getVideoInfoService() {
        return VideoInfoService.instance();
    }

    @NonNull
    public static AppService getAppService() {
        return AppService.instance();
    }

    public static PlaylistService getPlaylistService() {
        return new PlaylistService();
    }

}
