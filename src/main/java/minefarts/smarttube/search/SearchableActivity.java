package minefarts.smarttube.search;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;

import minefarts.smarttube.google.common.helpers.ServiceHelper;
import minefarts.smarttube.utils.AppUtil;
import minefarts.smarttube.utils.data.MediaItem;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.mylogger.Log;

import static androidx.core.content.IntentCompat.EXTRA_START_PLAYBACK;

public class SearchableActivity extends Activity {
    private static final String TAG = "SearchableActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Search data " + getIntent().getData());

        if (getIntent() != null && getIntent().getData() != null) {
            Uri uri = getIntent().getData();

            if (uri.getLastPathSegment() != null) {
                int id = Helpers.parseInt(uri.getLastPathSegment());

                boolean startPlayback = getIntent().getBooleanExtra(EXTRA_START_PLAYBACK, false);
                Log.d(TAG, "Should start playback? " + (startPlayback ? "yes" : "no"));

                String url = obtainVideoOrChannelUrl(id);

                if (url != null) {
                    Intent intent = AppUtil.getInstance(this).createAppIntent(url);
                    Log.d(TAG, "Starting search intent: " + intent);

                    startActivity(intent);
                } else {
                    Log.e(TAG, "Cannot find video id inside the url: %s", uri);
                }
            }
        }

        finish();
    }

    private String obtainVideoOrChannelUrl(int id) {
        MediaItem video = VideoContentProvider.findVideoWithId(id);

        if (video != null) {
            return video.getVideoId() != null 
            ? "https://www.youtube.com/watch?v="+video.getVideoId() 
            : "https://www.youtube.com/channel/"+video.getChannelId();
        }

        return null;
    }
}
