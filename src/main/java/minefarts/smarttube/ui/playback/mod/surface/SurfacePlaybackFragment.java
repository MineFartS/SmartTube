package minefarts.smarttube.ui.playback.mod.surface;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import minefarts.smarttube.leanback.app.PlaybackSupportFragment;
import minefarts.smarttube.ui.AspectRatioFrameLayout;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.prefs.PlayerTweaksData;
import minefarts.smarttube.utils.ViewUtil;
import minefarts.smarttube.R;
import android.util.Log;

/**
 * Subclass of {@link PlaybackSupportFragment} that is responsible for providing a {@link SurfaceView}
 * and rendering video.
 */
public class SurfacePlaybackFragment extends PlaybackSupportFragment {

    private SurfaceWrapper mVideoSurfaceWrapper;
    private AspectRatioFrameLayout mVideoSurfaceRoot;
    private int mBackgroundResId;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState
    ) {

        ViewGroup root = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

        if (root == null)
            throw new IllegalStateException("Can't create root of SurfacePlaybackFragment");

        mVideoSurfaceWrapper = new SurfaceViewWrapper(getContext(), root);

        mVideoSurfaceRoot = root.findViewById(R.id.surface_root);

        View surfaceView = mVideoSurfaceWrapper.getSurfaceView();
        FrameLayout.LayoutParams params =
            new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        surfaceView.setLayoutParams(params);

        mVideoSurfaceRoot.addView(surfaceView, 0);

        // Scaling/ratio calculations removed. Let layout handle video sizing.
        setBackgroundType(PlaybackSupportFragment.BG_LIGHT);
        return root;
    }

    /**
     * Adds {@link SurfaceHolder.Callback} to {@link SurfaceView}.
     */
    public void setSurfaceHolderCallback(SurfaceHolder.Callback callback) {
        if (mVideoSurfaceWrapper != null) {
            mVideoSurfaceWrapper.setSurfaceHolderCallback(callback);
        }
    }

    /**
     * Returns the surface view.
     */
    public View getSurfaceView() {
        return mVideoSurfaceWrapper.getSurfaceView();
    }

    @Override
    public void onDestroyView() {
        mVideoSurfaceWrapper = null;
        super.onDestroyView();
    }

    // Removed: scaleIfNeeded() was calculating aspect ratio/scale based on container rotation.
    // This caused incorrect fitting; now we rely on layout/Player sizing instead.


    /**
     * Setup player's background used when controls are showed.
     * @param resId background
     */
    protected void setBackgroundResource(int resId) {
        if (resId <= 0 || mBackgroundResId == resId) {
            return;
        }

        View backgroundView = (View) Helpers.getField(this, "mBackgroundView");

        if (backgroundView != null) {
            backgroundView.setBackgroundResource(resId);
            mBackgroundResId = resId;
        }
    }

    protected void setGravity(int gravity) {
        ViewUtil.setGravity(mVideoSurfaceRoot, gravity);
    }

    public void setVideoAspectRatio(int width, int height, float pixelWidthHeightRatio) {
        if (mVideoSurfaceRoot == null || width <= 0 || height <= 0) {
            return;
        }
        float videoAspectRatio = (width * pixelWidthHeightRatio) / height;
        mVideoSurfaceRoot.setAspectRatio(videoAspectRatio);
    }
    
}
