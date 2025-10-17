package com.liskovsoft.smartyoutubetv2.common.app.models.data;

import com.liskovsoft.mediaserviceinterfaces.data.MediaItem;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;

/**
 * Simple concrete implementation of MediaItem used throughout the app.
 * Acts as a lightweight container for media-related metadata extracted from
 * various sources (e.g. Video objects or MediaItemMetadata).
 *
 * Instances are created via the static factory methods and expose data via
 * the MediaItem interface.
 */
public final class SimpleMediaItem implements MediaItem {
    // Unique integer id for local usage (may be 0 if not set)
    private int mId;
    // YouTube (or service) video id
    private String mVideoId;
    // Playlist identifier if this item belongs to a playlist
    private String mPlaylistId;
    // Primary visible title of the item
    private String mTitle;
    // Key used to reload page containing this item (if applicable)
    private String mReloadPageKey;
    // Channel/author id
    private String mChannelId;
    // URL for card image (thumbnail)
    private String mCardImageUrl;
    // Optional params used for playback/navigation
    private String mParams;
    // Secondary title (e.g. channel name or subtitle)
    private CharSequence mSecondTitle;
    // Content type/category (movies, shows, etc.)
    private String mContentType;
    // Item type (app-specific)
    private int mType;
    // Background image URL (larger artwork)
    private String mBackgroundImageUrl;
    // Author display name
    private String mAuthor;
    // Percentage watched (0-100)
    private int mPercentWatched;
    // Resume start time (seconds)
    private int mStartTimeSeconds;
    // Badge text (e.g. "LIVE", "NEW")
    private String mBadgeText;
    // True when the source indicates new content is available
    private boolean mHaseNewContent;
    // Video preview URL (short preview clip)
    private String mVideoPreviewUrl;
    // Index inside playlist if applicable
    private int mPlaylistIndex;
    // Live/upcoming/movie flags
    private boolean mIsLive;
    private boolean mIsUpcoming;
    private boolean mIsMovie;
    // Click tracking parameters used for telemetry/navigation
    private String mClickTrackingParams;

    // Private constructor — use factory methods to create instances
    private SimpleMediaItem() {
    }

    /**
     * Create a SimpleMediaItem from a MediaItemMetadata object.
     * Only a subset of fields is populated from metadata.
     *
     * @param metadata source metadata
     * @return MediaItem populated from metadata
     */
    public static MediaItem from(MediaItemMetadata metadata) {
        SimpleMediaItem mediaItem = new SimpleMediaItem();

        // Populate commonly used fields from metadata
        mediaItem.mTitle = metadata.getTitle();
        mediaItem.mSecondTitle = metadata.getSecondTitle();
        mediaItem.mVideoId = metadata.getVideoId();
        mediaItem.mPlaylistId = metadata.getPlaylistInfo() != null ?
                metadata.getPlaylistInfo().getPlaylistId() : metadata.getNextVideo() != null ?
                metadata.getNextVideo().getPlaylistId() : null;
        mediaItem.mParams = metadata.getParams();
        mediaItem.mChannelId = metadata.getChannelId();

        return mediaItem;
    }

    /**
     * Create a SimpleMediaItem from a Video object.
     * This method copies many more fields available on Video into the media item.
     *
     * @param video source Video instance
     * @return MediaItem populated from video
     */
    public static MediaItem from(Video video) {
        SimpleMediaItem mediaItem = new SimpleMediaItem();

        mediaItem.mId = video.id;
        mediaItem.mTitle = video.getTitle();
        mediaItem.mSecondTitle = video.getSecondTitle();
        mediaItem.mContentType = video.category;
        mediaItem.mType = video.itemType;
        mediaItem.mVideoId = video.videoId;
        mediaItem.mChannelId = video.channelId;
        mediaItem.mBackgroundImageUrl = video.bgImageUrl;
        mediaItem.mCardImageUrl = video.cardImageUrl;
        mediaItem.mAuthor = video.author;
        mediaItem.mPercentWatched = (int) video.percentWatched;
        mediaItem.mStartTimeSeconds = video.startTimeSeconds;
        mediaItem.mBadgeText = video.badge;
        mediaItem.mHaseNewContent = video.hasNewContent;
        mediaItem.mVideoPreviewUrl = video.previewUrl;
        mediaItem.mPlaylistId = video.playlistId;
        mediaItem.mPlaylistIndex = video.playlistIndex;
        mediaItem.mParams = video.playlistParams;
        mediaItem.mReloadPageKey = video.reloadPageKey;
        mediaItem.mIsLive = video.isLive;
        mediaItem.mIsUpcoming = video.isUpcoming;
        mediaItem.mIsMovie = video.isMovie;
        mediaItem.mClickTrackingParams = video.clickTrackingParams;

        return mediaItem;
    }

    @Override
    public int getType() {
        return mType;
    }

    @Override
    public boolean isLive() {
        return mIsLive;
    }

    @Override
    public boolean isUpcoming() {
        return mIsUpcoming;
    }

    @Override
    public boolean isShorts() {
        // Shorts are not represented in this simple model
        return false;
    }

    @Override
    public boolean isMovie() {
        return mIsMovie;
    }

    @Override
    public int getPercentWatched() {
        return mPercentWatched;
    }

    @Override
    public int getStartTimeSeconds() {
        return mStartTimeSeconds;
    }

    @Override
    public String getAuthor() {
        return mAuthor;
    }

    @Override
    public String getFeedbackToken() {
        // Not provided by this implementation
        return null;
    }

    @Override
    public String getFeedbackToken2() {
        // Not provided by this implementation
        return null;
    }

    @Override
    public String getPlaylistId() {
        return mPlaylistId;
    }

    @Override
    public int getPlaylistIndex() {
        return mPlaylistIndex;
    }

    @Override
    public String getParams() {
        return mParams;
    }

    @Override
    public String getReloadPageKey() {
        return mReloadPageKey;
    }

    @Override
    public boolean hasNewContent() {
        return mHaseNewContent;
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public CharSequence getSecondTitle() {
        return mSecondTitle;
    }

    @Override
    public String getVideoId() {
        return mVideoId;
    }

    @Override
    public String getContentType() {
        return mContentType;
    }

    @Override
    public long getDurationMs() {
        // Duration not tracked here
        return 0;
    }

    @Override
    public String getBadgeText() {
        return mBadgeText;
    }

    @Override
    public String getProductionDate() {
        return null;
    }

    @Override
    public long getPublishedDate() {
        return 0;
    }

    @Override
    public String getCardImageUrl() {
        return mCardImageUrl;
    }

    @Override
    public String getBackgroundImageUrl() {
        return mBackgroundImageUrl;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public String getChannelId() {
        return mChannelId;
    }

    @Override
    public String getVideoPreviewUrl() {
        return mVideoPreviewUrl;
    }

    @Override
    public String getAudioChannelConfig() {
        return null;
    }

    @Override
    public String getPurchasePrice() {
        return null;
    }

    @Override
    public String getRentalPrice() {
        return null;
    }

    @Override
    public int getRatingStyle() {
        return 0;
    }

    @Override
    public double getRatingScore() {
        return 0;
    }

    @Override
    public boolean hasUploads() {
        return false;
    }

    @Override
    public String getClickTrackingParams() {
        return mClickTrackingParams;
    }

    @Override
    public String getSearchQuery() {
        return null;
    }
}
