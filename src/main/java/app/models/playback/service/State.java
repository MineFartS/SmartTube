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

public class State {

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

video.percentWatched = Math.min(100f, (positionMs * 100f) / lengthMs);

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