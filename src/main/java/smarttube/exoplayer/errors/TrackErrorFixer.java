package minefarts.smarttube.exoplayer.errors;

import androidx.annotation.Nullable;
import minefarts.smarttube.ExoPlaybackException;
import minefarts.smarttube.mediacodec.MediaCodecRenderer.DecoderInitializationException;
import minefarts.smarttube.source.DefaultMediaSourceEventListener;
import minefarts.smarttube.source.MediaSource.MediaPeriodId;
import minefarts.smarttube.source.chunk.Chunk;
import minefarts.smarttube.source.chunk.ContainerMediaChunk;
import minefarts.smarttube.upstream.HttpDataSource.InvalidResponseCodeException;
import minefarts.smarttube.utils.MimeTypes;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.exoplayer.selector.TrackSelectorManager;
import minefarts.smarttube.exoplayer.selector.track.MediaTrack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TrackErrorFixer extends DefaultMediaSourceEventListener {
    private static final int BLACKLIST_CHECK_MS = 1_000;
    private static final int BLACKLIST_CLEAR_MS = 10_000;
    private static final String TAG = TrackErrorFixer.class.getSimpleName();
    private final TrackSelectorManager mTrackSelectorManager;
    private long mSelectionTimeMs;

    private InvalidResponseCodeException mLastEx;

    public TrackErrorFixer(TrackSelectorManager trackSelectorManager) {
        mTrackSelectorManager = trackSelectorManager;
    }

    /**
     * 1) Blacklist non-playable audio tracks for live streams.<br/>
     * Last segment of such streams produce 404 error.<br/>
     * See DrLupo streams, for example.<br/.
     * <br/>
     * 2) Blacklist non-playable tracks for regular videos (error 503).<br/>
     */
    public boolean fixError(Exception e) {
        if (!(e instanceof InvalidResponseCodeException)) {
            return false;
        }

        InvalidResponseCodeException ex = (InvalidResponseCodeException) e;

        if (ex.responseCode != 404 && ex.responseCode != 503 && ex.responseCode != 500) {
            return false;
        }

        if (System.currentTimeMillis() - mSelectionTimeMs < BLACKLIST_CHECK_MS) {
            return false;
        }

        mLastEx = ex;

        return selectDifferentCodec(isAudio(mLastEx));
    }

    private boolean selectDifferentCodec(boolean isAudio) {
        if (System.currentTimeMillis() - mSelectionTimeMs < BLACKLIST_CLEAR_MS) {
            return false;
        }

        Set<MediaTrack> tracks = isAudio ? mTrackSelectorManager.getAudioTracks() : mTrackSelectorManager.getVideoTracks();

        if (tracks == null) {
            return false;
        }

        MediaTrack currentTrack = null;

        for (MediaTrack track : tracks) {
            if (track.isSelected) {
                currentTrack = track;
                break;
            }
        }

        if (currentTrack == null || currentTrack.format == null || currentTrack.format.codecs == null) {
            return false;
        }

        String currentCodec = currentTrack.format.codecs;
        int width = currentTrack.format.width;

        MediaTrack nextTrack = null;

        for (MediaTrack track : tracks) {
            if (track.format == null) {
                continue;
            }

            if (!currentCodec.equals(track.format.codecs) && track.format.width <= width) {
                nextTrack = track;
                break;
            }
        }

        if (nextTrack != null) {
            mTrackSelectorManager.selectTrack(nextTrack);
            mSelectionTimeMs = System.currentTimeMillis();
            return true;
        }

        return false;
    }

    private boolean isAudio(InvalidResponseCodeException ex) {
        String url = ex.dataSpec.uri.toString();

        return url.contains("mime/audio");
    }

    public void fixEmptyChunk(Chunk chunk) {
        // Fix when just started new type live stream ahead of the position
        if (chunk instanceof ContainerMediaChunk) {
            long nextLoadPosition = (Long) Helpers.getField(chunk, "nextLoadPosition");
            if (nextLoadPosition == 0) {
                Log.e(TAG, "Stream position behind the timeline. Waiting for new data...");
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onLoadError(int windowIndex, @Nullable MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo,
                            MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {
        fixError(error);
    }
}
