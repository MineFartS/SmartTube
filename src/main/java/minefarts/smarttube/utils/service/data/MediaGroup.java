package minefarts.smarttube.utils.service.data;

import minefarts.smarttube.utils.data.MediaItem;
import minefarts.smarttube.utils.browse.models.grid.GridTab;
import minefarts.smarttube.utils.browse.models.grid.GridTabContinuation;
import minefarts.smarttube.utils.browse.models.sections.Chip;
import minefarts.smarttube.utils.browse.models.sections.SectionContinuation;
import minefarts.smarttube.utils.browse.models.sections.Section;
import minefarts.smarttube.utils.common.models.items.ItemWrapper;
import minefarts.smarttube.utils.next.v1.models.SuggestedSection;
import minefarts.smarttube.utils.next.v1.result.WatchNextResultContinuation;
import minefarts.smarttube.utils.search.models.SearchResult;
import minefarts.smarttube.utils.search.models.SearchResultContinuation;
import minefarts.smarttube.utils.search.models.SearchSection;
import minefarts.smarttube.google.common.helpers.YouTubeHelper;

import java.util.ArrayList;
import java.util.List;

public class MediaGroup {

    public static final int TYPE_UNDEFINED = -1;
    public static final int TYPE_HOME = 0;
    public static final int TYPE_SEARCH = 1;
    public static final int TYPE_RECOMMENDED = 2;
    public static final int TYPE_HISTORY = 3;
    public static final int TYPE_SUBSCRIPTIONS = 4;
    public static final int TYPE_MUSIC = 5;
    public static final int TYPE_NEWS = 6;
    public static final int TYPE_GAMING = 7;
    public static final int TYPE_USER_PLAYLISTS = 8;
    public static final int TYPE_SUGGESTIONS = 9;
    public static final int TYPE_CHANNEL = 10;
    public static final int TYPE_SETTINGS = 11;
    public static final int TYPE_CHANNEL_UPLOADS = 12;
    public static final int TYPE_KIDS_HOME = 13;
    public static final int TYPE_TRENDING = 14;
    public static final int TYPE_SHORTS = 15;
    public static final int TYPE_NOTIFICATIONS = 16;
    public static final int TYPE_SPORTS = 17;
    public static final int TYPE_MOVIES = 18;
    public static final int TYPE_LIVE = 19;
    public static final int TYPE_MY_VIDEOS = 20;
    public static final int TYPE_PLAYBACK_QUEUE = 21;

    private final int mType;
    private String mTitle;
    private List<MediaItem> mMediaItems;
    public String mNextPageKey;
    private String mChannelUrl;
    private String mChannelId;
    private String mParams;
    private String mReloadPageKey;

    public MediaGroup(int type) {
        mType = type;
    }

    public static MediaGroup from(GridTab browseResult, int type) {
        if (browseResult == null) {
            return null;
        }

        return create(new MediaGroup(type), browseResult.getItemWrappers(), browseResult.getNextPageKey());
    }

    public static MediaGroup from(GridTabContinuation continuation, String reloadPageKey, String groupTitle, int groupType) {
        MediaGroup baseGroup = new MediaGroup(groupType);
        baseGroup.mReloadPageKey = reloadPageKey;
        MediaGroup mediaGroup = from(continuation, baseGroup);
        if (mediaGroup instanceof MediaGroup) {
            ((MediaGroup) mediaGroup).setTitle(groupTitle);
        }
        return mediaGroup;
    }

    public static MediaGroup from(GridTabContinuation continuation, MediaGroup baseGroup) {
        if (continuation == null || baseGroup == null) {
            return null;
        }

        MediaGroup newGroup = new MediaGroup(baseGroup.getType());
        newGroup.mTitle = baseGroup.getTitle();

        // Subscribed channel view. Add details.
        if (continuation.getBrowseId() != null) {
            newGroup.mChannelId = continuation.getBrowseId();
        }
        if (continuation.getParams() != null) {
            newGroup.mParams = continuation.getParams();
        }
        if (continuation.getCanonicalBaseUrl() != null) {
            newGroup.mChannelUrl = continuation.getCanonicalBaseUrl();
        }

        return create(newGroup, continuation.getItemWrappers(), continuation.getNextPageKey());
    }

    public static MediaGroup from(WatchNextResultContinuation continuation, MediaGroup baseGroup) {
        if (continuation == null) {
            return null;
        }

        MediaGroup newGroup = new MediaGroup(baseGroup.getType());
        newGroup.mTitle = baseGroup.getTitle();

        return create(newGroup, continuation.getItemWrappers(), continuation.getNextPageKey());
    }

    public static MediaGroup from(Section section, int type) {
        if (section == null) return null;

        MediaGroup mg = new MediaGroup(type);
        mg.mTitle = section.getTitle();
        mg.mNextPageKey = section.getNextPageKey();

        return create(mg, section.getItemWrappers(), section.getNextPageKey());
    }

    public static MediaGroup from(Chip chip, int type) {
        if (chip == null) return null;

        MediaGroup mg = new MediaGroup(type);
        mg.mTitle = chip.getTitle();
        mg.mNextPageKey = chip.getReloadPageKey();

        return create(mg, null, chip.getReloadPageKey());
    }

    public static MediaGroup from(SectionContinuation continuation, MediaGroup baseGroup) {
        if (continuation == null || baseGroup == null) return null;

        MediaGroup mg = new MediaGroup(baseGroup.getType());
        mg.mTitle = baseGroup.getTitle();

        return create(mg, continuation.getItemWrappers(), continuation.getNextPageKey());
    }

    public static List<MediaGroup> from(SearchResult searchResult, int type) {
        if (searchResult == null || searchResult.getSections() == null || searchResult.getSections().size() == 0) {
            return null;
        }

        List<MediaGroup> result = new ArrayList<>();

        for (SearchSection section : searchResult.getSections()) {
            result.add(from(section, type));
        }

        return result;
    }

    public static MediaGroup from(SearchSection searchSection, int type) {
        if (searchSection == null) {
            return null;
        }

        MediaGroup mg = new MediaGroup(type);
        mg.setTitle(searchSection.getTitle());

        return create(mg, searchSection.getItemWrappers(), searchSection.getNextPageKey());
    }

    public static MediaGroup from(SearchResultContinuation continuation, MediaGroup baseGroup) {
        if (continuation == null || baseGroup == null) return null;

        MediaGroup mg = new MediaGroup(baseGroup.getType());
        mg.mTitle = baseGroup.getTitle();

        return create(mg, continuation.getItemWrappers(), continuation.getNextPageKey());
    }

    public static MediaGroup from(Chip chip) {
        if (chip == null) return null;

        MediaGroup mg = new MediaGroup(TYPE_SUGGESTIONS);
        mg.mTitle = chip.getTitle();

        return create(mg, chip.getItemWrappers(), chip.getNextPageKey());
    }

    public static MediaGroup from(SuggestedSection section) {
        if (section == null) return null;

        MediaGroup mg = new MediaGroup(TYPE_SUGGESTIONS);
        mg.mTitle = section.getTitle();

        return create(mg, section.getItemWrappers(), section.getNextPageKey());
    }

    public static List<MediaGroup> from(List<Section> sections, int type) {
        List<MediaGroup> result = new ArrayList<>();

        if (sections != null && sections.size() > 0) {
            for (Section section : sections) {
                // Section contains chips (nested sections) or items. Not both.
                if (section.getChips() != null) {
                    for (Chip chip : section.getChips()) {
                        result.add(from(chip, type));
                    }
                } else {
                    result.add(from(section, type));
                }
            }
        }

        return result;
    }

    public static MediaGroup fromTabs(List<GridTab> tabs, int type) {
        return create(new MediaGroup(type), tabs);
    }

    public List<MediaItem> getMediaItems() {
        return mMediaItems;
    }

    public void setMediaItems(List<MediaItem> items) {
        mMediaItems = items;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public int getType() {
        return mType;
    }

    public String getChannelId() {
        return mChannelId;
    }

    public String getChannelUrl() {
        return mChannelUrl;
    }

    public String getParams() {
        return mParams;
    }

    public String getReloadPageKey() {
        return mReloadPageKey;
    }

    public String getNextPageKey() {
        return mNextPageKey;
    }

    public boolean isEmpty() {
        return mMediaItems == null || mMediaItems.isEmpty();
    }

    private static MediaGroup create(MediaGroup baseGroup, List<GridTab> tabs) {
        ArrayList<MediaItem> mediaItems = new ArrayList<>();

        if (tabs != null) {
            for (GridTab tab : tabs) {
                if (tab.isUnselectable()) {
                    continue;
                }

                YouTubeMediaItem item = YouTubeMediaItem.from(tab, baseGroup.getType());

                mediaItems.add(item);
            }
        }

        // Fix duplicated items after previous group reuse
        baseGroup.mMediaItems = !mediaItems.isEmpty() ? mediaItems : null;

        return baseGroup;
    }

    private static MediaGroup create(MediaGroup baseGroup, List<ItemWrapper> items, String nextPageKey) {
        ArrayList<MediaItem> mediaItems = new ArrayList<>();

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                ItemWrapper item = items.get(i);
                YouTubeMediaItem mediaItem = YouTubeMediaItem.from(item, i);
                if (mediaItem != null) {
                    mediaItem.setParams(baseGroup.mParams);
                    mediaItems.add(mediaItem);
                }
            }
        }

        // Fix duplicated items after previous group reuse
        baseGroup.mMediaItems = !mediaItems.isEmpty() ? mediaItems : null;
        baseGroup.mNextPageKey = nextPageKey;

        YouTubeHelper.filterIfNeeded(baseGroup);

        return baseGroup;
    }
}
