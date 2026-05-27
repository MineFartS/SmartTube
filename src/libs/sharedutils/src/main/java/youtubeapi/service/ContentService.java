package minefarts.sharedutils.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import minefarts.sharedutils.data.MediaItem;
import minefarts.sharedutils.helpers.Helpers;
import minefarts.sharedutils.mylogger.Log;
import minefarts.sharedutils.actions.ActionsService;
import minefarts.sharedutils.actions.ActionsServiceWrapper;
import minefarts.sharedutils.browse.v2.BrowseService2;
import minefarts.sharedutils.browse.v2.BrowseService2Wrapper;
import minefarts.sharedutils.common.models.impl.mediagroup.SuggestionsGroup;
import minefarts.sharedutils.next.v2.WatchNextService;
import minefarts.sharedutils.next.v2.WatchNextServiceWrapper;
import minefarts.sharedutils.rss.RssService;
import minefarts.sharedutils.search.SearchServiceWrapper;
import minefarts.sharedutils.service.internal.MediaServiceData;
import minefarts.sharedutils.utils.UtilsService;
import minefarts.sharedutils.browse.v1.BrowseService;
import minefarts.sharedutils.rx.RxHelper;
import minefarts.sharedutils.common.models.impl.mediagroup.BaseMediaGroup;
import minefarts.googlecommon.common.helpers.YouTubeHelper;
import minefarts.sharedutils.search.SearchService;
import minefarts.sharedutils.search.models.SearchResult;
import minefarts.sharedutils.service.data.MediaGroup;
import minefarts.sharedutils.SignInService;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContentService {
    private static final String TAG = ContentService.class.getSimpleName();
    private static ContentService sInstance;

    private ContentService() {
        Log.d(TAG, "Starting...");
    }

    public static ContentService instance() {
        if (sInstance == null) {
            sInstance = new ContentService();
        }

        return sInstance;
    }

    
    public List<MediaGroup> getSearch(String searchText) {
        checkSigned();

        SearchResult search = getSearchService().getSearch(searchText);
        return MediaGroup.from(search, MediaGroup.TYPE_SEARCH);
    }

    
    public List<MediaGroup> getSearch(String searchText, int options) {
        checkSigned();

        SearchResult search = getSearchService().getSearch(searchText, options);
        return MediaGroup.from(search, MediaGroup.TYPE_SEARCH);
    }

    
    public Observable<List<MediaGroup>> getSearchObserve(String searchText) {
        return RxHelper.fromCallable(() -> getSearch(searchText));
    }

    
    public Observable<List<MediaGroup>> getSearchObserve(String searchText, int options) {
        return RxHelper.fromCallable(() -> getSearch(searchText, options));
    }

    
    public List<String> getSearchTags(String searchText) {
        checkSigned();

        return getSearchService().getSearchTags(searchText);
    }

    
    public Observable<List<String>> getSearchTagsObserve(String searchText) {
        return RxHelper.fromCallable(() -> getSearchTags(searchText));
    }

    
    public MediaGroup getSubscriptions() {
        Log.d(TAG, "Getting subscriptions...");

        checkSigned();

        MediaGroup subscriptions = getBrowseService2().getSubscriptions();

        // TEMP fix. Subs not fully populated.
        if (subscriptions != null && subscriptions.getMediaItems() != null && subscriptions.getMediaItems().size() <= 5) {
            MediaGroup continuation = continueGroup(subscriptions);
            if (continuation == null || continuation.getMediaItems() == null || continuation.getMediaItems().isEmpty()) {
                if (getMediaServiceData() != null) {
                    return getBrowseService2().getSubscriptions();
                }
            }
        }

        return subscriptions;
    }

    
    public Observable<MediaGroup> getSubscriptionsObserve() {
        return RxHelper.fromCallable(this::getSubscriptions);
    }

    
    public MediaGroup getRssFeed(String... channelIds) {
        if (channelIds == null) {
            return null;
        }

        checkSigned();

        return RssService.getFeed(channelIds);
    }

    
    public Observable<MediaGroup> getRssFeedObserve(String... channelIds) {
        return RxHelper.fromCallable(() -> getRssFeed(channelIds));
    }
    
    public MediaGroup getRecommended() {
        Log.d(TAG, "Getting recommended...");

        checkSigned();

        kotlin.Pair<List<MediaGroup>, String> home = getBrowseService2().getHome();

        List<MediaGroup> groups = home != null ? home.getFirst() : null;

        return groups != null && !groups.isEmpty() ? groups.get(0) : null;
    }

    
    public Observable<MediaGroup> getRecommendedObserve() {
        return RxHelper.fromCallable(this::getRecommended);
    }

    
    public MediaGroup getHistory() {
        Log.d(TAG, "Getting history...");

        checkSigned();

        return getBrowseService2().getHistory();
    }

    
    public Observable<MediaGroup> getHistoryObserve() {
        return RxHelper.fromCallable(this::getHistory);
    }

    
    public MediaGroup getGroup(String reloadPageKey) {
        return getBrowseService2().getGroup(reloadPageKey, MediaGroup.TYPE_UNDEFINED, null);
    }

    
    public MediaGroup getGroup(MediaItem mediaItem) {
        return mediaItem.getReloadPageKey() != null ?
                getBrowseService2().getGroup(mediaItem.getReloadPageKey(), mediaItem.getType(), mediaItem.getTitle()) :
                getBrowseService2().getChannelAsGrid(mediaItem.getChannelId());
    }

    
    public Observable<MediaGroup> getGroupObserve(MediaItem mediaItem) {
        return RxHelper.fromCallable(() -> getGroup(mediaItem));
    }

    
    public Observable<MediaGroup> getGroupObserve(String reloadPageKey) {
        return RxHelper.fromCallable(() -> getGroup(reloadPageKey));
    }

    
    public List<MediaGroup> getHome() {
        checkSigned();

        List<MediaGroup> result = new ArrayList<>();
        kotlin.Pair<List<MediaGroup>, String> home = getBrowseService2().getHome();
        List<MediaGroup> groups = home != null ? home.getFirst() : null;

        if (groups == null) {
            Log.e(TAG, "Home group is empty");
            return null;
        }

        for (MediaGroup group : groups) {
            // Load chips
            if (group != null && group.isEmpty()) {
                List<MediaGroup> sections = getBrowseService2().continueEmptyGroup(group);

                if (sections != null) {
                    result.addAll(sections);
                }
            } else if (group != null) {
                result.add(group);
            }
        }

        return result;
    }

    
    public Observable<List<MediaGroup>> getHomeObserve() {
        return RxHelper.create(emitter -> {
            checkSigned();

            emitGroups(emitter, getBrowseService2().getHome());
        });
    }

    
    public Observable<List<MediaGroup>> getTrendingObserve() {
        return RxHelper.create(emitter -> {
            checkSigned();

            emitGroups(emitter, getBrowseService2().getTrending());
        });
    }

    
    public Observable<MediaGroup> getShortsObserve() {
        return RxHelper.create(emitter -> {
            checkSigned();

            MediaGroup shorts = getBrowseService2().getShorts();

            if (shorts != null && shorts.getNextPageKey() != null) {
                emitGroup(emitter, shorts);
            } else {
                emitGroupPartial(emitter, shorts);
                emitGroup(emitter, getBrowseService2().getShorts2());
            }
        });
    }

    
    public Observable<List<MediaGroup>> getKidsHomeObserve() {
        return RxHelper.create(emitter -> {
            checkSigned();

            emitGroups(emitter, getBrowseService2().getKidsHome());
        });
    }

    
    public Observable<List<MediaGroup>> getSportsObserve() {
        return RxHelper.create(emitter -> {
            checkSigned();

            emitGroups(emitter, getBrowseService2().getSports());
        });
    }

    
    public Observable<List<MediaGroup>> getLiveObserve() {
        return RxHelper.create(emitter -> {
            checkSigned();

            emitGroups(emitter, getBrowseService2().getLive());
        });
    }

    
    public Observable<MediaGroup> getMyVideosObserve() {
        return RxHelper.fromCallable(getBrowseService2()::getMyVideos);
    }

    
    public Observable<List<MediaGroup>> getMusicObserve() {
        return RxHelper.create(emitter -> {
            checkSigned();

            MediaGroup firstRow = getBrowseService2().getLikedMusic();
            emitGroupsPartial(emitter, Collections.singletonList(firstRow));

            emitGroups(emitter, getBrowseService2().getMusic());
        });
    }

    
    public Observable<List<MediaGroup>> getNewsObserve() {
        return RxHelper.create(emitter -> {
            checkSigned();

            emitGroups(emitter, getBrowseService2().getNews());
        });
    }

    
    public Observable<List<MediaGroup>> getGamingObserve() {
        return RxHelper.create(emitter -> {
            checkSigned();

            emitGroups(emitter, getBrowseService2().getGaming());
        });
    }

    
    public Observable<List<MediaGroup>> getChannelObserve(String channelId) {
        return getChannelObserve(channelId, null, null);
    }

    
    public Observable<List<MediaGroup>> getChannelObserve(MediaItem item) {
        return getChannelObserve(item.getChannelId(), item.getTitle(), item.getParams());
    }

    private Observable<List<MediaGroup>> getChannelObserve(String channelId, String title, String params) {
        return RxHelper.create(emitter -> {
            checkSigned();

            String canonicalId = UtilsService.canonicalChannelId(channelId);

            // Special type of channel that could be found inside Music section (see Liked row More button)
            if (YouTubeHelper.isGridChannel(canonicalId)) {
                MediaGroup gridChannel = getBrowseService2().getGridChannel(canonicalId, params);

                if (gridChannel instanceof BaseMediaGroup && !gridChannel.isEmpty()) {
                    ((BaseMediaGroup) gridChannel).setTitle(title);
                    emitGroups(emitter, Collections.singletonList(gridChannel));
                } else {
                    kotlin.Pair<List<MediaGroup>, String> channel = getBrowseService2().getChannel(canonicalId, params);
                    emitGroups(emitter, channel);
                }
            } else {
                kotlin.Pair<List<MediaGroup>, String> channel = getBrowseService2().getChannel(canonicalId, params);
                emitGroups(emitter, channel);
            }
        });
    }

    
    public MediaGroup getChannelSearch(String channelId, String query) {
        checkSigned();

        return getBrowseService2().getChannelSearch(channelId, query);
    }

    
    public Observable<MediaGroup> getChannelSearchObserve(String channelId, String query) {
        return RxHelper.fromCallable(() -> getChannelSearch(channelId, query));
    }

    private void emitGroups(ObservableEmitter<List<MediaGroup>> emitter, kotlin.Pair<List<MediaGroup>, String> groupsAndKey) {
        emitGroupsPartial(emitter, groupsAndKey);

        emitter.onComplete();
    }

    private void emitGroupsPartial(ObservableEmitter<List<MediaGroup>> emitter, kotlin.Pair<List<MediaGroup>, String> groupsAndKey) {
        if (groupsAndKey == null) {
            Log.e(TAG, "emitGroupsPartial: groupsAndKey is null");
            return;
        }

        List<MediaGroup> groups = groupsAndKey.getFirst();
        String nextKey = groupsAndKey.getSecond();

        while (groups != null && !groups.isEmpty()) {
            emitGroupsPartial(emitter, groups);
            groupsAndKey = getBrowseService2().continueSectionList(nextKey, groups.get(0).getType());
            groups = groupsAndKey != null ? groupsAndKey.getFirst() : null;
            nextKey = groupsAndKey != null ? groupsAndKey.getSecond() : null;
        }
    }

    private void emitGroups(ObservableEmitter<List<MediaGroup>> emitter, List<MediaGroup> groups) {
        emitGroupsPartial(emitter, groups);

        emitter.onComplete();
    }

    private void emitGroupsPartial(ObservableEmitter<List<MediaGroup>> emitter, List<MediaGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            Log.e(TAG, "emitGroupsPartial: groups are null or empty");
            return;
        }

        MediaGroup firstGroup = groups.get(0);
        Log.d(TAG, "emitGroupsPartial: begin emitting group of type %s...", firstGroup != null ? firstGroup.getType() : null);

        List<MediaGroup> collector = new ArrayList<>();

        for (MediaGroup group : groups) { // Preserve positions
            if (group == null) {
                continue;
            }

            if (group.isEmpty()) { // Contains Chips (nested sections)?
                if (!collector.isEmpty()) {
                    emitter.onNext(collector);
                    collector = new ArrayList<>();
                }

                List<MediaGroup> sections = getBrowseService2().continueEmptyGroup(group);

                if (sections != null) {
                    emitter.onNext(sections);
                }
            } else {
                collector.add(group);
            }
        }

        if (!collector.isEmpty()) {
            emitter.onNext(collector);
        }
    }

    private void emitGroup(ObservableEmitter<MediaGroup> emitter, MediaGroup group) {
        emitGroupPartial(emitter, group);

        emitter.onComplete();
    }

    private void emitGroupPartial(ObservableEmitter<MediaGroup> emitter, MediaGroup group) {
        if (group == null) {
            Log.e(TAG, "emitGroupPartial: group is null");
            return;
        }

        Log.d(TAG, "emitGroupPartial: begin emitting group of type %s...", group.getType());

        emitter.onNext(group);
    }

    
    public MediaGroup continueGroup(MediaGroup mediaGroup) {
        MediaGroup result = continueGroupChecked(mediaGroup);

        if (result == null) {
            return null;
        }

        if (result.isEmpty()) {
            // All contents has been filtered (e.g. shorts)
            return continueGroupChecked(result);
        }

        return result;
    }

    private MediaGroup continueGroupChecked(MediaGroup mediaGroup) {
        MediaGroup result = continueGroupInt(mediaGroup);

        if (result == null) {
            return null;
        }

        if (Helpers.equals(mediaGroup.getMediaItems(), result.getMediaItems()) &&
                Helpers.equals(mediaGroup.getNextPageKey(), result.getNextPageKey())) {
            // Result group is duplicate of the original. Seems that we've reached the end before. Skipping...
            return null;
        }

        return result;
    }

    private MediaGroup continueGroupInt(MediaGroup mediaGroup) {
        if (mediaGroup == null) {
            return null;
        }

        checkSigned();

        Log.d(TAG, "Continue group " + mediaGroup.getTitle() + "...");

        if (mediaGroup instanceof SuggestionsGroup) {
            return getWatchNextService().continueGroup(mediaGroup);
        }

        if (mediaGroup instanceof BaseMediaGroup) {
            MediaGroup group = null;

            // Fix channels with multiple empty groups (e.g. https://www.youtube.com/@RuhiCenetMedya/videos)
            for (int i = 0; i < 3; i++) {
                group = getBrowseService2().continueGroup(group == null ? mediaGroup : group);

                if (group == null || !group.isEmpty()) {
                    break;
                }
            }

            return group;
        }

        String nextKey = YouTubeHelper.extractNextKey(mediaGroup);

        switch (mediaGroup.getType()) {
            case MediaGroup.TYPE_SEARCH:
                return MediaGroup.from(
                        getSearchService().continueSearch(nextKey),
                        mediaGroup);
            case MediaGroup.TYPE_HISTORY:
            case MediaGroup.TYPE_SUBSCRIPTIONS:
            case MediaGroup.TYPE_USER_PLAYLISTS:
            case MediaGroup.TYPE_CHANNEL_UPLOADS:
            case MediaGroup.TYPE_UNDEFINED:
                return MediaGroup.from(
                        getBrowseService().continueGridTab(nextKey),
                        mediaGroup
                );
            default:
                return MediaGroup.from(
                        getBrowseService().continueSection(nextKey),
                        mediaGroup
                );
        }
    }

    
    public Observable<MediaGroup> continueGroupObserve(MediaGroup mediaGroup) {
        return RxHelper.fromCallable(() -> continueGroup(mediaGroup));
    }

    private void checkSigned() {
        getSignInService().checkAuth();
    }

    
    public Observable<List<MediaGroup>> getPlaylistRowsObserve() {
        return RxHelper.create(emitter -> {
            checkSigned();

            MediaGroup playlists = getPlaylists();

            if (playlists != null && playlists.getMediaItems() != null) {
                for (MediaItem playlist : playlists.getMediaItems()) {
                    kotlin.Pair<List<MediaGroup>, String> content = getBrowseService2().getChannel(playlist.getChannelId(), playlist.getParams());
                    if (content != null && content.getFirst() != null) {
                        MediaGroup mediaGroup = content.getFirst().get(0);
                        if (mediaGroup instanceof BaseMediaGroup) {
                            ((BaseMediaGroup) mediaGroup).setTitle(playlist.getTitle());
                        }
                        emitter.onNext(content.getFirst());
                    }
                }
                emitter.onComplete();
            } else {
                RxHelper.onError(emitter, "getPlaylistsRowObserve: the content is null");
            }
        });
    }

    
    public Observable<MediaGroup> getPlaylistsObserve() {
        return RxHelper.fromCallable(this::getPlaylists);
    }

    private MediaGroup getPlaylists() {
        checkSigned();

        return getBrowseService2().getMyPlaylists();
    }

    
    public void clearSearchHistory() {
        getActionsService().clearSearchHistory();
        getSearchService().clearSearchHistory();
    }

    @NonNull
    private static SignInService getSignInService() {
        return SignInService.instance();
    }

    @NonNull
    private static ActionsService getActionsService() {
        return ActionsServiceWrapper.instance();
    }

    @NonNull
    private static SearchService getSearchService() {
        return SearchServiceWrapper.instance();
    }

    @NonNull
    private static BrowseService getBrowseService() {
        return BrowseService.instance();
    }

    @NonNull
    public static BrowseService2 getBrowseService2() {
        return BrowseService2Wrapper.INSTANCE;
    }

    @NonNull
    private static WatchNextService getWatchNextService() {
        return WatchNextServiceWrapper.INSTANCE;
    }

    @Nullable
    private static MediaServiceData getMediaServiceData() {
        return MediaServiceData.instance();
    }
}
