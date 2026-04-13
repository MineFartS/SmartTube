

package androidx.leanback.media;

import android.view.SurfaceHolder;

/**
 * Optional interface to be implemented by any subclass of {@link PlaybackGlueHost} that contains
 * a {@link android.view.SurfaceView}. This will allow subclass of {@link PlaybackGlue} to setup
 * the surface holder callback during {@link PlaybackGlue#setHost(PlaybackGlueHost)}.
 *
 * @see PlaybackGlue#setHost(PlaybackGlueHost)
 */
public interface SurfaceHolderGlueHost {
    /**
     * Sets the {@link SurfaceHolder.Callback} on the the host.
     */
    void setSurfaceHolderCallback(SurfaceHolder.Callback callback);
}
