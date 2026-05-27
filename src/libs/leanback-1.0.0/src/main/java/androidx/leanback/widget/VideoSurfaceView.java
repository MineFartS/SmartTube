package androidx.leanback.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

import androidx.annotation.RestrictTo;

/**
 * Activity transition will change transitionVisibility multiple times even the view is not
 * running transition, which causes visual flickering during activity return transition.
 * This class disables setTransitionVisibility() to avoid the problem.
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class VideoSurfaceView extends SurfaceView {

    public VideoSurfaceView(Context context) {
        super(context);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Overrides hidden method View.setTransitionVisibility() to disable visibility changes
     * in activity transition.
     */
    public void setTransitionVisibility(int visibility) {
    }

}
