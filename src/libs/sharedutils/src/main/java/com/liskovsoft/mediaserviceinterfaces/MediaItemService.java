package com.liskovsoft.sharedutils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liskovsoft.sharedutils.data.DislikeData;
import com.liskovsoft.sharedutils.data.MediaItem;
import com.liskovsoft.sharedutils.data.MediaItemFormatInfo;
import com.liskovsoft.sharedutils.data.MediaItemMetadata;
import com.liskovsoft.sharedutils.data.MediaItemStoryboard;
import com.liskovsoft.sharedutils.data.PlaylistInfo;
import com.liskovsoft.sharedutils.data.SponsorSegment;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.sharedutils.actions.ActionsService;
import com.liskovsoft.sharedutils.actions.ActionsServiceWrapper;
import com.liskovsoft.sharedutils.block.SponsorBlockService;
import com.liskovsoft.sharedutils.block.data.SegmentList;
import com.liskovsoft.sharedutils.common.models.impl.mediaitem.BaseMediaItem;
import com.liskovsoft.sharedutils.feedback.FeedbackService;
import com.liskovsoft.sharedutils.next.v2.WatchNextService;
import com.liskovsoft.sharedutils.next.v2.WatchNextServiceWrapper;
import com.liskovsoft.sharedutils.playlist.PlaylistService;
import com.liskovsoft.sharedutils.playlist.PlaylistServiceWrapper;
import com.liskovsoft.sharedutils.playlistgroups.PlaylistGroupServiceImpl;
import com.liskovsoft.sharedutils.service.data.YouTubeMediaItem;
import com.liskovsoft.sharedutils.service.data.YouTubeSponsorSegment;
import com.liskovsoft.sharedutils.videoinfo.V2.VideoInfoService;
import com.liskovsoft.sharedutils.videoinfo.models.VideoInfo;
import com.liskovsoft.sharedutils.SignInService;

import io.reactivex.Observable;

import java.util.List;
import java.util.Set;

public class MediaItemService {
    
    public final static int PLAYLIST_ORDER_ADDED_DATE_NEWER_FIRST = 1;
    public final static int PLAYLIST_ORDER_ADDED_DATE_OLDER_FIRST = 2;
    public final static int PLAYLIST_ORDER_POPULARITY = 3;
    public final static int PLAYLIST_ORDER_PUBLISHED_DATE_NEWER_FIRST = 4;
    public final static int PLAYLIST_ORDER_PUBLISHED_DATE_OLDER_FIRST = 5;

    private static MediaItemService sInstance;
    
    private MediaItemFormatInfo mCachedFormatInfo;

    private MediaItemService() {}

    public static MediaItemService instance() {

        if (sInstance == null) {
            sInstance = new MediaItemService();
        }

        return sInstance;
    }

    private MediaItemFormatInfo getFormatInfo(String videoId) {

        MediaItemFormatInfo cachedFormatInfo = getCachedFormatInfo(videoId);

        if (cachedFormatInfo != null)
            return cachedFormatInfo;

        getSignInService().checkAuth();

        VideoInfo videoInfo = getVideoInfoService().getVideoInfo(videoId, null);

        MediaItemFormatInfo formatInfo = MediaItemFormatInfo.from(videoInfo);

        setCachedFormatInfo(formatInfo, null);

        return formatInfo;
    }

    public Observable<MediaItemFormatInfo> getFormatInfoObserve(String videoId) {
        return RxHelper.fromCallable(() -> getFormatInfo(videoId));
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

    public Observable<Void> subscribeObserve(MediaItem item) {
        return RxHelper.fromRunnable(() -> subscribe(item));
    }

    public Observable<Void> subscribeObserve(String channelId) {
        return RxHelper.fromRunnable(() -> subscribe(channelId));
    }

    public Observable<Void> unsubscribeObserve(MediaItem item) {
        return RxHelper.fromRunnable(() -> unsubscribe(item));
    }

    public Observable<Void> unsubscribeObserve(String channelId) {
        return RxHelper.fromRunnable(() -> unsubscribe(channelId));
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

    public void setLike(MediaItem item) {
        getSignInService().checkAuth();

        getActionsService().setLike(item.getVideoId());
    }

    public void removeLike(MediaItem item) {
        getSignInService().checkAuth();

        getActionsService().removeLike(item.getVideoId());
    }

    public void setDislike(MediaItem item) {
        getSignInService().checkAuth();

        getActionsService().setDislike(item.getVideoId());
    }

    public void removeDislike(MediaItem item) {
        getSignInService().checkAuth();

        getActionsService().removeDislike(item.getVideoId());
    }

    public void subscribe(MediaItem item) {
        subscribe(item.getChannelId(), item.getParams());
    }

    public void subscribe(String channelId) {
        subscribe(channelId, null);
    }

    private void subscribe(String channelId, String params) {
        getSignInService().checkAuth();

        getActionsService().subscribe(channelId, params);
    }

    public void unsubscribe(MediaItem item) {
        unsubscribe(item.getChannelId());
    }

    public void unsubscribe(String channelId) {
        getSignInService().checkAuth();

        getActionsService().unsubscribe(channelId);
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

    public List<SponsorSegment> getSponsorSegments(String videoId) {
        SegmentList segmentList = getSponsorBlockService().getSegmentList(videoId);

        return YouTubeSponsorSegment.from(segmentList);
    }

    public List<SponsorSegment> getSponsorSegments(String videoId, Set<String> categories) {
        SegmentList segmentList = getSponsorBlockService().getSegmentList(videoId, categories);

        return YouTubeSponsorSegment.from(segmentList);
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

    public Observable<List<SponsorSegment>> getSponsorSegmentsObserve(String videoId) {
        return RxHelper.fromCallable(() -> getSponsorSegments(videoId));
    }

    public Observable<List<SponsorSegment>> getSponsorSegmentsObserve(String videoId, Set<String> categories) {
        return RxHelper.fromCallable(() -> getSponsorSegments(videoId, categories));
    }

    public Observable<DislikeData> getDislikeDataObserve(String videoId) {
        return RxHelper.fromCallable(() -> getWatchNextService().getDislikeData(videoId));
    }

    public Observable<String> getUnlocalizedTitleObserve(String videoId) {
        return RxHelper.fromCallable(() -> getWatchNextService().getUnlocalizedTitle(videoId));
    }

    public void invalidateCache() {
        mCachedFormatInfo = null;
    }

    private MediaItemFormatInfo getCachedFormatInfo(String videoId) {
        return  mCachedFormatInfo != null &&
                mCachedFormatInfo.getVideoId() != null &&
                mCachedFormatInfo.getVideoId().equals(videoId) &&
                mCachedFormatInfo.isCacheActual() ? mCachedFormatInfo : null;
    }

    private void setCachedFormatInfo(MediaItemFormatInfo formatInfo, String clickTrackingParams) {
        mCachedFormatInfo = formatInfo;

        if (formatInfo != null) {
            formatInfo.setClickTrackingParams(clickTrackingParams);
        }
    }

    @NonNull
    private static SignInService getSignInService() {
        return SignInService.instance();
    }

    @NonNull
    private static SponsorBlockService getSponsorBlockService() {
        return SponsorBlockService.instance();
    }

    @NonNull
    private static VideoInfoService getVideoInfoService() {
        return VideoInfoService.instance();
    }

    @NonNull
    private static ActionsService getActionsService() {
        return ActionsServiceWrapper.instance();
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
    private static WatchNextService getWatchNextService() {
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
