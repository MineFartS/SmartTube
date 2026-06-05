package minefarts.smarttube.utils.service.data;

import minefarts.smarttube.utils.data.ChapterItem;
import minefarts.smarttube.utils.service.data.MediaGroup;
import minefarts.smarttube.utils.data.MediaItem;
import minefarts.smarttube.utils.data.NotificationState;
import minefarts.smarttube.utils.data.PlaylistInfo;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.browse.models.sections.Chip;
import minefarts.smarttube.utils.common.models.items.VideoItem;
import minefarts.smarttube.utils.next.v1.models.ButtonStates;
import minefarts.smarttube.utils.next.v1.models.SuggestedSection;
import minefarts.smarttube.utils.next.v1.models.VideoMetadata;
import minefarts.smarttube.utils.next.v1.models.VideoOwner;
import minefarts.smarttube.utils.next.v1.result.WatchNextResult;
import minefarts.smarttube.google.common.helpers.YouTubeHelper;

import java.util.ArrayList;
import java.util.List;

public class MediaItemMetadata {

    private static final String TAG = MediaItemMetadata.class.getSimpleName();

    public static final int LIKE_STATUS_INDIFFERENT = 0;
    public static final int LIKE_STATUS_LIKE = 1;
    public static final int LIKE_STATUS_DISLIKE = 2;
    
    private String mTitle;
    private CharSequence mSecondTitle;
    private CharSequence mSecondTitleAlt;
    private String mDescription;
    private String mAuthor;
    private String mAuthorImageUrl;
    private String mViewCount;
    private String mLikesCount;
    private String mDislikesCount;
    private String mPublishedDate;
    private boolean mIsSubscribed;
    private int mLikeStatus;
    private String mVideoId;
    private String mChannelId;
    private String mParams;
    private int mPercentWatched;
    private MediaItem mNextVideo;
    private List<MediaGroup> mSuggestions;
    private boolean mIsLive;
    private boolean mIsUpcoming;
    private PlaylistInfo mPlaylistInfo;

    public static MediaItemMetadata from(WatchNextResult watchNextResult) {
    
        if (watchNextResult == null) {
            return null;
        }

        MediaItemMetadata mediaItemMetadata = new MediaItemMetadata();

        VideoMetadata videoMetadata = watchNextResult.getVideoMetadata();
        VideoOwner videoOwner = watchNextResult.getVideoOwner();
        VideoItem videoDetails = watchNextResult.getVideoDetails();

        if (videoMetadata == null && videoOwner == null && videoDetails == null) {
            Log.e(TAG, "Oops. Next format has been changed. Please upgrade parser.");
        }

        if (videoDetails != null) {
    
            mediaItemMetadata.mAuthor = Helpers.toString(videoDetails.getUserName());
            mediaItemMetadata.mChannelId = videoDetails.getChannelId();
            mediaItemMetadata.mTitle = videoDetails.getTitle();
            mediaItemMetadata.mVideoId = videoDetails.getVideoId();
            mediaItemMetadata.mPublishedDate = videoDetails.getPublishedDate();

            mediaItemMetadata.mSecondTitle = YouTubeHelper.createInfo(
                mediaItemMetadata.mAuthor,
                videoDetails.getPublishedDate(),
                videoDetails.getViewCountText()
            );

            mediaItemMetadata.mSecondTitleAlt = YouTubeHelper.createInfo(
                mediaItemMetadata.mAuthor,
                videoDetails.getPublishedDate(),
                videoDetails.getShortViewCountText()
            );

        }

        if (videoOwner != null) {
            
            mediaItemMetadata.mAuthor = videoOwner.getVideoAuthor();
            mediaItemMetadata.mAuthorImageUrl = YouTubeHelper.findOptimalResThumbnailUrl(videoOwner.getThumbnails());
            mediaItemMetadata.mChannelId = videoOwner.getChannelId();
            
            Boolean subscribed = videoOwner.isSubscribed();
            mediaItemMetadata.mIsSubscribed = subscribed != null && subscribed;
        
        }

        if (videoMetadata != null) {
            
            String author = mediaItemMetadata.mAuthor != null ? mediaItemMetadata.mAuthor : videoMetadata.getByLine();
            String publishedTime = videoMetadata.getPublishedTime() != null ? videoMetadata.getPublishedTime() : videoMetadata.getAlbumName();
            
            mediaItemMetadata.mTitle = videoMetadata.getTitle();
            
            mediaItemMetadata.mSecondTitle = YouTubeHelper.createInfo(
                author, 
                publishedTime,
                videoMetadata.getShortViewCount()
            );

            mediaItemMetadata.mSecondTitleAlt = YouTubeHelper.createInfo(
                author,
                videoMetadata.getPublishedDate(),
                videoMetadata.getShortViewCount()
            );

            mediaItemMetadata.mVideoId = videoMetadata.getVideoId();
            mediaItemMetadata.mDescription = videoMetadata.getDescription();
            mediaItemMetadata.mDislikesCount = videoMetadata.getDislikesCount();
            mediaItemMetadata.mLikesCount = videoMetadata.getLikesCount();
            mediaItemMetadata.mViewCount = Helpers.toString(videoMetadata.getViewCount());
            mediaItemMetadata.mPercentWatched = videoMetadata.getPercentWatched();
            mediaItemMetadata.mPublishedDate = videoMetadata.getPublishedDate();
            mediaItemMetadata.mIsLive = videoMetadata.isLive();
            mediaItemMetadata.mIsUpcoming = videoMetadata.isUpcoming();

            String likeStatus = videoMetadata.getLikeStatus();

            if (likeStatus != null) {
                
                switch (likeStatus) {
                
                    case VideoMetadata.LIKE_STATUS_LIKE:
                        mediaItemMetadata.mLikeStatus = MediaItemMetadata.LIKE_STATUS_LIKE;
                        break;
                
                    case VideoMetadata.LIKE_STATUS_DISLIKE:
                        mediaItemMetadata.mLikeStatus = MediaItemMetadata.LIKE_STATUS_DISLIKE;
                        break;

                }
            }
        }

        mediaItemMetadata.mNextVideo = YouTubeMediaItem.from(watchNextResult.getNextVideo());

        List<SuggestedSection> suggestedSections = watchNextResult.getSuggestedSections();

        if (suggestedSections != null) {

            mediaItemMetadata.mSuggestions = new ArrayList<>();

            for (SuggestedSection section : suggestedSections) {

                if (section.getChips() != null) {
                    // Contains multiple nested sections
                    for (Chip chip : section.getChips()) {
                        mediaItemMetadata.mSuggestions.add(MediaGroup.from(chip));
                    }
                }

                mediaItemMetadata.mSuggestions.add(MediaGroup.from(section));

            }
        }

        ButtonStates buttonStates = watchNextResult.getButtonStates();

        // Alt path to get like/subscribe status (when no such info in metadata section, e.g. YouTube Music items)
        if (buttonStates != null) {

            if (buttonStates.isSubscribeToggled() != null) {
                mediaItemMetadata.mIsSubscribed = buttonStates.isSubscribeToggled();
            }

            if (buttonStates.isLikeToggled() != null && buttonStates.isLikeToggled()) {
                mediaItemMetadata.mLikeStatus = MediaItemMetadata.LIKE_STATUS_LIKE;
            }

            if (buttonStates.isDislikeToggled() != null && buttonStates.isDislikeToggled()) {
                mediaItemMetadata.mLikeStatus = MediaItemMetadata.LIKE_STATUS_DISLIKE;
            }

            if (buttonStates.getChannelId() != null) {
                mediaItemMetadata.mChannelId = buttonStates.getChannelId();
            }

        }

        return mediaItemMetadata;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public CharSequence getSecondTitle() {
        return mSecondTitle;
    }

    public void setSecondTitle(CharSequence secondTitle) {
        mSecondTitle = secondTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getAuthorImageUrl() {
        return mAuthorImageUrl;
    }

    public String getViewCount() {
        return mViewCount;
    }

    public String getLikeCount() {
        return mLikesCount;
    }

    public String getDislikeCount() {
        return mDislikesCount;
    }

    public String getSubscriberCount() {
        return null;
    }

    public String getPublishedDate() {
        return mPublishedDate;
    }

    public String getVideoId() {
        return mVideoId;
    }

    public void setVideoId(String videoId) {
        mVideoId = videoId;
    }

    public MediaItem getNextVideo() {
        return mNextVideo;
    }

    public void setNextVideo(MediaItem nextVideo) {
        mNextVideo = nextVideo;
    }

    public MediaItem getShuffleVideo() {
        return null;
    }

    public boolean isSubscribed() {
        return mIsSubscribed;
    }

    public boolean isLive() {
        return mIsLive;
    }

    public String getLiveChatKey() {
        return null;
    }

    public String getCommentsKey() {
        return null;
    }

    public boolean isUpcoming() {
        return mIsUpcoming;
    }

    public String getChannelId() {
        return mChannelId;
    }

    public String getParams() {
        return mParams;
    }

    public int getPercentWatched() {
        return mPercentWatched;
    }

    public int getLikeStatus() {
        return mLikeStatus;
    }

    public List<MediaGroup> getSuggestions() {
        return mSuggestions;
    }

    public void setSuggestions(List<MediaGroup> suggestions) {
        mSuggestions = suggestions;
    }

    public PlaylistInfo getPlaylistInfo() {
        return mPlaylistInfo;
    }

    public void setPlaylistInfo(PlaylistInfo playlistInfo) {
        mPlaylistInfo = playlistInfo;
    }

    public List<ChapterItem> getChapters() {
        return null;
    }

    public List<NotificationState> getNotificationStates() {
        return null;
    }

    public long getDurationMs() {
        return -1;
    }

    public String getBadgeText() {
        return null;
    }
    
}
