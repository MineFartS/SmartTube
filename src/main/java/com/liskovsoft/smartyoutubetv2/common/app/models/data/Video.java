package com.liskovsoft.smartyoutubetv2.common.app.models.data;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liskovsoft.mediaserviceinterfaces.data.ItemGroup;
import com.liskovsoft.mediaserviceinterfaces.data.ChapterItem;
import com.liskovsoft.mediaserviceinterfaces.data.DislikeData;
import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItem;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemFormatInfo;
import com.liskovsoft.mediaserviceinterfaces.data.ItemGroup.Item;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;
import com.liskovsoft.mediaserviceinterfaces.data.NotificationState;
import com.liskovsoft.mediaserviceinterfaces.data.PlaylistInfo;
import com.liskovsoft.sharedutils.helpers.DateHelper;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.service.VideoStateService;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerTweaksData;
import com.liskovsoft.googlecommon.common.helpers.ServiceHelper;
import com.liskovsoft.googlecommon.common.helpers.YouTubeHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a video item with associated metadata used throughout the app.
 *
 * Contains identifiers, UI titles, images, playback info and helper methods
 * to convert/sync with service interfaces types.
 */
public final class Video {
    /** Constant used for liked music playlist id. */
    public static final String PLAYLIST_LIKED_MUSIC = "LM";
    /** Delimiter used in tertiary text fields. */
    public static final String TERTIARY_TEXT_DELIM = "•";
    /** Maximum live duration to consider reasonable (ms). */
    public static final long MAX_LIVE_DURATION_MS = 24 * 60 * 60 * 1_000;
    private static final int MAX_AUTHOR_LENGTH_CHARS = 20;
    private static final String BLACK_PLACEHOLDER_URL = "https://via.placeholder.com/1280x720/000000/000000";
    // Minimum percent watched to consider restoring last position (ignore immediate closes).
    private static final float RESTORE_POSITION_PERCENTS = 10;

    // Public mutable fields representing video state and metadata.
    public int id;
    public String title;
    public String deArrowTitle;
    public CharSequence secondTitle;
    private String metadataTitle;
    private CharSequence metadataSecondTitle;
    public String description;
    public String category;
    public int itemType = -1;
    public String channelId;
    public String videoId;
    public String playlistId;
    public String remotePlaylistId;
    public int playlistIndex = -1;
    public String playlistParams;
    public String reloadPageKey;
    public String bgImageUrl;
    public String cardImageUrl;
    public String altCardImageUrl;
    public String author;
    public String badge;
    public String previewUrl;
    public float percentWatched = -1;
    public int startTimeSeconds;
    public MediaItem mediaItem;
    public MediaItem nextMediaItem;
    public MediaItem shuffleMediaItem;
    public PlaylistInfo playlistInfo;
    public boolean hasNewContent;
    public boolean isLive;
    public boolean isUpcoming;
    public boolean isUnplayable;
    public boolean isShorts;
    public boolean isChapter;
    public boolean isMovie;
    public boolean isSubscribed;
    public boolean isRemote;
    public int groupPosition = -1; // group position in multi-grid fragments
    public String clickTrackingParams;
    public boolean isSynced;
    /** Timestamp when the Video instance was created. Useful for caching/ordering. */
    public final long timestamp = System.currentTimeMillis();
    public int sectionId = -1;
    public String channelGroupId;
    public long startTimeMs;
    public long pendingPosMs;
    public boolean fromQueue;
    public boolean isPending;
    public boolean finishOnEnded;
    public boolean incognito;
    public String likeCount;
    public String dislikeCount;
    public String subscriberCount;
    public float volume = 1.0f;
    public boolean deArrowProcessed;
    public boolean isLiveEnd;
    public boolean isShuffled;
    public String searchQuery;
    private int startSegmentNum;
    private long liveDurationMs = -1;
    private long durationMs = -1;
    // WeakReference to avoid memory leaks from holding a strong reference to parent group
    private WeakReference<VideoGroup> group;
    public List<NotificationState> notificationStates;

    /** Default constructor — creates an empty Video object. */
    public Video() {
       // NOP
    }

    /**
     * Private convenience constructor used by internal factories.
     */
    private Video(
            final int id,
            final String category,
            final String title,
            final String desc,
            final String videoId,
            final String bgImageUrl,
            final String cardImageUrl,
            final String author) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.secondTitle = desc;
        this.videoId = videoId;
        this.bgImageUrl = bgImageUrl;
        this.cardImageUrl = cardImageUrl;
        this.author = author;
    }

    /**
     * Create Video from a MediaItem returned by service interfaces.
     *
     * @param item media item, may be null
     * @return new Video or null if item is null
     */
    public static Video from(MediaItem item) {
        if (item == null) {
            return null;
        }

        Video video = new Video();

        video.id = item.getId();
        video.title = item.getTitle();
        video.secondTitle = item.getSecondTitle();
        video.category = item.getContentType();
        video.itemType = item.getType();
        video.videoId = item.getVideoId();
        video.channelId = item.getChannelId();
        video.bgImageUrl = item.getBackgroundImageUrl();
        video.cardImageUrl = item.getCardImageUrl();
        video.author = item.getAuthor();
        video.percentWatched = item.getPercentWatched();
        video.startTimeSeconds = item.getStartTimeSeconds();
        video.badge = item.getBadgeText();
        video.hasNewContent = item.hasNewContent();
        video.previewUrl = item.getVideoPreviewUrl();
        video.playlistId = item.getPlaylistId();
        video.playlistIndex = item.getPlaylistIndex();
        video.playlistParams = item.getParams();
        video.reloadPageKey = item.getReloadPageKey();
        video.isLive = item.isLive();
        video.isUpcoming = item.isUpcoming();
        video.isShorts = item.isShorts();
        video.isMovie = item.isMovie();
        video.clickTrackingParams = item.getClickTrackingParams();
        video.durationMs = item.getDurationMs();
        video.searchQuery = item.getSearchQuery();
        video.mediaItem = item;

        return video;
    }

    /** Create a shallow copy from another Video instance. */
    public static Video from(Video item) {
        Video video = new Video();

        video.id = item.id;
        video.title = item.title;
        video.category = item.category;
        video.itemType = item.itemType;
        video.secondTitle = item.secondTitle;
        video.videoId = item.videoId;
        video.channelId = item.channelId;
        video.bgImageUrl = item.bgImageUrl;
        video.cardImageUrl = item.cardImageUrl;
        video.author = item.author;
        video.percentWatched = item.percentWatched;
        video.badge = item.badge;
        video.hasNewContent = item.hasNewContent;
        video.previewUrl = item.previewUrl;
        video.playlistId = item.playlistId;
        video.playlistIndex = item.playlistIndex;
        video.playlistParams = item.playlistParams;
        video.reloadPageKey = item.getReloadPageKey();
        video.isLive = item.isLive;
        video.isUpcoming = item.isUpcoming();
        video.clickTrackingParams = item.clickTrackingParams;
        video.mediaItem = item.mediaItem;
        video.group = item.group;

        return video;
    }

    /** Create a Video with only videoId set. */
    public static Video from(String videoId) {
        Video video = new Video();
        video.videoId = videoId;
        return video;
    }

    /** Create a Video from a chapter item (used for chapters). */
    public static Video from(ChapterItem chapter) {
        Video video = new Video();
        video.isChapter = true;
        video.title = chapter.getTitle();
        video.cardImageUrl = chapter.getCardImageUrl();
        video.startTimeMs = chapter.getStartTimeMs();
        video.badge = ServiceHelper.millisToTimeText(chapter.getStartTimeMs());
        return video;
    }

    /** Create a Video representing an item group (e.g. channel group). */
    public static Video from(ItemGroup group) {
        Video video = new Video();
        video.title = group.getTitle();
        video.cardImageUrl = group.getIconUrl();
        video.channelGroupId = group.getId();
        return video;
    }

    /** Create a Video representing a channel item. */
    public static Video from(Item channel) {
        Video video = new Video();
        video.title = channel.getTitle();
        video.cardImageUrl = channel.getIconUrl();
        video.channelId = channel.getChannelId();
        return video;
    }

    /**
     * Equality is based on stable hashCode and mix-flag to keep adapters/scrolling logic working.
     * Use with caution — original equals was by reference.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Video) {
            Video video = (Video) obj;

            return hashCode() == video.hashCode() && isMix() == video.isMix();
        }

        return false;
    }

    /**
     * Hash code uses a combination of identifying fields so equal content produces same id/hash.
     * Falls back to super.hashCode() when Helpers.hashCodeAny returns -1.
     */
    @Override
    public int hashCode() {
        int hashCode = Helpers.hashCodeAny(videoId, playlistId, reloadPageKey, playlistParams, channelId, sectionId, channelGroupId, mediaItem);
        return hashCode != -1 ? hashCode : super.hashCode();
    }

    /** Utility to show debug info about a video on screen (short message). */
    public static void printDebugInfo(Context context, Video item) {
        MessageHelpers.showLongMessage(context,
                String.format("videoId=%s, playlistId=%s, reloadPageKey=%s, playlistParams=%s, channelId=%s, mediaItem=%s, extra=%s",
                        item.videoId, item.playlistId, item.reloadPageKey, item.playlistParams, item.channelId, item.mediaItem, item.sectionId)
        );
    }
    
    /** Null-safe equals wrapper. */
    public static boolean equals(Video video1, Video video2) {
        if (video1 == null) {
            return false;
        }

        return video1.equals(video2);
    }

    /** Check if video object is empty (no id). */
    public static boolean isEmpty(Video video) {
        return video == null || video.videoId == null;
    }

    /**
     * Return stable id; if not set, generate from hashCode.
     *
     * @return id
     */
    public int getId() {
        if (id == 0 || id == -1) {
            id = hashCode();
        }

        return id;
    }

    /** Title preferred for UI — deArrowTitle overrides original title when present. */
    public String getTitle() {
        return deArrowTitle != null ? deArrowTitle : title;
    }

    /** Full title uses metadata when available (prefer metadataTitle). */
    public String getTitleFull() {
        return deArrowTitle != null ? deArrowTitle : metadataTitle != null ? metadataTitle : title;
    }

    /** Return secondary title as provided by source. */
    public CharSequence getSecondTitle() {
        return secondTitle;
    }

    /** Prefer metadata secondary title unless upcoming (metadata may be inaccurate for upcoming). */
    public CharSequence getSecondTitleFull() {
        return metadataSecondTitle != null && !isUpcoming ? metadataSecondTitle : secondTitle;
    }

    /** Return effective playlist id, prefer remotePlaylistId for remote items. */
    public String getPlaylistId() {
        return isRemote && remotePlaylistId != null ? remotePlaylistId : playlistId;
    }

    /** Return card image URL, allow override via altCardImageUrl. */
    public String getCardImageUrl() {
        return altCardImageUrl != null ? altCardImageUrl : cardImageUrl;
    }

    /**
     * Compute author display name.
     * If author field is missing, attempt to extract from titles or fallback to YouTubeHelper.
     */
    public String getAuthor() {
        if (author != null) {
            return author;
        }

        String mainTitle = metadataTitle != null ? metadataTitle : title;
        CharSequence subtitle = metadataSecondTitle != null ? metadataSecondTitle : secondTitle;
        // If this item represents a video we extract author from secondary title, otherwise synthesize info.
        return hasVideo() ? extractAuthor(subtitle) : Helpers.toString(YouTubeHelper.createInfo(mainTitle, subtitle));
    }

    /** Get parent group (may be null). */
    public VideoGroup getGroup() {
        return group != null ? group.get() : null;
    }

    /** Set parent group using a weak reference (prevent memory leaks). */
    public void setGroup(VideoGroup group) {
        this.group = new WeakReference<>(group);
    }

    /** Return position of this video within its group, or -1 when unknown. */
    public int getPositionInsideGroup() {
        return getGroup() != null && !getGroup().isEmpty() ? getGroup().getVideos().indexOf(this) : -1;
    }

    private static String extractAuthor(CharSequence secondTitle) {
        return extractAuthor(Helpers.toString(secondTitle));
    }

    /**
     * Extract author from a secondary title string. Attempts to skip labels (4K, LIVE etc)
     * and returns null when the subtitle starts with a number (views).
     */
    private static String extractAuthor(String secondTitle) {
        String result = null;

        if (secondTitle != null) {
            // Remove explicit LIVE marker fragment added by some sources.
            secondTitle = secondTitle.replace(TERTIARY_TEXT_DELIM + " LIVE", "");
            String[] split = secondTitle.split(TERTIARY_TEXT_DELIM);

            if (split.length <= 1) {
                result = secondTitle;
            } else {
                // Heuristic: first fragment may be a short label; choose appropriate part for author.
                result = split.length < 4 && split[0].trim().length() > 2 ? split[0] : split[1];
            }
        }

        // Skip subtitles starting with numeric (e.g. "1.4M views").
        return !TextUtils.isEmpty(result) && !Helpers.isNumeric(result.substring(0, 1)) ? Helpers.abbreviate(result.trim(), MAX_AUTHOR_LENGTH_CHARS) : null;
    }

    /** Find all videos by the given author in the provided group. */
    public static List<Video> findVideosByAuthor(VideoGroup group, String author) {
        List<Video> result = new ArrayList<>();

        if (group != null && group.getVideos() != null) {
            for (Video video : group.getVideos()) {
                if (Helpers.equals(video.getAuthor(), author)) {
                    result.add(video);
                }
            }
        }

        return result;
    }

    public int describeContents() {
        return 0;
    }

    /**
     * Deserialize a Video from string representation produced by toString().
     * This method contains several backward-compatibility patches for older formats.
     *
     * @param spec serialized string
     * @return Video or null on parse failure
     */
    public static Video fromString(String spec) {
        if (spec == null) {
            return null;
        }

        String[] split = Helpers.splitObj(spec);

        // Backward compatibility: extend arrays for older serialized formats.
        if (split.length == 10) {
            split = Helpers.appendArray(split, new String[]{null});
        }
        if (split.length == 11) {
            split = Helpers.appendArray(split, new String[]{"-1"});
        }
        if (split.length == 12) {
            split = Helpers.appendArray(split, new String[]{null});
        }
        if (split.length == 13) {
            split = Helpers.appendArray(split, new String[]{"-1"});
        }
        if (split.length == 14) {
            split = Helpers.appendArray(split, new String[]{null});
        }
        if (split.length == 15) {
            split = Helpers.appendArray(split, new String[]{null});
        }
        if (split.length == 16) {
            split = Helpers.appendArray(split, new String[]{"-1"});
        }
        if (split.length == 17) {
            split = Helpers.appendArray(split, new String[]{null});
        }
        if (split.length == 18) {
            split = Helpers.appendArray(split, new String[]{null});
        }
        if (split.length == 19) {
            split = Helpers.appendArray(split, new String[]{null});
        }
        if (split.length == 20) {
            split = Helpers.appendArray(split, new String[]{"false"});
        }
        if (split.length == 21) {
            split = Helpers.appendArray(split, new String[]{"-1"});
        }
        if (split.length == 22) {
            split = Helpers.appendArray(split, new String[]{null});
        }

        if (split.length != 23) {
            return null;
        }

        Video result = new Video();

        result.id = Helpers.parseInt(split[0]);
        result.category = Helpers.parseStr(split[1]);
        result.title = Helpers.parseStr(split[2]);
        result.videoId = Helpers.parseStr(split[3]);
        //result.videoUrl = Helpers.parseStr(split[4]);
        result.playlistId = Helpers.parseStr(split[5]);
        result.channelId = Helpers.parseStr(split[6]);
        result.bgImageUrl = Helpers.parseStr(split[7]);
        result.cardImageUrl = Helpers.parseStr(split[8]);
        //result.mediaItem = YouTubeMediaItem.deserializeMediaItem(Helpers.parseStr(split[9]));
        result.playlistParams = Helpers.parseStr(split[10]);
        result.sectionId = Helpers.parseInt(split[11]);
        result.reloadPageKey = Helpers.parseStr(split[12]);
        result.itemType = Helpers.parseInt(split[13]);
        result.secondTitle = Helpers.parseStr(split[14]);
        result.previewUrl = Helpers.parseStr(split[15]);
        result.percentWatched = Helpers.parseFloat(split[16]);
        result.metadataTitle = Helpers.parseStr(split[17]);
        result.metadataSecondTitle = Helpers.parseStr(split[18]);
        result.badge = Helpers.parseStr(split[19]);
        result.isLive = Helpers.parseBoolean(split[20]);
        result.channelGroupId = Helpers.parseStr(split[21]);
        result.searchQuery = Helpers.parseStr(split[22]);

        // Reset legacy sentinel value.
        if (Helpers.equals(result.channelGroupId, "-1")) {
            result.channelGroupId = null;
        }

        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return Helpers.mergeObj(id, category, title, videoId, null, playlistId, channelId, bgImageUrl, cardImageUrl,
                null, playlistParams, sectionId, getReloadPageKey(), itemType, secondTitle, previewUrl, percentWatched,
                metadataTitle, metadataSecondTitle, badge, isLive, channelGroupId, searchQuery);
    }

    /** Convenience checks for presence of identifiers. */
    public boolean hasVideo() {
        return videoId != null;
    }

    public boolean hasChannel() {
        return channelId != null;
    }

    /**
     * Note: Channels section may use playlistParams instead of playlistId.
     * @return true if has playlist id
     */
    public boolean hasPlaylist() {
        return playlistId != null;
    }

    public boolean hasNextPlaylist() {
        return hasNextItem() && getPlaylistId() != null && getPlaylistId().equals(nextMediaItem.getPlaylistId());
    }

    /** Whether reloadPageKey (persisted paging) is available. */
    public boolean hasReloadPageKey() {
        return getReloadPageKey() != null;
    }

    public boolean hasNextPageKey() {
        return getNextPageKey() != null;
    }

    public boolean hasNextItem() {
        return nextMediaItem != null;
    }

    public boolean hasNestedItems() {
        return mediaItem != null && mediaItem.hasUploads();
    }

    public boolean hasPlaylistIndex() {
        return playlistIndex > 0;
    }

    public boolean isChannel() {
        return videoId == null && playlistId == null && channelId != null;
    }

    /** Channels that act as playlists (type PLAYLIST). */
    public boolean isPlaylistAsChannel() {
        return videoId == null && channelId != null && itemType == MediaItem.TYPE_PLAYLIST;
    }

    public boolean isPlaylistInChannel() {
        return belongsToChannel() && hasPlaylist() && !belongsToSamePlaylistGroup();
    }

    /** Heuristic to detect mixes (autogenerated mixes). */
    public boolean isMix() {
        return mediaItem != null && !isLive && !isLiveEnd && Helpers.hasWords(badge) &&
                (durationMs <= 0 || isSynced) && (hasPlaylist() || hasChannel() || hasNestedItems());
    }

    public boolean isFullLive() {
        return isLive && startSegmentNum == 0;
    }

    public boolean isEmpty() {
        if (isChapter) {
            return false;
        }

        // Movies "Free with Ads" are not supported — treat as empty for now.
        return Helpers.allNulls(videoId, playlistId, reloadPageKey, playlistParams, channelId, searchQuery) || isMovie;
    }

    public String getGroupTitle() {
        return getGroup() != null ? getGroup().getTitle() : null;
    }

    /**
     * Effective reloadPageKey: prefer local field, otherwise inherit from group.
     * Used to persist paging for Channels and User playlists.
     */
    public String getReloadPageKey() {
        return reloadPageKey != null ? reloadPageKey :
                getGroup() != null ? getGroup().getReloadPageKey() : null;
    }

    public String getNextPageKey() {
        return getGroup() != null ? getGroup().getNextPageKey() : null;
    }

    /** Return background image url or a black placeholder when missing. */
    public String getBackgroundUrl() {
        return bgImageUrl != null ? bgImageUrl : BLACK_PLACEHOLDER_URL;
    }

    /** Check whether first and last items in group belong to same author. */
    public boolean belongsToSameAuthorGroup() {
        if (getGroup() == null || getGroup().getSize() < 2) {
            return false;
        }

        Video first = getGroup().get(0);
        Video last = getGroup().get(getGroup().getSize() - 1);

        String author1 = extractAuthor(first.getSecondTitle());
        String author2 = extractAuthor(last.getSecondTitle());

        return author1 != null && author2 != null && Helpers.equals(author1, author2);
    }

    /** Check whether group items belong to same playlist. */
    public boolean belongsToSamePlaylistGroup() {
        if (getGroup() == null || getGroup().getSize() < 2) {
            return false;
        }

        // Filter items that actually have playlist id information.
        List<Video> filtered = Helpers.filter(getGroup().getVideos(), item -> item.getPlaylistId() != null || item.playlistParams != null, 10);

        if (filtered == null || filtered.size() < 2) {
            return false;
        }

        Video first = filtered.get(0);
        Video second = filtered.get(1);

        String playlist1 = first.getPlaylistId() != null ? first.getPlaylistId() : first.playlistParams;
        String playlist2 = second.getPlaylistId() != null ? second.getPlaylistId() : second.playlistParams;

        return playlist1 != null && playlist2 != null && Helpers.equals(playlist1, playlist2);
    }

    /** Internal helper used by section playlist heuristics. */
    private boolean checkAllVideosHasPlaylist() {
        if (getGroup() == null || getGroup().getSize() < 2) {
            return false;
        }

        return playlistId != null && getGroup().get(0).playlistId != null && getGroup().get(1).playlistId != null && getGroup().get(getGroup().getSize() - 1).playlistId != null;
    }

    // Convenience wrappers for group membership checks.
    public boolean belongsToHome() { return belongsToGroup(MediaGroup.TYPE_HOME); }
    public boolean belongsToChannel() { return belongsToGroup(MediaGroup.TYPE_CHANNEL); }
    public boolean belongsToChannelUploads() { return belongsToGroup(MediaGroup.TYPE_CHANNEL_UPLOADS); }
    public boolean belongsToSubscriptions() { return belongsToGroup(MediaGroup.TYPE_SUBSCRIPTIONS); }
    public boolean belongsToHistory() { return belongsToGroup(MediaGroup.TYPE_HISTORY); }
    public boolean belongsToMusic() { return belongsToGroup(MediaGroup.TYPE_MUSIC); }
    public boolean belongsToShorts() { return belongsToGroup(MediaGroup.TYPE_SHORTS); }
    public boolean belongsToShortsGroup() { return isShorts && (belongsToShorts() || belongsToHome()); }
    public boolean belongsToSearch() { return belongsToGroup(MediaGroup.TYPE_SEARCH); }
    public boolean belongsToNotifications() { return belongsToGroup(MediaGroup.TYPE_NOTIFICATIONS); }
    public boolean belongsToPlaybackQueue() { return belongsToGroup(MediaGroup.TYPE_PLAYBACK_QUEUE); }
    public boolean belongsToSuggestions() { return belongsToGroup(MediaGroup.TYPE_SUGGESTIONS); }
    public boolean belongsToUserPlaylists() { return belongsToGroup(MediaGroup.TYPE_USER_PLAYLISTS); }
    public boolean belongsToUndefined() { return belongsToGroup(MediaGroup.TYPE_UNDEFINED); }

    private boolean belongsToGroup(int groupId) {
        return getGroup() != null && getGroup().getType() == groupId;
    }

    public boolean belongsToSection() {
        return getGroup() != null && getGroup().getSection() != null;
    }

    /** Sync basic state (percentWatched) from another Video. */
    public void sync(Video video) {
        if (video == null) {
            return;
        }

        percentWatched = video.percentWatched;
    }

    /** Sync metadata coming from MediaItemMetadata (titles, description, subscription state etc). */
    public void sync(MediaItemMetadata metadata) {
        if (metadata == null) {
            return;
        }

        if (isLive && !metadata.isLive()) {
            isLiveEnd = true;
        }

        metadataTitle = metadata.getTitle();
        metadataSecondTitle = metadata.getSecondTitle();
        isLive = metadata.isLive();
        isUpcoming = metadata.isUpcoming();

        if (metadata.getDescription() != null) {
            description = metadata.getDescription();
        }
        channelId = metadata.getChannelId();
        nextMediaItem = findNextVideo(metadata);
        shuffleMediaItem = metadata.getShuffleVideo();
        playlistInfo = metadata.getPlaylistInfo();
        isSubscribed = metadata.isSubscribed();
        likeCount = metadata.getLikeCount();
        dislikeCount = metadata.getDislikeCount();
        subscriberCount = metadata.getSubscriberCount();
        notificationStates = metadata.getNotificationStates();
        author = metadata.getAuthor();
        durationMs = metadata.getDurationMs();
        isSynced = true;
    }

    /** Sync format-related info (live flags, duration, volume) from MediaItemFormatInfo. */
    public void sync(MediaItemFormatInfo formatInfo) {
        if (formatInfo == null) {
            return;
        }
        
        isLive = formatInfo.isLive();

        if (description == null) {
            description = formatInfo.getDescription();
        }

        // Published/start time is relevant for live streams only.
        if (formatInfo.isLive()) {
            startTimeMs = formatInfo.getStartTimeMs() > 0 ? formatInfo.getStartTimeMs() : DateHelper.toUnixTimeMs(formatInfo.getStartTimestamp());
            startSegmentNum = formatInfo.getStartSegmentNum();
        }

        volume = formatInfo.getVolumeLevel();
        isUnplayable = formatInfo.isUnplayable();
    }

    /** Update like/dislike counts from DislikeData. */
    public void sync(DislikeData dislikeData) {
        if (dislikeData == null) {
            return;
        }

        String likeCountNew = dislikeData.getLikeCount();
        String dislikeCountNew = dislikeData.getDislikeCount();
        if (likeCountNew != null) {
            likeCount = likeCountNew;
        }
        if (dislikeCountNew != null) {
            dislikeCount = dislikeCountNew;
        }
    }

    /** Create a lightweight copy with only display/playback relevant fields. */
    public Video copy() {
        Video video = new Video();
        video.videoId = videoId;
        video.playlistId = playlistId;
        video.playlistIndex = playlistIndex;
        video.channelId = channelId;
        video.title = title;
        video.metadataTitle = metadataTitle;
        video.secondTitle = secondTitle;
        video.metadataSecondTitle = metadataSecondTitle;
        video.percentWatched = percentWatched;
        video.cardImageUrl = cardImageUrl;
        video.fromQueue = fromQueue;
        video.bgImageUrl = bgImageUrl;
        video.isLive = isLive;
        video.isUpcoming = isUpcoming;
        video.nextMediaItem = nextMediaItem;
        video.shuffleMediaItem = shuffleMediaItem;
        video.durationMs = durationMs;

        if (getGroup() != null) {
            video.setGroup(getGroup().copy()); // Needed for proper multi row fragments sync (row id == group id)
        }

        return video;
    }

    /** Helper to find next playable MediaItem from metadata; includes a remote-queue fallback. */
    private MediaItem findNextVideo(MediaItemMetadata metadata) {
        if (metadata == null) {
            return null;
        }

        MediaItem nextVideo = metadata.getNextVideo();

        // BUGFIX: player closed after last video from the remote queue — try suggestions fallback.
        if (nextVideo == null && isRemote) {
            List<MediaGroup> suggestions = metadata.getSuggestions();

            if (suggestions != null && suggestions.size() > 1) {
                List<MediaItem> mediaItems = suggestions.get(1).getMediaItems();
                nextVideo = Helpers.findFirst(mediaItems, item -> item.getVideoId() != null);
            }
        }

        return nextVideo;
    }

    /** Mark video as fully viewed (percentWatched=100 and position to end). */
    public void markFullyViewed() {
        percentWatched = 100;
        startTimeSeconds = (int)(getDurationMs() / 1_000);
    }

    /** Mark video as not viewed. */
    public void markNotViewed() {
        percentWatched = 0;
        startTimeSeconds = 0;
    }

    /**
     * Calculate live duration since startTimeMs. If stream ended, return real duration when available.
     *
     * @return live duration in milliseconds
     */
    public long getLiveDurationMs() {
        if (startTimeMs == 0) {
            return 0;
        }

        // If stream ended while watching, prefer known duration.
        if (!isLive) {
            return durationMs > 0 ? durationMs : liveDurationMs;
        }

        liveDurationMs = System.currentTimeMillis() - startTimeMs;
        return liveDurationMs > 0 ? liveDurationMs : 0;
    }

    public long getDurationMs() {
        return durationMs;
    }

    /** Effective playback position in ms: prefer explicit startTimeSeconds, otherwise percent-based restore. */
    public long getPositionMs() {
        if (startTimeSeconds > 0) {
            return startTimeSeconds * 1_000L;
        }

        return getPositionFromPercentWatched();
    }

    /** Compute position from percentWatched while ignoring immediate opens (small percentages). */
    private long getPositionFromPercentWatched() {
        if (percentWatched <= RESTORE_POSITION_PERCENTS || percentWatched >= 100) {
            return 0;
        }

        long posMs = (long) (durationMs / 100f * percentWatched);
        return posMs > 0 && posMs < durationMs ? posMs : 0;
    }

    /** Convert to service MediaItem wrapper. */
    public MediaItem toMediaItem() {
        return SimpleMediaItem.from(this);
    }

    /** Sync playback state from VideoStateService. */
    public void sync(VideoStateService.State state) {
        if (state != null) {
            percentWatched = state.positionMs / (state.durationMs / 100f);
        }
    }

    /**
     * Determine whether section playlist fallback is enabled for this item.
     * Uses PlayerTweaksData and several heuristics to avoid hidden/invalid playlists.
     */
    public boolean isSectionPlaylistEnabled(Context context) {
        return PlayerTweaksData.instance(context).isSectionPlaylistEnabled() && !belongsToSuggestions()
                && (!checkAllVideosHasPlaylist() || nextMediaItem == null
                       || (!isMix() && !belongsToSamePlaylistGroup()))
                && (!isRemote || remotePlaylistId == null);
    }

    /**
     * Create a human readable playlist title derived from item/group metadata.
     *
     * @return formatted playlist title or null when not applicable
     */
    public String createPlaylistTitle() {
        if (!hasPlaylist()) {
            return null;
        }
        
        boolean isChannelPlaylistItem = getGroupTitle() != null && belongsToSameAuthorGroup() && belongsToSamePlaylistGroup();
        boolean isUserPlaylistItem = getGroupTitle() != null && belongsToSamePlaylistGroup();
        String title = isChannelPlaylistItem ? getAuthor() : isUserPlaylistItem ? null : getTitle();
        String subtitle = isChannelPlaylistItem || isUserPlaylistItem || belongsToUserPlaylists() ? getGroupTitle() : getAuthor();
        return title != null && subtitle != null ? String.format("%s - %s", title, subtitle) : String.format("%s", title != null ? title : subtitle);
    }

    /**
     * Create a channel title suitable for UI display using reloadPageKey/channel info.
     *
     * @return formatted channel title or null when not applicable
     */
    public String createChannelTitle() {
        if (!hasReloadPageKey() && !hasChannel()) {
            return null;
        }
        
        boolean hasChannel = hasChannel() && !isChannel();
        boolean isUserPlaylistItem = getGroupTitle() != null && belongsToSamePlaylistGroup();
        String title = hasChannel ? getAuthor() : isUserPlaylistItem ? null : getTitle();
        String subtitle = isUserPlaylistItem ? getGroupTitle() : hasChannel || isChannel() ? null : getAuthor();
        return title != null && subtitle != null ? String.format("%s - %s", title, subtitle) : String.format("%s", title != null ? title : subtitle);
    }
}
