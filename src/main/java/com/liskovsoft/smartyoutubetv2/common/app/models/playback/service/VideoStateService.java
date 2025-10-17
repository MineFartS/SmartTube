package com.liskovsoft.smartyoutubetv2.common.app.models.playback.service;

/**
 * In-memory LRU-like cache of playback states with debounced persistence to AppPrefs.
 * Keeps serialization compact and handles profile changes.
 *
 * Responsibilities:
 * - Keep a small LRU-like list of State objects describing last-played videos
 *   (position, duration, speed, timestamp).
 * - Persist and restore the list to AppPrefs as a single serialized string.
 * - Throttle persistence to avoid frequent disk writes.
 * - Provide lookup helpers by video id.
 *
 * Note: The service intentionally stores state separately from Video instances
 * because a single video may be represented by multiple Video objects across the app.
 */
public class VideoStateService implements ProfileChangeListener {
    @SuppressLint("StaticFieldLeak")
    private static VideoStateService sInstance;

    // Number of entries to keep depending on available memory.
    private static final int MIN_PERSISTENT_STATE_SIZE = 50;
    private static final int MAX_PERSISTENT_STATE_SIZE = 300;

    // Delay before actually writing state to disk (milliseconds). Batches rapid updates.
    private static final long PERSIST_DELAY_MS = 10_000;

    // In-memory list acting as an LRU-like collection for recent states.
    // We keep a List<State> instead of a Map to preserve simple ordering and ease of serialization.
    private final List<State> mStates;

    // Preferences helper used to persist/restore state string.
    private final AppPrefs mPrefs;

    // Delimiter used for persisted state items at the top-level (between State.toString() instances).
    private static final String DELIM = "&si;";

    // Flag to mark that playback history is broken (used by caller logic).
    private boolean mIsHistoryBroken;

    // Runnable scheduled to perform actual persistence after debounce delay.
    private final Runnable mPersistStateInt = this::persistStateInt;

    private VideoStateService(Context context) {
        mPrefs = AppPrefs.instance(context);
        mPrefs.addListener(this);
        // Create a safe LRU-like list with capacity tuned to device RAM.
        mStates = Helpers.createSafeLRUList(
                Utils.isEnoughRam() ? MAX_PERSISTENT_STATE_SIZE : MIN_PERSISTENT_STATE_SIZE);
        restoreState();
    }

    /**
     * Obtain singleton instance. Requires a Context on first call.
     */
    public static VideoStateService instance(Context context) {
        if (sInstance == null && context != null) {
            sInstance = new VideoStateService(context.getApplicationContext());
        }

        return sInstance;
    }

    /**
     * Returns the backing list of states (mutable).
     * Callers should treat it as read-mostly.
     */
    public List<State> getStates() {
        return mStates;
    }

    /**
     * Get the most recent saved state, or null when no state exists.
     */
    public @Nullable State getLastState() {
        if (isEmpty()) {
            return null;
        }

        return mStates.get(mStates.size() - 1);
    }

    /**
     * Lookup a saved state by video id. Linear scan is acceptable because list size is capped.
     */
    public State getByVideoId(String videoId) {
        for (State state : mStates) {
            if (Helpers.equals(videoId, state.video.videoId)) {
                return state;
            }
        }

        return null;
    }

    /**
     * Remove all saved states corresponding to the provided videoId and persist change.
     */
    public void removeByVideoId(String videoId) {
        Helpers.removeIf(mStates, state -> Helpers.equals(state.video.videoId, videoId));
        persistState();
    }

    public boolean isEmpty() {
        return mStates.isEmpty();
    }

    /**
     * Save a state into the LRU list and schedule persistence. Newer entries go to the end.
     */
    public void save(State state) {
        mStates.add(state);
        persistState();
    }

    /**
     * Clear in-memory states and persist the empty result.
     */
    public void clear() {
        mStates.clear();
        persistState();
    }

    /**
     * Mark that history is broken (used by UI/logic) and will be preserved during persist.
     */
    public void setHistoryBroken(boolean isBroken) {
        mIsHistoryBroken = isBroken;
    }

    public boolean isHistoryBroken() {
        return mIsHistoryBroken;
    }

    /**
     * Restore state from preferences into the in-memory list.
     * Called during construction and when profile changes.
     */
    private void restoreState() {
        mStates.clear();
        String data = mPrefs.getStateUpdaterData();

        String[] split = Helpers.splitData(data);

        setStateData(Helpers.parseStr(split, 0));
        mIsHistoryBroken = Helpers.parseBoolean(split, 1);
    }

    /**
     * Perform the actual write to preferences.
     *
     * If history is broken we include that flag in the persisted string using mergeData helper.
     * Otherwise we write only the state payload.
     */
    private void persistStateInt() {
        if (mIsHistoryBroken) {
            mPrefs.setStateUpdaterData(Helpers.mergeData(getStateData(), mIsHistoryBroken));
        } else {
            // Eliminate additional string creation with the merge
            mPrefs.setStateUpdaterData(getStateData());
        }
    }

    /**
     * Schedule persistence after a short delay to batch rapid updates.
     */
    private void persistState() {
        // Improve memory and disk usage by debouncing writes.
        Utils.postDelayed(mPersistStateInt, PERSIST_DELAY_MS);
    }

    /**
     * Represents a saved playback state for a single video.
     *
     * Contains video reference, current position, duration, playback speed and a timestamp.
     * The class is serializable to a compact String via toString/from(String).
     */
    public static class State {
        private static final String DELIM = "&sf;";
        public final Video video;
        public final long positionMs;
        public final long durationMs;
        public final float speed;
        public final long timestamp = System.currentTimeMillis();

        public State(Video video, long positionMs) {
            this(video, positionMs, -1);
        }

        public State(Video video, long positionMs, long durationMs) {
            this(video, positionMs, durationMs, 1.0f);
        }

        public State(Video video, long positionMs, long durationMs, float speed) {
            this.video = video;
            this.positionMs = positionMs;
            this.durationMs = durationMs;
            this.speed = speed;
        }

        /**
         * Reconstruct a State from its serialized representation.
         * Returns null for invalid input.
         */
        public static State from(String spec) {
            if (spec == null) {
                return null;
            }

            String[] split = Helpers.split(spec, DELIM);

            String videoId = Helpers.parseStr(split, 0);
            long positionMs = Helpers.parseLong(split, 1);
            long lengthMs = Helpers.parseLong(split, 2);
            float speed = Helpers.parseFloat(split, 3);

            Video video = Video.fromString(videoId);

            // backward compatibility: if Video.fromString fails create minimal Video holder
            if (video == null) {
                video = new Video();
                video.videoId = videoId;
            }

            // Rehydrate percentWatched for quick UI display (may be approximate).
            video.percentWatched = (positionMs * 100f) / lengthMs;

            return new State(video, positionMs, lengthMs, speed);
        }

        @NonNull
        @Override
        public String toString() {
            return Helpers.merge(DELIM, video, positionMs, durationMs, speed);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof State) {
                return Helpers.equals(video, ((State) obj).video);
            }

            return false;
        }
    }

    @Override
    public void onProfileChanged() {
        // When app profile changes we must reload persisted states for the new profile.
        restoreState();
    }

    /**
     * Safe wrapper around setStateData to guard against strange ArrayIndexOutOfBoundsException
     * observed on some devices (NVidia Shield reported).
     */
    private void setStateDataSafe(String data) {
        try {
            setStateData(data);
        } catch (ArrayIndexOutOfBoundsException e) { // weird issue (NVidia Shield)
            e.printStackTrace();
        }
    }

    /**
     * Parse the payload string (multiple State specs separated by DELIM) and populate mStates.
     */
    private void setStateData(String data) {
        if (data != null) {
            String[] split = Helpers.split(data, DELIM);

            for (String spec : split) {
                State state = State.from(spec);

                if (state != null) {
                    mStates.add(state);
                }
            }
        }
    }

    /**
     * Serialize current mStates into a single string for persistence.
     * Individual State.toString() values are joined using DELIM.
     */
    private String getStateData() {
        StringBuilder sb = new StringBuilder();

        for (State state : mStates) {
            if (sb.length() != 0) {
                sb.append(DELIM);
            }

            sb.append(state);
        }

        return sb.toString();
    }
}
