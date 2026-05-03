package SmartTubeApp.misc;

import android.content.Context;
import android.util.Pair;

import com.liskovsoft.sharedutils.videoinfo.models.VideoInfo;
import com.liskovsoft.googleapi.oauth2.manager.OAuth2AccountManager;
import com.liskovsoft.sharedutils.videoinfo.V2.VideoInfoService;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.service.data.YouTubeMediaItemFormatInfo;
import com.liskovsoft.sharedutils.service.YouTubeMediaItemService;
import com.liskovsoft.sharedutils.service.ContentService;
import com.liskovsoft.sharedutils.MediaItemService;
import com.liskovsoft.sharedutils.ServiceManager;
import com.liskovsoft.sharedutils.NotificationsService;
import com.liskovsoft.sharedutils.SignInService;
import com.liskovsoft.sharedutils.SignInService.OnAccountChange;
import com.liskovsoft.sharedutils.oauth.Account;
import com.liskovsoft.sharedutils.data.MediaGroup;
import com.liskovsoft.sharedutils.data.MediaItem;
import com.liskovsoft.sharedutils.data.MediaItemFormatInfo;
import com.liskovsoft.sharedutils.data.MediaItemMetadata;
import com.liskovsoft.sharedutils.data.NotificationState;
import com.liskovsoft.sharedutils.data.PlaylistInfo;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import SmartTubeApp.app.models.data.Queue;
import SmartTubeApp.app.models.data.Video;
import SmartTubeApp.app.models.data.VideoGroup;
import SmartTubeApp.app.models.playback.service.VideoStateService;
import SmartTubeApp.app.models.playback.service.VideoStateService.State;
import SmartTubeApp.app.presenters.ChannelPresenter;
import SmartTubeApp.app.presenters.ChannelUploadsPresenter;
import SmartTubeApp.prefs.AccountsData;
import SmartTubeApp.prefs.AppPrefs;
import SmartTubeApp.prefs.MainUIData;
import SmartTubeApp.utils.LoadingManager;
import SmartTubeApp.utils.Utils;
import com.liskovsoft.sharedutils.service.YouTubeServiceManager;
import com.liskovsoft.sharedutils.playlist.PlaylistService;
import com.liskovsoft.sharedutils.okhttp.ApiCaller;
import com.liskovsoft.sharedutils.app.AppService;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class MediaServiceManager implements OnAccountChange {

    private static final String TAG = MediaServiceManager.class.getSimpleName();
    
    private static MediaServiceManager sInstance;
    
    private final MediaItemService mItemService;
    private final ContentService mContentService;
    private final SignInService mSignInService;
    private final NotificationsService mNotificationsService;
    private final PlaylistService mPlaylistService;
    private final YouTubeMediaItemService mYTMediaItemService;
    private final VideoInfoService mVideoInfoService;
    private final OAuth2AccountManager mAccountManager;
    private final AppService mAppService;

    private Disposable mMetadataAction;
    private Disposable mUploadsAction;
    private Disposable mRowsAction;
    private Disposable mSubscribedChannelsAction;
    private Disposable mFormatInfoAction;
    private Disposable mPlaylistGroupAction;
    private Disposable mPlaylistInfosAction;
    
    private static final int MIN_GRID_GROUP_SIZE = 13;
    private static final int MIN_ROW_GROUP_SIZE = 5;

    private final Map<Integer, Pair<Integer, Long>> mContinuations = new HashMap<>();
    private final List<AccountChangeListener> mAccountListeners = new CopyOnWriteArrayList<>();

    private float mPositionSec;
    private String mVideoId;

    public interface OnMetadata {
        void onMetadata(MediaItemMetadata metadata);
    }

    public interface OnMediaGroup {
        void onMediaGroup(MediaGroup group);
    }

    public interface OnMediaGroupList {
        void onMediaGroupList(List<MediaGroup> groupList);
    }

    public interface OnFormatInfo {
        void onFormatInfo(MediaItemFormatInfo formatInfo);
    }

    public interface OnAccountList {
        void onAccountList(List<Account> accountList);
    }

    public interface OnPlaylistInfos {
        void onPlaylistInfos(List<PlaylistInfo> playlistInfos);
    }

    public interface AccountChangeListener {
        void onAccountChanged(Account account);
    }

    public interface OnError {
        void onError(Throwable error);
    }

    private MediaServiceManager() {

        ServiceManager service = YouTubeServiceManager.instance();
        
        mItemService = service.getMediaItemService();
        mContentService = service.getContentService();
        mSignInService = service.getSignInService();
        mNotificationsService = service.getNotificationsService();
        mPlaylistService = new PlaylistService();
        mYTMediaItemService = YouTubeMediaItemService.instance();
        mAccountManager = OAuth2AccountManager.instance();
        mVideoInfoService = VideoInfoService.instance();
        mAppService = AppService.instance();

        mSignInService.addOnAccountChange(this);

    }

    public static MediaServiceManager instance() {
        if (sInstance == null) {
            sInstance = new MediaServiceManager();
        }

        return sInstance;
    }

    public void loadMetadata(Video video, OnMetadata onMetadata) {
        if (video == null) {
            return;
        }

        RxHelper.disposeActions(mMetadataAction);

        Observable<MediaItemMetadata> observable;

        // NOTE: Load suggestions from mediaItem isn't robust. Because playlistId may be initialized from RemoteControlManager.
        // Video might be loaded from Channels section (has playlistParams)
        if (video.mediaItem != null) {
            // Use additional data like playlist id
            observable = mItemService.getMetadataObserve(video.mediaItem);
        } else {
            // Simply load
            observable = mItemService.getMetadataObserve(video.videoId, video.getPlaylistId(), video.playlistIndex, video.playlistParams);
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
    public void loadMetadata(MediaItem mediaItem, OnMetadata onMetadata) {
        if (mediaItem == null) {
            return;
        }

        RxHelper.disposeActions(mMetadataAction);

        Observable<MediaItemMetadata> observable;

        observable = mItemService.getMetadataObserve(mediaItem);

        mMetadataAction = observable
                .subscribe(
                        onMetadata::onMetadata,
                        error -> Log.e(TAG, "loadMetadata error: %s", error.getMessage())
                );
    }

    public void loadChannelUploads(Video item, OnMediaGroup onMediaGroup) {
        if (item == null) {
            return;
        }

        loadChannelUploads(item.mediaItem, onMediaGroup);
    }

    public void loadChannelUploads(MediaItem item, OnMediaGroup onMediaGroup) {
        if (item == null) {
            return;
        }

        RxHelper.disposeActions(mUploadsAction);

        Observable<MediaGroup> observable = mContentService.getGroupObserve(item);

        mUploadsAction = observable
                .subscribe(
                        onMediaGroup::onMediaGroup,
                        error -> {
                            onMediaGroup.onMediaGroup(null);
                            Log.e(TAG, "loadChannelUploads error: %s", error.getMessage());
                        }
                );
    }

    public void loadSubscribedChannels(OnMediaGroup onMediaGroup) {
        RxHelper.disposeActions(mSubscribedChannelsAction);

        Observable<MediaGroup> observable = mContentService.getSubscribedChannelsByNewContentObserve();

        mSubscribedChannelsAction = observable
                .subscribe(
                        onMediaGroup::onMediaGroup,
                        error -> Log.e(TAG, "loadSubscribedChannels error: %s", error.getMessage())
                );
    }

    public void loadChannelRows(Video item, OnMediaGroupList onMediaGroupList) {
        loadChannelRows(item, onMediaGroupList, null);
    }

    public void loadChannelRows(Video item, OnMediaGroupList onMediaGroupList, OnError onError) {
        if (item == null) {
            return;
        }

        RxHelper.disposeActions(mRowsAction);

        Observable<List<MediaGroup>> observable = item.mediaItem != null ?
                mContentService.getChannelObserve(item.mediaItem) : mContentService.getChannelObserve(item.channelId);

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

    public void loadChannelPlaylist(Video item, OnMediaGroup callback) {
        loadChannelRows(
                item,
                mediaGroupList -> callback.onMediaGroup(mediaGroupList.get(0))
        );
    }

    public void loadFormatInfo(Video item, OnFormatInfo onFormatInfo) {
        if (item == null) {
            return;
        }

        RxHelper.disposeActions(mFormatInfoAction);

        Observable<MediaItemFormatInfo> observable = mItemService.getFormatInfoObserve(item.videoId);

        mFormatInfoAction = observable
                .subscribe(
                        onFormatInfo::onFormatInfo,
                        error -> Log.e(TAG, "loadFormatInfo error: %s", error.getMessage())
                );
    }

    public void loadPlaylists(Video item, OnMediaGroup onPlaylistGroup) {
        if (item == null) {
            return;
        }

        RxHelper.disposeActions(mPlaylistGroupAction);

        Observable<MediaGroup> observable = mContentService.getPlaylistsObserve();

        mPlaylistGroupAction = observable
                .subscribe(
                        onPlaylistGroup::onMediaGroup,
                        error -> Log.e(TAG, "loadPlaylists error: %s", error.getMessage())
                );
    }

    public void getPlaylistInfos(OnPlaylistInfos onPlaylistInfos) {
        RxHelper.disposeActions(mPlaylistInfosAction);

        Observable<List<PlaylistInfo>> observable = mItemService.getPlaylistsInfoObserve(null);

        mPlaylistInfosAction = observable
                .subscribe(
                        onPlaylistInfos::onPlaylistInfos,
                        error -> Log.e(TAG, "getPlaylistInfos error: %s", error.getMessage())
                );
    }

    public void loadAccounts(OnAccountList onAccountList) {
        onAccountList.onAccountList(mSignInService.getAccounts());
    }

    public void authCheck(Runnable onSuccess, Runnable onError) {
        if (onSuccess == null && onError == null) {
            return;
        }

        if (mSignInService.isSigned()) {
            if (onSuccess != null) {
                onSuccess.run();
            }
        } else {
            if (onError != null) {
                onError.run();
            }
        }
    }

    public void disposeActions() {
        RxHelper.disposeActions(mMetadataAction, mUploadsAction, mRowsAction, mSubscribedChannelsAction);
    }

    /**
     * Most tiny ui has 8 cards in a row or 24 in grid.
     */
    public void shouldContinueGridGroup(Context context, VideoGroup group, Runnable onNeedContinue) {
        shouldContinueTheGroup(context, group, onNeedContinue, true);
    }

    public void shouldContinueRowGroup(Context context, VideoGroup group, Runnable onNeedContinue) {
        shouldContinueTheGroup(context, group, onNeedContinue, false);
    }

    /**
     * Most tiny ui has 8 cards in a row or 24 in grid.
     */
    public void shouldContinueTheGroup(Context context, VideoGroup group, Runnable onNeedContinue, boolean isGrid) {
        if (shouldContinueTheGroup(context, group, isGrid) && onNeedContinue != null) {
            onNeedContinue.run();
        }
    }

    /**
     * Most tiny ui has 8 cards in a row or 24 in grid.
     */
    public boolean shouldContinueGridGroup(Context context, VideoGroup group) {
        return shouldContinueTheGroup(context, group, true);
    }

    public boolean shouldContinueRowGroup(Context context, VideoGroup group) {
        return shouldContinueTheGroup(context, group,false);
    }

    /**
     * Most tiny ui has 8 cards in a row or 24 in grid.
     */
    public boolean shouldContinueTheGroup(Context context, VideoGroup group, boolean isGrid) {

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

    public void clearSearchHistory() {
        RxHelper.runAsyncUser(mContentService::clearSearchHistory);
    }

    public void updateHistory(Video video, long positionMs) {

        Queue.sync(video);

        VideoStateService stateService = VideoStateService.instance(null);

        if (stateService != null) {
            stateService.save(new State(
                video, 
                positionMs, 
                video.getDurationMs()
            ));
        } else {
            Log.e(TAG, "State Service not Instantiated");
        }

        mAccountManager.checkAuth();

        YouTubeMediaItemFormatInfo formatInfo = mYTMediaItemService.getFormatInfo(video.videoId);

        if (formatInfo == null) {
            Log.e(TAG, "Can't update history for video id %s. formatInfo == null", video.videoId);
            return;
        }

        if (!formatInfo.isAuth() && !formatInfo.isUnplayable() && mSignInService.isSigned()) {
            
            VideoInfo videoInfo = mVideoInfoService.getAuthVideoInfo(
                formatInfo.getVideoId(), 
                formatInfo.getClickTrackingParams()
            );

            formatInfo.sync(YouTubeMediaItemFormatInfo.from(videoInfo));
        
        }

        String videoId = formatInfo.getVideoId();
        float oldPositionSec = mPositionSec;
        float positionSec = positionMs / 1_000f;

        ApiCaller apiTempl = new ApiCaller("https://www.youtube.com/api/stats/playback?ns=yt&ver=2");
        apiTempl.add("docid", videoId);
        apiTempl.add("len", Helpers.parseFloat(formatInfo.getLengthSeconds()));
        apiTempl.add("cpn", mAppService.getClientPlaybackNonce());            
        apiTempl.add("ei", formatInfo.getEventId());
        apiTempl.add("vm", formatInfo.getVisitorMonitoringData());
        apiTempl.add("of", formatInfo.getOfParam());

        ApiCaller api;

        if (mVideoId == null || mVideoId != videoId) {

            mVideoId = videoId;
            mPositionSec = 0;

            api = apiTempl.copy();
            api.add("cmt", oldPositionSec);
            api.call();
        
        }

        api = apiTempl.copy();
        api.add("st", oldPositionSec);
        api.add("et", positionSec);
        api.add("cmt", positionSec);
        api.call();
        
        mPositionSec = positionSec;

    }

    public void hideNotification(Video item) {
        if (item != null && item.belongsToNotifications()) {
            RxHelper.execute(mNotificationsService.hideNotificationObserve(item.mediaItem));
        }
    }

    public void setNotificationState(NotificationState state, OnError onError) {
        RxHelper.execute(mNotificationsService.setNotificationStateObserve(state), onError::onError);
    }

    public void removeFromWatchLaterPlaylist(Video video) {
        removeFromWatchLaterPlaylist(video, null);
    }

    public void removeFromWatchLaterPlaylist(Video video, Runnable onSuccess) {
        if (video == null || !mSignInService.isSigned()) {
            return;
        }

        Disposable playlistsInfoAction = mItemService.getPlaylistsInfoObserve(video.videoId)
                .subscribe(
                        videoPlaylistInfos -> {
                            PlaylistInfo watchLater = videoPlaylistInfos.get(0);

                            if (watchLater.isSelected()) {
                                Observable<Void> editObserve = mItemService.removeFromPlaylistObserve(watchLater.getPlaylistId(), video.videoId);

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

    public void addAccountListener(AccountChangeListener listener) {
        if (!mAccountListeners.contains(listener)) {
            if (listener instanceof AccountsData ||
                listener instanceof AppPrefs) {
                mAccountListeners.add(0, listener); // data classes should be called before regular listeners
            } else {
                mAccountListeners.add(listener);
            }
        }
    }

    public void removeAccountListener(AccountChangeListener listener) {
        mAccountListeners.remove(listener);
    }

    public Account getSelectedAccount() {
        return mSignInService.getSelectedAccount();
    }

    public String printAccountDebugInfo() {
        return mSignInService.printDebugInfo();
    }

    @Override
    public void onAccountChanged(Account account) {
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

        MediaServiceManager.instance().loadChannelRows(item, groups -> {
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

}