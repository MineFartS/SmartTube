package com.liskovsoft.smartyoutubetv2.common.app.models.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages an in-memory playlist of Video objects.
 *
 * Responsibilities:
 * - Maintain the ordered list of videos.
 * - Track the current playing index.
 * - Provide helpers to add/remove/trim items and to sync externally-updated items.
 *
 * This class is a simple singleton to be used across the app.
 */
public class Playlist {
    // Maximum number of items to keep in the playlist. Older items are dropped.
    private static final int PLAYLIST_MAX_SIZE = 50;

    // The backing list holding playlist items in play order.
    private final List<Video> mPlaylist;

    // Items that were synced from external sources in the current session.
    private final List<Video> mSyncedItems;

    // Index of the currently playing item in mPlaylist, or -1 when no current item.
    private int mCurrentIndex;

    // Singleton instance.
    private static Playlist sInstance;

    private Playlist() {
        mPlaylist = new ArrayList<>();
        mSyncedItems = new ArrayList<>();
        mCurrentIndex = -1;
    }

    /**
     * Obtain the singleton instance.
     */
    public static Playlist instance() {
        if (sInstance == null) {
            sInstance = new Playlist();
        }

        return sInstance;
    }

    /**
     * Clears the playlist and resets current position.
     */
    public void clear() {
        mPlaylist.clear();
        mCurrentIndex = -1;
    }

    /**
     * Only clears the current position without removing items.
     * Useful when the playback session ends but the list should be preserved.
     */
    public void clearPosition() {
        mCurrentIndex = -1;
    }

    /**
     * Replace any existing matching items and append the provided list.
     * Used to sync list with remotely added items.
     *
     * @param videos list of videos to append
     */
    public void addAll(List<Video> videos) {
        // Remove any duplicates first to maintain order from the provided list.
        mPlaylist.removeAll(videos);
        mPlaylist.addAll(videos);
    }

    /**
     * Adds a video to the end of the playlist.
     *
     * If the video equals the currently playing item, we replace it to keep position.
     * If the video equals the current last element, we treat it as a replace and advance index.
     *
     * Also trims the playlist to PLAYLIST_MAX_SIZE and clears references on the previous item
     * to reduce memory footprint.
     *
     * @param video to be added
     */
    public void add(Video video) {
        if (Video.isEmpty(video)) {
            return;
        }

        Video current = getCurrent();

        // If the incoming item equals the current one, replace the object but keep the index.
        if (video.equals(current)) {
            replace(current, video);
            return;
        }

        // Check whether the video was the last item before removal; if so, we will increment index
        // to preserve the relative "last element" behavior.
        boolean isLastElement = !mPlaylist.isEmpty() && video.equals(mPlaylist.get(mPlaylist.size() - 1));

        // Remove any existing copy of this video (do nothing if not present).
        remove(video);

        // Append to the end.
        mPlaylist.add(video);

        // If we replaced the last element, advance the current index to point to the same logical item.
        if (isLastElement) {
            mCurrentIndex++;
        }

        // Enforce max size and release heavy references from previous item to avoid OOM.
        trimPlaylist();
        stripPrevItem();
    }

    /**
     * Insert a video directly after the current position (or at the end if no current).
     *
     * @param video video to insert as next
     */
    public void next(Video video) {
        if (Video.isEmpty(video)) {
            return;
        }

        // Remove existing copies first.
        remove(video);

        // Compute insert position: normally currentIndex + 1, but if current index is invalid,
        // append at the end.
        int nextIdx = mPlaylist.size() > mCurrentIndex ? mCurrentIndex + 1 : mPlaylist.size() - 1;

        // Defensive check: if index calculation yields negative (no items), abort.
        if (nextIdx < 0) {
            return;
        }

        mPlaylist.add(nextIdx, video);

        // Trim and cleanup references as above.
        trimPlaylist();
        stripPrevItem();
    }

    /**
     * Removes a video from the playlist (unless it is the currently playing item).
     * Adjusts mCurrentIndex to keep it pointing to the same logical item.
     *
     * @param video item to remove
     */
    public void remove(Video video) {
        if (Video.isEmpty(video)) {
            return;
        }

        // Never remove the currently playing item here; that should be handled separately.
        if (video.equals(getCurrent())) {
            return;
        }

        int index = mPlaylist.indexOf(video);

        if (index >= 0) {
            mPlaylist.remove(video);

            // If the removed element was before current, shift current index left by one.
            if (index < mCurrentIndex) {
                mCurrentIndex--;
            }

            // If current index is now past the end (removed last element), set it to last.
            if (mCurrentIndex >= mPlaylist.size()) {
                mCurrentIndex = mPlaylist.size() - 1;
            }
        }
    }

    public boolean contains(Video video) {
        if (Video.isEmpty(video)) {
            return false;
        }

        return mPlaylist.contains(video);
    }

    public boolean containsAfterCurrent(Video video) {
        if (Video.isEmpty(video)) {
            return false;
        }

        List<Video> afterCurrent = getAllAfterCurrent();
        return afterCurrent != null && afterCurrent.contains(video);
    }

    // Previously an alternative trimming strategy existed and was commented out.
    // The current implementation keeps the most-recent PLAYLIST_MAX_SIZE items.

    /**
     * Returns the next video without changing position, or null if none.
     */
    public Video getNext() {
        if (mCurrentIndex >= 0 && (mCurrentIndex + 1) < mPlaylist.size()) {
            return mPlaylist.get(mCurrentIndex + 1);
        }

        return null;
    }

    /**
     * Returns the previous video without changing position, or null if none.
     */
    public Video getPrevious() {
        if ((mCurrentIndex - 1) >= 0) {
            return mPlaylist.get(mCurrentIndex - 1);
        }

        return null;
    }

    /**
     * Set the provided video as the current one. If it is already present, moves the index to it.
     * Otherwise, the video is appended and becomes the current.
     *
     * @param video item to set current
     */
    public void setCurrent(Video video) {
        if (Video.isEmpty(video)) {
            return;
        }

        int currentPosition = mPlaylist.indexOf(video);

        if (currentPosition >= 0) {
            mCurrentIndex = currentPosition;
        } else {
            add(video);
            mCurrentIndex = mPlaylist.size() - 1;
        }
    }

    /**
     * Return the current video, or null if none is selected.
     */
    public Video getCurrent() {
        if (mCurrentIndex < mPlaylist.size() && mCurrentIndex >= 0) {
            return mPlaylist.get(mCurrentIndex);
        }

        return null;
    }

    /**
     * Read-only access to the playlist.
     */
    public List<Video> getAll() {
        return Collections.unmodifiableList(mPlaylist);
    }

    /**
     * Read-only access to synced items (items that were updated from remote).
     */
    public List<Video> getChangedItems() {
        return Collections.unmodifiableList(mSyncedItems);
    }

    public boolean hasNext() {
        return getNext() != null;
    }

    /**
     * Returns a view (subList) of all items after the current one.
     * If no current is selected (mCurrentIndex == -1) returns the whole list.
     *
     * Note: the returned list is a view into the backing list (not a copy).
     */
    public List<Video> getAllAfterCurrent() {
        if (mCurrentIndex == -1) {
            return mPlaylist;
        }

        int fromIndex = mCurrentIndex + 1;
        if (fromIndex > 0 && fromIndex < mPlaylist.size()) {
            return mPlaylist.subList(fromIndex, mPlaylist.size());
        }

        return null;
    }

    /**
     * Remove all items after the current one. Does nothing if current is not set.
     */
    public void removeAllAfterCurrent() {
        if (mCurrentIndex == -1) {
            return;
        }

        int fromIndex = mCurrentIndex + 1;
        int size = mPlaylist.size();
        if (fromIndex > 0 && fromIndex < size) {
            // Clear the tail portion in-place.
            mPlaylist.subList(fromIndex, size).clear();
        }
    }

    /**
     * Trim playlist if it exceeds PLAYLIST_MAX_SIZE.
     *
     * Keeps the last PLAYLIST_MAX_SIZE entries (most recent ones). Adjusts mCurrentIndex
     * to keep it pointing to the same logical item after trimming.
     */
    private void trimPlaylist() {
        int size = mPlaylist.size();
        boolean playlistTooBig = size > PLAYLIST_MAX_SIZE;

        if (playlistTooBig) {
            // Number of items to remove from the beginning.
            int toIndex = size - PLAYLIST_MAX_SIZE;
            // Remove head items in-place.
            mPlaylist.subList(0, toIndex).clear();
            // Shift index left by the number of removed items.
            mCurrentIndex -= toIndex;
        }
    }

    /**
     * Release references held by the item immediately preceding current to reduce memory usage.
     *
     * Many Video objects can hold heavy MediaItem references; clearing those for already-played
     * items helps avoid OOM on constrained devices.
     */
    private void stripPrevItem() {
        if (mCurrentIndex == -1) {
            return;
        }

        int prevPosition = mCurrentIndex - 1;

        if (prevPosition < mPlaylist.size() && prevPosition >= 0) {
            Video prevItem = mPlaylist.get(prevPosition);
            if (prevItem != null) {
                prevItem.mediaItem = null;
                prevItem.nextMediaItem = null;
                prevItem.shuffleMediaItem = null;
            }
        }
    }

    /**
     * Replace an existing item instance with a new one while preserving its position.
     *
     * @param origin   existing item to replace
     * @param newItem  new item instance
     */
    private void replace(Video origin, Video newItem) {
        int index = mPlaylist.indexOf(origin);

        if (index != -1) {
            mPlaylist.set(index, newItem);
        }
    }

    /**
     * Reset synced items list for a new session.
     */
    public void onNewSession() {
        mSyncedItems.clear();
    }

    /**
     * Sync an externally-updated Video instance with the corresponding item in the playlist.
     *
     * Because items may be clones (to save memory), we first find the matching item in the
     * current playlist and call its sync method. We also track the origin in mSyncedItems.
     *
     * @param origin updated video instance
     */
    public void sync(Video origin) {
        if (origin == null) {
            return;
        }

        // Sync to maintain order. Item in playlist may be a different instance but equals() matches.
        for (Video video : mPlaylist) {
            if (video.equals(origin)) {
                video.sync(origin);
                break;
            }
        }

        // Keep track of synced items; remove first to update ordering.
        mSyncedItems.remove(origin);
        mSyncedItems.add(origin);
    }
}