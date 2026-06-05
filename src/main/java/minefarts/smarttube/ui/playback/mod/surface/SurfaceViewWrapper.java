package minefarts.smarttube.ui.playback.mod.surface;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

public class SurfaceViewWrapper implements SurfaceWrapper {
    
    private int mState = SURFACE_NOT_CREATED;
    private final SurfaceView mVideoSurface;
    private SurfaceHolder.Callback mMediaPlaybackCallback;

    public SurfaceViewWrapper(Context context, ViewGroup root) {
        
        mVideoSurface = (SurfaceView) LayoutInflater.from(context).inflate(
            minefarts.smarttube.R.layout.lb_video_surface, 
            root, false
        );

        // PIP flickering fix
        // https://github.com/google/ExoPlayer/issues/8611
        //mVideoSurface.getHolder().setFixedSize(1, 1);
        mVideoSurface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (mMediaPlaybackCallback != null) {
                    mMediaPlaybackCallback.surfaceCreated(holder);
                }
                mState = SURFACE_CREATED;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mMediaPlaybackCallback != null) {
                    mMediaPlaybackCallback.surfaceChanged(holder, format, width, height);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mMediaPlaybackCallback != null) {
                    mMediaPlaybackCallback.surfaceDestroyed(holder);
                }
                mState = SURFACE_NOT_CREATED;
            }
        });
    }

    /**
     * Adds {@link SurfaceHolder.Callback} to {@link SurfaceView}.
     */
    @Override
    public void setSurfaceHolderCallback(SurfaceHolder.Callback callback) {
        mMediaPlaybackCallback = callback;

        if (callback != null) {
            if (mState == SURFACE_CREATED) {
                SurfaceHolder holder = mVideoSurface.getHolder();
                // Ensure we re-fire both events when the callback is attached late.
                // Some renderers (and devices) can remain black if only surfaceCreated is sent
                // but surfaceChanged (width/height) isn't delivered.
                mMediaPlaybackCallback.surfaceCreated(holder);

                // Provide current surface size if possible.
                // getSurfaceFrame() isn't always reliable; try holder.getSurfaceFrame() then fall back.
                android.graphics.Rect frame = holder.getSurfaceFrame();
                int width = frame != null ? frame.width() : 0;
                int height = frame != null ? frame.height() : 0;

                // Fallback to current view size if surface frame is empty.
                if (width <= 0 || height <= 0) {
                    width = mVideoSurface.getWidth();
                    height = mVideoSurface.getHeight();
                }

                if (width > 0 && height > 0) {
                    mMediaPlaybackCallback.surfaceChanged(holder, /* format= */ 0, width, height);
                }
            }
        }
    }


    @Override
    public View getSurfaceView() {
        return mVideoSurface;
    }
}
