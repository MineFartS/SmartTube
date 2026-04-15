package smartyoutubetv1.app.models.playback.controllers;

import com.liskovsoft.sharedutils.mylogger.Log;
import smartyoutubetv1.app.models.playback.BasePlayerController;
import smartyoutubetv1.app.models.data.Video;
import smartyoutubetv1.app.views.PlaybackView;
import smartyoutubetv1.misc.MediaServiceManager;
import com.liskovsoft.mediaserviceinterfaces.SignInService;

public class WatchStateController extends BasePlayerController {
    private static final String TAG = WatchStateController.class.getSimpleName();
    private boolean mUploaded = false;

    @Override
    public void onPlayEnd() {
        if (mUploaded) {
            Log.d(TAG, "Watch state already uploaded, skipping");
            return;
        }

        Video video = getVideo();
        PlaybackView player = getPlayer();

        if (video == null || player == null) {
            Log.w(TAG, "Can't upload watch state: video or player null");
            return;
        }

        long durationMs = player.getDurationMs();
        if (durationMs <= 0) {
            Log.w(TAG, "Can't upload watch state: invalid duration");
            return;
        }

        SignInService signIn = getSignInService();
        if (!signIn.isSigned()) {
            Log.d(TAG, "Not signed in, skipping watch state upload");
            return;
        }

        Log.d(TAG, String.format("Uploading watch state completion: video=%s durationMs=%d", video.videoId, durationMs));

        MediaServiceManager.instance().updateHistory(video, durationMs);

        mUploaded = true;
        Log.d(TAG, "Watch state uploaded successfully");
    }

    @Override
    public void onNewVideo(Video item) {
        mUploaded = false; // reset for new video
    }

    @Override
    public void onFinish() {
        mUploaded = false; // reset
    }
}
