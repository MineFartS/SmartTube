package androidx.leanback.app;

import android.view.SurfaceHolder;

import androidx.leanback.media.PlaybackGlue;
import androidx.leanback.media.PlaybackGlueHost;
import androidx.leanback.media.SurfaceHolderGlueHost;

/**
 * {@link PlaybackGlueHost} implementation
 * the interaction between {@link PlaybackGlue} and {@link VideoSupportFragment}.
 */
public class VideoSupportFragmentGlueHost extends PlaybackSupportFragmentGlueHost
        implements SurfaceHolderGlueHost {
    @SuppressWarnings("HidingField") // Supertype field is package scope to avoid synthetic accessor
    private final VideoSupportFragment mFragment;

    public VideoSupportFragmentGlueHost(VideoSupportFragment fragment) {
        super(fragment);
        this.mFragment = fragment;
    }

    /**
     * Sets the {@link android.view.SurfaceHolder.Callback} on the host.
     * {@link PlaybackGlueHost} is assumed to either host the {@link SurfaceHolder} or
     * have a reference to the component hosting it for rendering the video.
     */
    @Override
    public void setSurfaceHolderCallback(SurfaceHolder.Callback callback) {
        mFragment.setSurfaceHolderCallback(callback);
    }

}
