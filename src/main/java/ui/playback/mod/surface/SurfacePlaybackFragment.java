package SmartTubeApp.ui.playback.mod.surface;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.leanback.app.PlaybackSupportFragment;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.liskovsoft.sharedutils.helpers.Helpers;
import SmartTubeApp.app.models.playback.manager.PlayerEngine;
import SmartTubeApp.prefs.PlayerData;
import SmartTubeApp.prefs.PlayerTweaksData;
import SmartTubeApp.util.ViewUtil;
import SmartTubeApp.R;
import android.util.Log;

/**
 * Subclass of {@link PlaybackSupportFragment} that is responsible for providing a {@link SurfaceView}
 * and rendering video.
 */
public class SurfacePlaybackFragment extends PlaybackSupportFragment {

    private SurfaceWrapper mVideoSurfaceWrapper;
    private AspectRatioFrameLayout mVideoSurfaceRoot;
    private int mBackgroundResId;
    
    private static final String TAG = "SurfacePlaybackFragment";

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
        
        mVideoSurfaceRoot.addView(mVideoSurfaceWrapper.getSurfaceView(), 0);
        
        mVideoSurfaceRoot.setAspectRatioListener(this::scaleIfNeeded);
        
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

    private void scaleIfNeeded(Object... ignored) {

        if (mVideoSurfaceRoot.getWidth() == 0 || mVideoSurfaceRoot.getHeight() == 0) {
            return;
        }

        float angle = mVideoSurfaceRoot.getRotation();

        int width, height;

        if (Helpers.floatEquals(angle, 90) || Helpers.floatEquals(angle, 270)) {
            float ratio = mVideoSurfaceRoot.getWidth() / ((float) mVideoSurfaceRoot.getHeight());

            width = mVideoSurfaceRoot.getHeight();
            height = (int) (mVideoSurfaceRoot.getHeight() / ratio);
        } else {
            width = mVideoSurfaceRoot.getWidth();
            height = mVideoSurfaceRoot.getHeight();
        }

        // https://stackoverflow.com/questions/52196362/how-resize-textureview-to-fullscreen-when-rotation-90
        View textureView = mVideoSurfaceWrapper.getSurfaceView();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) textureView.getLayoutParams();
        params.width = width;
        params.height = height;
        params.gravity = Gravity.CENTER;
        textureView.setLayoutParams(params);
    }

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

}
