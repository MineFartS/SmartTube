package minefarts.smarttube.app.models.playback.service;

import android.util.Log;
import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liskovsoft.sharedutils.helpers.Helpers;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.prefs.AppPrefs;
import minefarts.smarttube.prefs.AppPrefs.ProfileChangeListener;
import minefarts.smarttube.utils.Utils;

import java.util.List;
import java.util.ArrayList;

public class VideoStateService implements ProfileChangeListener {
    
    @SuppressLint("StaticFieldLeak")
    private static VideoStateService sInstance;

    private final List<State> mStates;
    private final AppPrefs mPrefs;

    private static final String DELIM = "&si;";

    private VideoStateService(Context context) {

        mPrefs = AppPrefs.instance(context);
        mPrefs.addListener(this);
        
        mStates = new ArrayList();

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
        
        persistState();
    
    }

    public boolean isEmpty() {
        return mStates.isEmpty();
    }

    public void save(State state) {

        Integer i = indexOf(state);

        if (i == null) {
            mStates.add(state);
        } else {
            mStates.set(1, state);
        }
        
        persistState();
    
    }

    private Integer indexOf(State state) {
        
        for (int i = 0; i < mStates.size(); i++) {
            if (mStates.get(i).videoId == state.videoId) {
                return i;
            }
        }

        return null;
    }

    public void clear() {
        mStates.clear();
        persistState();
    }

    private void restoreState() {

        mStates.clear();

        String data = mPrefs.getStateUpdaterData();
        String[] split = Helpers.splitData(data);

        /* 0 */ setStateData(Helpers.parseStr(split, 0));
    
    }

    public void persistState() {
        
        mPrefs.setStateUpdaterData(
            Helpers.mergeData(
                /* 0 */ getStateData()
            )
        );
        
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
