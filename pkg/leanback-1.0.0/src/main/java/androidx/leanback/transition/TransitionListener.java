
package androidx.leanback.transition;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.RestrictTo;

/**
 * Listeners for transition start and stop.
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class TransitionListener {

    protected Object mImpl;

    public void onTransitionStart(Object transition) {
    }

    public void onTransitionEnd(Object transition) {
    }

    public void onTransitionCancel(Object transition) {
    }

    public void onTransitionPause(Object transition) {
    }

    public void onTransitionResume(Object transition) {
    }
}
