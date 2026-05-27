package minefarts.smarttube.ui.playback.mod.surface;

import android.view.SurfaceHolder;
import minefarts.smarttube.leanback.app.PlaybackSupportFragmentGlueHost;
import minefarts.smarttube.leanback.media.PlaybackGlue;
import minefarts.smarttube.leanback.media.PlaybackGlueHost;
import minefarts.smarttube.leanback.media.SurfaceHolderGlueHost;

/**
 * {@link PlaybackGlueHost} implementation
 * the interaction between {@link PlaybackGlue} and {@link minefarts.smarttube.leanback.app.VideoSupportFragment}.
 */
public class SurfacePlaybackFragmentGlueHost extends PlaybackSupportFragmentGlueHost
        implements SurfaceHolderGlueHost {
    @SuppressWarnings("HidingField") // Supertype field is package scope to avoid synthetic accessor
    private final SurfacePlaybackFragment mFragment;

    public SurfacePlaybackFragmentGlueHost(SurfacePlaybackFragment fragment) {
        super(fragment);
        this.mFragment = fragment;
    }

    /**
     * Sets the {@link SurfaceHolder.Callback} on the host.
     * {@link PlaybackGlueHost} is assumed to either host the {@link SurfaceHolder} or
     * have a reference to the component hosting it for rendering the video.
     */
    @Override
    public void setSurfaceHolderCallback(SurfaceHolder.Callback callback) {
        mFragment.setSurfaceHolderCallback(callback);
    }

}
