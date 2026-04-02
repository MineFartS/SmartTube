package smartyoutubetv1.app.models.playback.service;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liskovsoft.sharedutils.helpers.Helpers;
import smartyoutubetv1.app.models.data.Video;
import smartyoutubetv1.prefs.AppPrefs;
import smartyoutubetv1.prefs.AppPrefs.ProfileChangeListener;
import smartyoutubetv1.utils.Utils;
import com.liskovsoft.sharedutils.rx.RxHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import android.util.Log;

public class VideoStateService implements ProfileChangeListener {
    
    @SuppressLint("StaticFieldLeak")
    private static VideoStateService sInstance;

    private final List<State> mStates;
    private final AppPrefs mPrefs;

    private static final String DELIM = "&si;";

    private VideoStateService(Context context) {

        mPrefs = AppPrefs.instance(context);
        mPrefs.addListener(this);
        
        mStates = Helpers.createSafeLRUList(200);

        restoreState();

    }

    public static VideoStateService instance(Context context) {
        if (sInstance == null && context != null) {
            sInstance = new VideoStateService(context.getApplicationContext());
        }

        return sInstance;
    }

    public List<State> getStates() {
        return mStates;
    }

    public @Nullable State getLastState() {
        if (isEmpty()) {
            return null;
        }

        return mStates.get(mStates.size() - 1);
    }

    public State getByVideoId(String videoId) {
        for (State state : mStates) {
            if (Helpers.equals(videoId, state.video.videoId)) {
                return state;
            }
        }

        return null;
    }

    public void removeByVideoId(String videoId) {
        Helpers.removeIf(
            mStates, 
            state -> Helpers.equals(state.video.videoId, videoId)
        );
        Log.d(TAG, "removeByVideoId: Removing state for videoId=" + videoId + ", states left: " + mStates.size());
        persistState();
    }

    public boolean isEmpty() {
        return mStates.isEmpty();
    }

    public void save(State state) {
        if (mStates.contains(state)) {
            Log.d(TAG, "save: State already exists for videoId=" + state.video.videoId + ", skipping");
            return;
        }
        mStates.add(state);
        Log.d(TAG, "save: Added state for videoId=" + state.video.videoId + ", total states: " + mStates.size());
        persistState();
    }

    public void clear() {
        Log.d(TAG, "clear: Clearing all states");
        mStates.clear();
        persistState();
    }

    private void restoreState() {
        Log.d(TAG, "restoreState: Starting async restore");
        Observable.fromCallable(() -> {
            mStates.clear();
            String data = mPrefs.getStateUpdaterData();
            String[] split = Helpers.splitData(data);
            setStateData(Helpers.parseStr(split, 0));
            Log.d(TAG, "restoreState: Restored " + mStates.size() + " states");
            return mStates.size();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
          .subscribe(count -> Log.d(TAG, "restoreState: Success, " + count + " states"),
                     error -> Log.e(TAG, "restoreState failed", error));
    }

    private static final String TAG = "VideoStateService";

    public void persistState() {
        Log.d(TAG, "persistState: Persisting " + mStates.size() + " states async");
        Observable.fromCallable(() -> {
            String data = Helpers.mergeData(/* 0 */ getStateData());
            mPrefs.setStateUpdaterData(data);
            return data;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
          .retry(3)
          .subscribe(data -> Log.d(TAG, "persistState: Success"),
                     error -> Log.e(TAG, "persistState failed after retries", error));
    }

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

            // backward compatibility
            if (video == null) {
                video = new Video();
                video.videoId = videoId;
            }

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
        restoreState();
    }

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
