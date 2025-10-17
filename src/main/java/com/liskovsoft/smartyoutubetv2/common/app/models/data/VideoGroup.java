package com.liskovsoft.smartyoutubetv2.common.app.models.data;

import android.text.TextUtils;

import com.liskovsoft.mediaserviceinterfaces.data.ChapterItem;
import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItem;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.service.VideoStateService;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.service.VideoStateService.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * Container for a group/row of Video items displayed in the UI.
 *
 * Responsibilities:
 * - Hold metadata about the group (id, title, section, position, action, type).
 * - Manage the underlying list of Video objects and provide utility operations
 *   (add/remove/strip playlist info/lookup).
 * - Convert from MediaGroup/MediaItem/ChapterItem representations to Video model objects.
 *
 * Notes:
 * - Many factory "from" helpers exist to build VideoGroup from different sources.
 * - mVideos may be null until items are added; several methods defensively handle that.
 * - Some methods intentionally return unmodifiable lists (views) because adapters
 *   expect a stable collection and filter it themselves.
 */
public class VideoGroup {
    /**
     * Add at the end of the existing group
     */
    public static final int ACTION_APPEND = 0;
    /**
     * Clear whole fragment and then add this group
     */
    public static final int ACTION_REPLACE = 1;
    public static final int ACTION_REMOVE = 2;
    public static final int ACTION_REMOVE_AUTHOR = 3;
    public static final int ACTION_SYNC = 4;
    /**
     * Add at the begin of the existing group
     */
    public static final int ACTION_PREPEND = 5;
    private static final String TAG = VideoGroup.class.getSimpleName();
    private int mId;
    private String mTitle;
    private List<Video> mVideos;
    private MediaGroup mMediaGroup;
    private BrowseSection mSection;
    private int mPosition = -1;
    private int mAction = ACTION_APPEND;
    private int mType = -1;
    public boolean isQueue;

    // Factory helpers -------------------------------------------------------

    public static VideoGroup from(BrowseSection section) {
        return from((MediaGroup) null, section);
    }

    public static VideoGroup from(MediaGroup mediaGroup) {
        return from(mediaGroup, (BrowseSection) null);
    }

    public static VideoGroup from(BrowseSection section, int groupPosition) {
        return from((MediaGroup) null, section, groupPosition);
    }

    public static VideoGroup from(MediaGroup mediaGroup, BrowseSection section) {
        return from(mediaGroup, section, -1);
    }

    public static VideoGroup from(Video item) {
        return from(item, extractGroupPosition(item));
    }

    public static VideoGroup from(Video item, int groupPosition) {
        return from(new ArrayList<>(Collections.singletonList(item)), null, groupPosition);
    }

    public static VideoGroup from(List<Video> items) {
        return from(items, null);
    }

    public static VideoGroup from(List<Video> items, BrowseSection section) {
        return from(items, section, extractGroupPosition(items));
    }

    /**
     * Main factory that constructs a VideoGroup from an existing list of Video objects.
     * - Extracts group id/title from the topmost item that already contains group info.
     * - Sets groupPosition and attaches the group reference into any Video items without one.
     */
    public static VideoGroup from(List<Video> items, BrowseSection section, int groupPosition) {
        VideoGroup videoGroup = new VideoGroup();
        // Getting topmost element. Could help when syncing multi rows fragments.
        Video topItem = findTopmostItemWithGroup(items);
        if (topItem != null && topItem.getGroup() != null) {
            videoGroup.mId = topItem.getGroup().getId();
            videoGroup.mTitle = topItem.getGroup().getTitle();
        }
        videoGroup.mVideos = items;
        videoGroup.mPosition = groupPosition;
        videoGroup.mSection = section;

        for (Video item : items) {
            // Section as playlist fix. Don't change the root.
            if (item.getGroup() == null) {
                item.setGroup(videoGroup);
            }
        }

        return videoGroup;
    }

    /**
     * Builds VideoGroup from MediaGroup (network/service model).
     * Converts each MediaItem -> Video and appends into this group.
     */
    public static VideoGroup from(MediaGroup mediaGroup, BrowseSection section, int groupPosition) {
        VideoGroup videoGroup = new VideoGroup();
        videoGroup.mSection = section;
        videoGroup.mPosition = groupPosition;
        videoGroup.mVideos = new ArrayList<>();
        videoGroup.mMediaGroup = mediaGroup;
        videoGroup.mTitle = mediaGroup != null && mediaGroup.getTitle() != null ?
                mediaGroup.getTitle() : section != null ? section.getTitle() : null;
        // Fix duplicated rows e.g. Shorts
        //videoGroup.mId = !TextUtils.isEmpty(videoGroup.mTitle) ? videoGroup.mTitle.hashCode() : videoGroup.hashCode();
        videoGroup.mId = videoGroup.hashCode();

        if (mediaGroup == null) {
            return videoGroup;
        }

        if (mediaGroup.getMediaItems() == null) {
            Log.e(TAG, "MediaGroup doesn't contain media items. Title: " + mediaGroup.getTitle());
            return videoGroup;
        }

        for (MediaItem item : mediaGroup.getMediaItems()) {
            Video video = Video.from(item);

            videoGroup.add(video);
        }

        return videoGroup;
    }

    /**
     * Merge mediaGroup into an existing VideoGroup instance.
     * The baseGroup will receive converted MediaItems appended to its list.
     */
    public static VideoGroup from(VideoGroup baseGroup, MediaGroup mediaGroup) {
        baseGroup.mMediaGroup = mediaGroup;

        if (mediaGroup == null) {
            return baseGroup;
        }

        if (mediaGroup.getMediaItems() == null) {
            Log.e(TAG, "MediaGroup doesn't contain media items. Title: " + mediaGroup.getTitle());
            return baseGroup;
        }

        for (MediaItem item : mediaGroup.getMediaItems()) {
            Video video = Video.from(item);

            baseGroup.add(video);
        }

        baseGroup.mAction = ACTION_APPEND;

        return baseGroup;
    }

    public static VideoGroup fromChapters(List<ChapterItem> chapters, String title) {
        VideoGroup videoGroup = new VideoGroup();
        videoGroup.mTitle = title;
        videoGroup.mVideos = new ArrayList<>();

        for (ChapterItem chapter : chapters) {
            Video video = Video.from(chapter);

            videoGroup.add(video);
        }

        return videoGroup;
    }

    // Accessors -------------------------------------------------------------

    public List<Video> getVideos() {
        // NOTE: Don't make the collection read only
        // The collection will be filtered inside VideoGroupObjectAdapter
        return Collections.unmodifiableList(mVideos);
    }

    public String getTitle() {
        return mTitle;
    }

    /**
     * Set the title for this group. Title may be used as a stable id if desired.
     */
    public void setTitle(String title) {
        mTitle = title;

        //if (!TextUtils.isEmpty(title) && (mId == 0 || mId == hashCode())) {
        //    mId = title.hashCode();
        //}
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public MediaGroup getMediaGroup() {
        return mMediaGroup;
    }

    public BrowseSection getSection() {
        return mSection;
    }

    /**
     * Heuristic: checks first up to 8 items to decide if this group is Shorts.
     * All sampled items must have isShorts == true to count as Shorts.
     */
    public boolean isShorts() {
        if (isEmpty()) {
            return false;
        }

        for (int i = 0; i < Math.min(8, mVideos.size()); i++) {
             if (!mVideos.get(i).isShorts)
                 return false;
        }

        return true;
    }

    /**
     * Group position in multi-grid fragments.
     * It isn't used on other types of fragments.
     */
    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public int getAction() {
        return mAction;
    }

    /**
     * When prepending a group its position becomes 0 (top of the fragment).
     */
    public void setAction(int action) {
        mAction = action;

        if (action == ACTION_PREPEND) {
            mPosition = 0;
        }
    }

    public int getType() {
        return mType != -1 ? mType : getMediaGroup() != null ? getMediaGroup().getType() : -1;
    }

    public void setType(int type) {
        mType = type;
    }

    public String getReloadPageKey() {
        return getMediaGroup() != null ? getMediaGroup().getReloadPageKey() : null;
    }

    public String getNextPageKey() {
        return getMediaGroup() != null ? getMediaGroup().getNextPageKey() : null;
    }

    /**
     * Lightweight copy (without nested videos) used by adapters when only metadata is required.
     */
    public VideoGroup copy() {
        VideoGroup videoGroup = new VideoGroup();
        videoGroup.mId = mId;
        videoGroup.mTitle = mTitle;
        videoGroup.mPosition = mPosition;

        return videoGroup;
    }

    // Internal helpers -----------------------------------------------------

    /**
     * Getting topmost element. Could help when syncing multi rows fragments.
     * Traverses from the end backward to find first Video that already carries group info.
     */
    private static Video findTopmostItemWithGroup(List<Video> items) {
        if (items.isEmpty()) {
            return null;
        }

        for (int i = (items.size() - 1); i >= 0; i--) {
            Video video = items.get(i);
            if (video.getGroup() != null) {
                return video;
            }
        }

        return items.get(items.size() - 1); // No group. Fallback to last item then.
    }

    private static int extractGroupPosition(List<Video> items) {
        if (items == null || items.isEmpty()) {
            return -1;
        }

        return extractGroupPosition(findTopmostItemWithGroup(items));
    }

    private static int extractGroupPosition(Video item) {
        int groupPosition = -1;

        if (item != null) {
            groupPosition = item.groupPosition;
        }

        return groupPosition;
    }

    // Mutators & utilities -------------------------------------------------

    public void removeAllBefore(Video video) {
        if (mVideos == null) {
            return;
        }

        removeAllBefore(mVideos.indexOf(video));
    }

    public void removeAllBefore(int index) {
        if (mVideos == null) {
            return;
        }

        if (index <= 0 || index >= mVideos.size()) {
            return;
        }

        // Keep sublist starting at index (inclusive)
        mVideos = mVideos.subList(index, mVideos.size());
    }

    /**
     * Remove playlist id fields from all videos in this group.
     * Useful when displaying the same group in different playlist contexts.
     */
    public void stripPlaylistInfo() {
        if (mVideos == null) {
            return;
        }

        for (Video video : mVideos) {
            video.playlistId = null;
            video.remotePlaylistId = null;
        }
    }

    public Video findVideoById(String videoId) {
        if (mVideos == null) {
            return null;
        }

        Video result = null;

        for (Video video : mVideos) {
            if (Helpers.equals(videoId, video.videoId)) {
                result = video;
                break;
            }
        }

        return result;
    }

    public void clear() {
        if (mVideos == null) {
            return;
        }

        mVideos.clear();
    }

    public boolean contains(Video video) {
        if (mVideos == null) {
            return false;
        }

        return mVideos.contains(video);
    }

    public int getSize() {
        if (mVideos == null) {
            return -1;
        }

        return mVideos.size();
    }

    public int indexOf(Video video) {
        if (mVideos == null) {
            return -1;
        }

        return mVideos.indexOf(video);
    }

    public Video get(int idx) {
        if (mVideos == null) {
            return null;
        }

        return mVideos.get(idx);
    }

    /**
     * Remove a video from the internal list.
     * Catches UnsupportedOperationException and ConcurrentModificationException because
     * adapters may pass read-only or concurrently-modified lists.
     */
    public void remove(Video video) {
        if (mVideos == null) {
            return;
        }

        try {
            // ConcurrentModificationException fix?
            mVideos.remove(video);
        } catch (UnsupportedOperationException | ConcurrentModificationException e) { // read only collection
            e.printStackTrace();
        }
    }

    /**
     * Safe check for emptiness; may catch ConcurrentModificationException from adapter mutations.
     */
    public boolean isEmpty() {
        try {
            return mVideos == null || mVideos.isEmpty();
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Append a Video to the end of the group.
     * Contains a defensive duplicate-check to avoid UI duplication bugs.
     */
    public void add(Video video) {
        // TODO: remove the hack someday.
        // Dirty hack for avoiding group duplication.
        // Duplicated items suddenly appeared in Home, Subscriptions and History.
        // See: VideoGroupObjectAdapter.mVideoItems
        if (mVideos != null && mVideos.contains(video)) {
            return;
        }

        int size = getSize();
        add(size != -1 ? size : 0, video);
    }

    /**
     * Insert video at the provided index.
     *
     * Responsibilities:
     * - Initialize mVideos if null.
     * - Set video.groupPosition and attach back-reference to this group.
     * - Try to sync playback state (percentWatched) from VideoStateService when available.
     */
    public void add(int idx, Video video) {
        if (video == null || video.isEmpty()) {
            return;
        }

        if (mVideos == null) {
            mVideos = new ArrayList<>();
        }

        // Group position in multi-grid fragments
        video.groupPosition = mPosition;
        video.setGroup(this);

        // Try to hydrate watched percent/state from the global state service.
        VideoStateService stateService = VideoStateService.instance(null);
        if (stateService != null && (video.percentWatched == -1 || video.percentWatched == 100)) {
            State state = stateService.getByVideoId(video.videoId);
            video.sync(state);
        }

        mVideos.add(idx, video);
    }
}
