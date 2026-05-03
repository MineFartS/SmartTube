package com.liskovsoft.sharedutils.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liskovsoft.sharedutils.MediaItemService;
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
import com.liskovsoft.sharedutils.service.data.YouTubeMediaItemFormatInfo;
import com.liskovsoft.sharedutils.service.data.YouTubeSponsorSegment;
import com.liskovsoft.sharedutils.videoinfo.V2.VideoInfoService;
import com.liskovsoft.sharedutils.videoinfo.models.VideoInfo;
import com.liskovsoft.sharedutils.okhttp.ApiCaller;

import io.reactivex.Observable;

import java.util.List;
import java.util.Set;

public class YouTubeMediaItemService implements MediaItemService {
    
    private static YouTubeMediaItemService sInstance;
    
    private YouTubeMediaItemFormatInfo mCachedFormatInfo;

    private YouTubeMediaItemService() {}

    public static YouTubeMediaItemService instance() {

        if (sInstance == null) {
            sInstance = new YouTubeMediaItemService();
        }

        return sInstance;
    }

    /**
     * Format info is cached because it's supposed to run in multiple methods
     */
    @Override
    public YouTubeMediaItemFormatInfo getFormatInfo(MediaItem item) {
        return getFormatInfo(
            item.getVideoId(), 
            item.getClickTrackingParams()
        );
    }

    @Override
    public YouTubeMediaItemFormatInfo getFormatInfo(String videoId) {
        return getFormatInfo(videoId, null);
    }

    @Override
    public YouTubeMediaItemFormatInfo getFormatInfo(String videoId, String clickTrackingParams) {

        YouTubeMediaItemFormatInfo cachedFormatInfo = getCachedFormatInfo(videoId);

        if (cachedFormatInfo != null) {
            return cachedFormatInfo;
        }

        ApiCaller.setBypassEnabled(true);

        getSignInService().checkAuth();

        ApiCaller.setBypassEnabled(false);

        VideoInfo videoInfo = getVideoInfoService().getVideoInfo(
            videoId, 
            clickTrackingParams
        );

        YouTubeMediaItemFormatInfo formatInfo = YouTubeMediaItemFormatInfo.from(videoInfo);

        setCachedFormatInfo(
            formatInfo, 
            clickTrackingParams
        );

        return formatInfo;
    }

    @Override
    public Observable<MediaItemFormatInfo> getFormatInfoObserve(MediaItem item) {
        return RxHelper.fromCallable(() -> getFormatInfo(item));
    }

    @Override
    public Observable<MediaItemFormatInfo> getFormatInfoObserve(String videoId) {
        return RxHelper.fromCallable(() -> getFormatInfo(videoId));
    }

    @Override
    public Observable<MediaItemFormatInfo> getFormatInfoObserve(String videoId, String clickTrackingParams) {
        return RxHelper.fromCallable(() -> getFormatInfo(videoId, clickTrackingParams));
    }

    @Override
    public MediaItemStoryboard getStoryboard(MediaItem item) {
        return getStoryboard(item.getVideoId());
    }

    @Override
    public MediaItemStoryboard getStoryboard(String videoId) {
        YouTubeMediaItemFormatInfo format = getFormatInfo(videoId);
        return format != null ? format.createStoryboard() : null;
    }

    @Override
    public Observable<MediaItemStoryboard> getStoryboardObserve(MediaItem item) {
        return RxHelper.fromCallable(() -> getStoryboard(item));
    }

    @Override
    public Observable<MediaItemStoryboard> getStoryboardObserve(String videoId) {
        return RxHelper.fromCallable(() -> getStoryboard(videoId));
    }

    @Override
    public MediaItemMetadata getMetadata(MediaItem item) {
        return getMetadata(
            item.getVideoId(), 
            item.getPlaylistId(), 
            item.getPlaylistIndex(), 
            item.getParams()
        );
    }

    @Override
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

    @Override
    public MediaItemMetadata getMetadata(String videoId) {
        return getWatchNextService().getMetadata(videoId);
    }

    @Override
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

    @Override
    public Observable<MediaItemMetadata> getMetadataObserve(String videoId) {
        return RxHelper.fromCallable(() -> getMetadata(videoId));
    }

    @Override
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

    @Override
    public Observable<Void> subscribeObserve(MediaItem item) {
        return RxHelper.fromRunnable(() -> subscribe(item));
    }

    @Override
    public Observable<Void> subscribeObserve(String channelId) {
        return RxHelper.fromRunnable(() -> subscribe(channelId));
    }

    @Override
    public Observable<Void> unsubscribeObserve(MediaItem item) {
        return RxHelper.fromRunnable(() -> unsubscribe(item));
    }

    @Override
    public Observable<Void> unsubscribeObserve(String channelId) {
        return RxHelper.fromRunnable(() -> unsubscribe(channelId));
    }

    @Override
    public Observable<Void> setLikeObserve(MediaItem item) {
        return RxHelper.fromRunnable(() -> setLike(item));
    }

    @Override
    public Observable<Void> removeLikeObserve(MediaItem item) {
        return RxHelper.fromRunnable(() -> removeLike(item));
    }

    @Override
    public Observable<Void> setDislikeObserve(MediaItem item) {
        return RxHelper.fromRunnable(() -> setDislike(item));
    }

    @Override
    public Observable<Void> removeDislikeObserve(MediaItem item) {
        return RxHelper.fromRunnable(() -> removeDislike(item));
    }

    @Override
    public void setLike(MediaItem item) {
        getSignInService().checkAuth();

        getActionsService().setLike(item.getVideoId());
    }

    @Override
    public void removeLike(MediaItem item) {
        getSignInService().checkAuth();

        getActionsService().removeLike(item.getVideoId());
    }

    @Override
    public void setDislike(MediaItem item) {
        getSignInService().checkAuth();

        getActionsService().setDislike(item.getVideoId());
    }

    @Override
    public void removeDislike(MediaItem item) {
        getSignInService().checkAuth();

        getActionsService().removeDislike(item.getVideoId());
    }

    @Override
    public void subscribe(MediaItem item) {
        subscribe(item.getChannelId(), item.getParams());
    }

    @Override
    public void subscribe(String channelId) {
        subscribe(channelId, null);
    }

    private void subscribe(String channelId, String params) {
        getSignInService().checkAuth();

        getActionsService().subscribe(channelId, params);
    }

    @Override
    public void unsubscribe(MediaItem item) {
        unsubscribe(item.getChannelId());
    }

    @Override
    public void unsubscribe(String channelId) {
        getSignInService().checkAuth();

        getActionsService().unsubscribe(channelId);
    }

    @Override
    public void markAsNotInterested(String feedbackToken) {
        getSignInService().checkAuth();

        getFeedbackService().markAsNotInterested(feedbackToken);
    }

    @Override
    public Observable<Void> markAsNotInterestedObserve(String feedbackToken) {
        return RxHelper.fromRunnable(() -> markAsNotInterested(feedbackToken));
    }

    @Override
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

    @Override
    public void removeFromPlaylist(String playlistId, String videoId) {
        getSignInService().checkAuth();

        getPlaylistService().removeFromPlaylist(playlistId, videoId);
    }

    @Override
    public void renamePlaylist(String playlistId, String newName) {
        getSignInService().checkAuth();

        getPlaylistService().renamePlaylist(playlistId, newName);
    }

    @Override
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

    @Override
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

    @Override
    public List<SponsorSegment> getSponsorSegments(String videoId) {
        SegmentList segmentList = getSponsorBlockService().getSegmentList(videoId);

        return YouTubeSponsorSegment.from(segmentList);
    }

    @Override
    public List<SponsorSegment> getSponsorSegments(String videoId, Set<String> categories) {
        SegmentList segmentList = getSponsorBlockService().getSegmentList(videoId, categories);

        return YouTubeSponsorSegment.from(segmentList);
    }

    @Override
    public Observable<List<PlaylistInfo>> getPlaylistsInfoObserve(String videoId) {
        return RxHelper.fromCallable(() -> getPlaylistsInfo(videoId));
    }

    @Override
    public Observable<Void> addToPlaylistObserve(String playlistId, String videoId) {
        return RxHelper.fromRunnable(() -> addToPlaylist(playlistId, videoId));
    }

    @Override
    public Observable<Void> addToPlaylistObserve(String playlistId, MediaItem item) {
        return RxHelper.fromRunnable(() -> addToPlaylist(playlistId, item));
    }

    @Override
    public Observable<Void> removeFromPlaylistObserve(String playlistId, String videoId) {
        return RxHelper.fromRunnable(() -> removeFromPlaylist(playlistId, videoId));
    }

    @Override
    public Observable<Void> renamePlaylistObserve(String playlistId, String newName) {
        return RxHelper.fromRunnable(() -> renamePlaylist(playlistId, newName));
    }

    @Override
    public Observable<Void> setPlaylistOrderObserve(String playlistId, int playlistOrder) {
        return RxHelper.fromRunnable(() -> setPlaylistOrder(playlistId, playlistOrder));
    }

    @Override
    public Observable<Void> savePlaylistObserve(String playlistId) {
        return RxHelper.fromRunnable(() -> savePlaylist(playlistId));
    }

    @Override
    public Observable<Void> savePlaylistObserve(MediaItem item) {
        return RxHelper.fromRunnable(() -> savePlaylist(item));
    }

    @Override
    public Observable<Void> removePlaylistObserve(String playlistId) {
        return RxHelper.fromRunnable(() -> removePlaylist(playlistId));
    }

    @Override
    public Observable<Void> createPlaylistObserve(String playlistName, String videoId) {
        return RxHelper.fromRunnable(() -> createPlaylist(playlistName, videoId));
    }

    @Override
    public Observable<Void> createPlaylistObserve(String playlistName, MediaItem item) {
        return RxHelper.fromRunnable(() -> createPlaylist(playlistName, item));
    }

    @Override
    public Observable<List<SponsorSegment>> getSponsorSegmentsObserve(String videoId) {
        return RxHelper.fromCallable(() -> getSponsorSegments(videoId));
    }

    @Override
    public Observable<List<SponsorSegment>> getSponsorSegmentsObserve(String videoId, Set<String> categories) {
        return RxHelper.fromCallable(() -> getSponsorSegments(videoId, categories));
    }

    @Override
    public Observable<DislikeData> getDislikeDataObserve(String videoId) {
        return RxHelper.fromCallable(() -> getWatchNextService().getDislikeData(videoId));
    }

    @Override
    public Observable<String> getUnlocalizedTitleObserve(String videoId) {
        return RxHelper.fromCallable(() -> getWatchNextService().getUnlocalizedTitle(videoId));
    }

    public void invalidateCache() {
        mCachedFormatInfo = null;
    }

    private YouTubeMediaItemFormatInfo getCachedFormatInfo(String videoId) {
        return  mCachedFormatInfo != null &&
                mCachedFormatInfo.getVideoId() != null &&
                mCachedFormatInfo.getVideoId().equals(videoId) &&
                mCachedFormatInfo.isCacheActual() ? mCachedFormatInfo : null;
    }

    private void setCachedFormatInfo(YouTubeMediaItemFormatInfo formatInfo, String clickTrackingParams) {
        mCachedFormatInfo = formatInfo;

        if (formatInfo != null) {
            formatInfo.setClickTrackingParams(clickTrackingParams);
        }
    }

    @NonNull
    private static YouTubeSignInService getSignInService() {
        return YouTubeSignInService.instance();
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
