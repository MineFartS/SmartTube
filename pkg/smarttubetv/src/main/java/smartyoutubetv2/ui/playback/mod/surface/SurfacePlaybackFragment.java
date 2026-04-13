package smartyoutubetv2.ui.playback.mod.surface;

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
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.ResizeMode;
import com.liskovsoft.sharedutils.helpers.Helpers;
import smartyoutubetv1.app.models.playback.manager.PlayerEngine;
import smartyoutubetv1.prefs.PlayerData;
import smartyoutubetv1.prefs.PlayerTweaksData;
import smartyoutubetv2.util.ViewUtil;
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
        
        mVideoSurfaceRoot = root.findViewById(smartyoutubetv2.R.id.surface_root);
        if (mVideoSurfaceRoot == null) {
            Log.e(TAG, "surface_root not found in layout");
            return root;
        }
        
        if (mVideoSurfaceWrapper == null) {
            mVideoSurfaceWrapper = new SurfaceViewWrapper(getContext(), root);
        }
        
        mVideoSurfaceRoot.addView(mVideoSurfaceWrapper.getSurfaceView(), 0);
        mVideoSurfaceRoot.setAspectRatioListener((targetAspectRatio, naturalAspectRatio, aspectRatioMismatch) -> scaleIfNeeded());
        
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

    /** Returns the {@link ResizeMode}. */
    protected @ResizeMode int getResize() {
        return mVideoSurfaceRoot.getResizeMode();
    }

    /**
     * Sets the {@link ResizeMode}.
     *
     * @param resizeMode The {@link ResizeMode}.
     */
    protected void setResize(@ResizeMode int resizeMode) {
        mVideoSurfaceRoot.setResizeMode(resizeMode);
    }

    protected void setZoom(int percents) {
        mVideoSurfaceRoot.setZoom(percents);
    }

    protected void setRotation(int angle) {
        if (Helpers.floatEquals(mVideoSurfaceRoot.getRotation(), angle) || mVideoSurfaceWrapper == null) {
            return;
        }

        if (mVideoSurfaceWrapper instanceof TextureViewWrapper) {
            mVideoSurfaceRoot.setRotation(angle);
        } else {
            mVideoSurfaceRoot.removeView(mVideoSurfaceWrapper.getSurfaceView());
            mVideoSurfaceWrapper = new TextureViewWrapper(getContext(), (ViewGroup) getView());
            mVideoSurfaceRoot.addView(mVideoSurfaceWrapper.getSurfaceView(), 0);
            mVideoSurfaceRoot.setRotation(angle);

            ((PlayerEngine) this).restartEngine();
        }
    }

    protected void setFlipEnabled(boolean enabled) {
        float scaleX = enabled ? -1f : 1f;

        if (Helpers.floatEquals(mVideoSurfaceRoot.getScaleX(), scaleX) || mVideoSurfaceWrapper == null) {
            return;
        }

        if (mVideoSurfaceWrapper instanceof TextureViewWrapper) {
            mVideoSurfaceRoot.setScaleX(scaleX);
        } else {
            mVideoSurfaceRoot.removeView(mVideoSurfaceWrapper.getSurfaceView());
            mVideoSurfaceWrapper = new TextureViewWrapper(getContext(), (ViewGroup) getView());
            mVideoSurfaceRoot.addView(mVideoSurfaceWrapper.getSurfaceView(), 0);
            mVideoSurfaceRoot.setScaleX(scaleX);

            ((PlayerEngine) this).restartEngine();
        }
    }

    private void scaleIfNeeded() {
        if (!(mVideoSurfaceWrapper instanceof TextureViewWrapper)) {
            return;
        }

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
