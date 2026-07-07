package minefarts.smarttube.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liskovsoft.mediaserviceinterfaces.data.MediaItem;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemStoryboard;
import com.liskovsoft.mediaserviceinterfaces.data.PlaylistInfo;
import com.liskovsoft.youtubeapi.common.models.impl.mediaitem.BaseMediaItem;
import com.liskovsoft.youtubeapi.next.v2.WatchNextService;
import com.liskovsoft.youtubeapi.next.v2.WatchNextServiceWrapper;
import com.liskovsoft.youtubeapi.playlistgroups.PlaylistGroupServiceImpl;
import com.liskovsoft.youtubeapi.service.data.YouTubeMediaItem;
import com.liskovsoft.youtubeapi.videoinfo.models.VideoInfo;
import com.liskovsoft.youtubeapi.browse.v1.BrowseService;
import com.liskovsoft.googlecommon.common.helpers.RetrofitHelper;
import com.liskovsoft.youtubeapi.channelgroups.ChannelGroupServiceImpl;
import com.liskovsoft.youtubeapi.notifications.NotificationStorage;
import com.liskovsoft.youtubeapi.common.helpers.PostDataHelper;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemFormatInfo;
import com.liskovsoft.mediaserviceinterfaces.data.SponsorSegment;
import com.liskovsoft.youtubeapi.videoinfo.V2.VideoInfoService;
import com.liskovsoft.youtubeapi.service.YouTubeSignInService;

import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.rx.RxHelper;
import minefarts.smarttube.utils.feedback.FeedbackService;
import minefarts.smarttube.utils.playlist.PlaylistService;
import minefarts.smarttube.utils.playlist.PlaylistServiceWrapper;
import minefarts.smarttube.utils.actions.models.ActionResult;
import minefarts.smarttube.utils.actions.ActionsApi;
import minefarts.smarttube.CacheManager;

import io.reactivex.Observable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import retrofit2.Call;

public class MediaItemService {

    private static final String TAG = MediaItemService.class.getSimpleName();
    
    public final static int PLAYLIST_ORDER_ADDED_DATE_NEWER_FIRST = 1;
    public final static int PLAYLIST_ORDER_ADDED_DATE_OLDER_FIRST = 2;
    public final static int PLAYLIST_ORDER_POPULARITY = 3;
    public final static int PLAYLIST_ORDER_PUBLISHED_DATE_NEWER_FIRST = 4;
    public final static int PLAYLIST_ORDER_PUBLISHED_DATE_OLDER_FIRST = 5;

    private static MediaItemService sInstance;
    
    public MediaItemFormatInfo mCachedFormatInfo;
    
    ActionsApi mActionsApi;

    public static MediaItemService instance() {
        if (sInstance == null) {
            sInstance = new MediaItemService();
            sInstance.mActionsApi = RetrofitHelper.create(ActionsApi.class);
        }

        return sInstance;
    }

    public MediaItemFormatInfo getFormatInfo(String videoId) {
        try {
            VideoInfo videoInfo = getVideoInfoService().getVideoInfo(videoId, null);
            return (MediaItemFormatInfo) videoInfo;
        } catch (Exception e) {
            // Handle V8 runtime errors from youtubeapi
            String errorMsg = e.getMessage();
            Throwable cause = e.getCause();
            
            if (errorMsg != null && (errorMsg.contains("jsc is not a function") || 
                                          errorMsg.contains("V8 runtime error"))) {
                Log.e(TAG, "V8 signature challenge error - rethrowing for retry", e);
                throw new RuntimeException("V8SignatureChallengeError", e);
            }
            
            if (cause != null && cause.getMessage() != null) {
                String causeMsg = cause.getMessage();
                if (causeMsg.contains("jsc is not a function") || 
                    causeMsg.contains("V8 runtime error")) {
                    Log.e(TAG, "V8 signature challenge error (from cause) - rethrowing for retry", e);
                    throw new RuntimeException("V8SignatureChallengeError", e);
                }
            }
            
            throw e;
        }
    }

    public Observable<MediaItemFormatInfo> getFormatInfoObserve(String videoId) {
        return RxHelper.fromCallable(() -> getFormatInfo(videoId))
                .doOnError(error -> {
                    if (error.getMessage() != null && 
                        error.getMessage().contains("V8SignatureChallengeError")) {
                        Log.e(TAG, "Format info retrieval failed with V8 error, clearing cache", error);
                        // Clear any cached player data that might be corrupted
                        CacheManager.clear();
                    }
                });
    }

    public MediaItemStoryboard getStoryboard(MediaItem item) {
        return getStoryboard(item.getVideoId());
    }

    public MediaItemStoryboard getStoryboard(String videoId) {
        MediaItemFormatInfo format = getFormatInfo(videoId);
        return format != null ? format.createStoryboard() : null;
    }

    public Observable<MediaItemStoryboard> getStoryboardObserve(MediaItem item) {
        return RxHelper.fromCallable(() -> getStoryboard(item));
    }

    public Observable<MediaItemStoryboard> getStoryboardObserve(String videoId) {
        return RxHelper.fromCallable(() -> getStoryboard(videoId));
    }

    public MediaItemMetadata getMetadata(MediaItem item) {
        return getMetadata(
            item.getVideoId(), 
            item.getPlaylistId(), 
            item.getPlaylistIndex(), 
            item.getParams()
        );
    }

    public MediaItemMetadata getMetadata(
        String videoId, 
        String playlistId, 
        int playlistIndex, 
        String playlistParams
    ) {
        return getWatchNextService().getMetadata(
            videoId, 
            playlistId, 
            playlistIndex, 
            playlistParams
        );
    }

    public MediaItemMetadata getMetadata(String videoId) {
        return getWatchNextService().getMetadata(videoId);
    }

    public Observable<MediaItemMetadata> getMetadataObserve(MediaItem item) {

        return RxHelper.create(emitter -> {
        
            MediaItemMetadata metadata = getMetadata(item);

            if (metadata != null) {
                syncItem(item, metadata);
                emitter.onNext(metadata);
                emitter.onComplete();
            } else {
                RxHelper.onError(emitter, "getMetadataObserve result is null");
            }
        
        });

    }

    public Observable<MediaItemMetadata> getMetadataObserve(String videoId) {
        return RxHelper.fromCallable(() -> getMetadata(videoId));
    }

    public Observable<MediaItemMetadata> getMetadataObserve(
        String videoId, 
        String playlistId, 
        int playlistIndex, 
        String playlistParams
    ) {
        return RxHelper.fromCallable(() -> getMetadata(
            videoId, 
            playlistId, 
            playlistIndex, 
            playlistParams
        ));
    }

    public Observable<Void> setLikeObserve(MediaItem item) {
        return RxHelper.fromRunnable(() -> setLike(item));
    }

    public Observable<Void> removeLikeObserve(MediaItem item) {
        return RxHelper.fromRunnable(() -> removeLike(item));
    }

    public Observable<Void> setDislikeObserve(MediaItem item) {
        return RxHelper.fromRunnable(() -> setDislike(item));
    }

    public Observable<Void> removeDislikeObserve(MediaItem item) {
        return RxHelper.fromRunnable(() -> removeDislike(item));
    }
    
    public static String getLikeActionQuery(String videoId) {
        String likeTemplate = String.format("\"target\":{\"videoId\":\"%s\"}", videoId);
        return PostDataHelper.createQueryTV(likeTemplate);
    }

    public void setLike(MediaItem item) {
        getSignInService().checkAuth();

        Call<ActionResult> wrapper = mActionsApi.setLike(
            getLikeActionQuery(item.getVideoId())
        );

        RetrofitHelper.get(wrapper); // ignore result

        NotificationStorage.setLike(true);
    }

    public void removeLike(MediaItem item) {
        getSignInService().checkAuth();

        Call<ActionResult> wrapper = mActionsApi.removeLike(
            getLikeActionQuery(item.getVideoId())
        );

        RetrofitHelper.get(wrapper); // ignore result

        NotificationStorage.setLike(false);
    }

    public void setDislike(MediaItem item) {
        getSignInService().checkAuth();

        Call<ActionResult> wrapper = mActionsApi.setDislike(
            getLikeActionQuery(item.getVideoId())
        );

        RetrofitHelper.get(wrapper); // ignore result

        NotificationStorage.setLike(false);
    }

    public void removeDislike(MediaItem item) {
        getSignInService().checkAuth();

        Call<ActionResult> wrapper = mActionsApi.removeDislike(
            getLikeActionQuery(item.getVideoId())
        );

        RetrofitHelper.get(wrapper); // ignore result

        NotificationStorage.setLike(true);
    }

    public void markAsNotInterested(String feedbackToken) {
        getSignInService().checkAuth();

        getFeedbackService().markAsNotInterested(feedbackToken);
    }

    public Observable<Void> markAsNotInterestedObserve(String feedbackToken) {
        return RxHelper.fromRunnable(() -> markAsNotInterested(feedbackToken));
    }

    public List<PlaylistInfo> getPlaylistsInfo(String videoId) {
        getSignInService().checkAuth();

        return getPlaylistService().getPlaylistsInfo(videoId);
    }

    private void addToPlaylist(String playlistId, String videoId) {
        getSignInService().checkAuth();

        getPlaylistService().addToPlaylist(playlistId, videoId);
    }

    private void addToPlaylist(String playlistId, MediaItem item) {
        getSignInService().checkAuth();

        PlaylistGroupServiceImpl.cachedItem = item;
        getPlaylistService().addToPlaylist(playlistId, item.getVideoId());
    }

    public void removeFromPlaylist(String playlistId, String videoId) {
        getSignInService().checkAuth();

        getPlaylistService().removeFromPlaylist(playlistId, videoId);
    }

    public void renamePlaylist(String playlistId, String newName) {
        getSignInService().checkAuth();

        getPlaylistService().renamePlaylist(playlistId, newName);
    }

    public void setPlaylistOrder(String playlistId, int playlistOrder) {
        getSignInService().checkAuth();

        getPlaylistService().setPlaylistOrder(playlistId, playlistOrder);
    }

    private void savePlaylist(String playlistId) {
        getSignInService().checkAuth();

        getPlaylistService().savePlaylist(playlistId);
    }

    private void savePlaylist(MediaItem item) {
        getSignInService().checkAuth();

        PlaylistGroupServiceImpl.cachedItem = item;
        getPlaylistService().savePlaylist(item.getPlaylistId());
    }

    public void removePlaylist(String playlistId) {
        getSignInService().checkAuth();

        getPlaylistService().removePlaylist(playlistId);
    }

    private void createPlaylist(String playlistName, String videoId) {
        getSignInService().checkAuth();

        getPlaylistService().createPlaylist(playlistName, videoId);
    }

    private void createPlaylist(String playlistName, @Nullable MediaItem item) {
        getSignInService().checkAuth();

        PlaylistGroupServiceImpl.cachedItem = item;
        getPlaylistService().createPlaylist(playlistName, item != null ? item.getVideoId() : null);
    }

    public Observable<List<PlaylistInfo>> getPlaylistsInfoObserve(String videoId) {
        return RxHelper.fromCallable(() -> getPlaylistsInfo(videoId));
    }

    public Observable<Void> addToPlaylistObserve(String playlistId, String videoId) {
        return RxHelper.fromRunnable(() -> addToPlaylist(playlistId, videoId));
    }

    public Observable<Void> addToPlaylistObserve(String playlistId, MediaItem item) {
        return RxHelper.fromRunnable(() -> addToPlaylist(playlistId, item));
    }

    public Observable<Void> removeFromPlaylistObserve(String playlistId, String videoId) {
        return RxHelper.fromRunnable(() -> removeFromPlaylist(playlistId, videoId));
    }

    public Observable<Void> renamePlaylistObserve(String playlistId, String newName) {
        return RxHelper.fromRunnable(() -> renamePlaylist(playlistId, newName));
    }

    public Observable<Void> setPlaylistOrderObserve(String playlistId, int playlistOrder) {
        return RxHelper.fromRunnable(() -> setPlaylistOrder(playlistId, playlistOrder));
    }

    public Observable<Void> savePlaylistObserve(String playlistId) {
        return RxHelper.fromRunnable(() -> savePlaylist(playlistId));
    }

    public Observable<Void> savePlaylistObserve(MediaItem item) {
        return RxHelper.fromRunnable(() -> savePlaylist(item));
    }

    public Observable<Void> removePlaylistObserve(String playlistId) {
        return RxHelper.fromRunnable(() -> removePlaylist(playlistId));
    }

    public Observable<Void> createPlaylistObserve(String playlistName, String videoId) {
        return RxHelper.fromRunnable(() -> createPlaylist(playlistName, videoId));
    }

    public Observable<Void> createPlaylistObserve(String playlistName, MediaItem item) {
        return RxHelper.fromRunnable(() -> createPlaylist(playlistName, item));
    }

    public Observable<String> getUnlocalizedTitleObserve(String videoId) {
        return RxHelper.fromCallable(() -> getWatchNextService().getUnlocalizedTitle(videoId));
    }

    @NonNull
    private static YouTubeSignInService getSignInService() {
        return YouTubeSignInService.instance();
    }

    @NonNull
    private static VideoInfoService getVideoInfoService() {
        return VideoInfoService.instance();
    }

    @NonNull
    private static PlaylistService getPlaylistService() {
        return PlaylistServiceWrapper.instance();
    }

    @NonNull
    private static FeedbackService getFeedbackService() {
        return FeedbackService.instance();
    }

    @NonNull
    public static WatchNextService getWatchNextService() {
        return WatchNextServiceWrapper.INSTANCE;
    }

    private static void syncItem(MediaItem item, MediaItemMetadata metadata) {
        if (item instanceof BaseMediaItem) {
            ((BaseMediaItem) item).sync(metadata);
        } else if (item instanceof YouTubeMediaItem) {
            ((YouTubeMediaItem) item).sync(metadata);
        }
    }

}
