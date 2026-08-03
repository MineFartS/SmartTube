package minefarts.smarttube.leanback.transition;

import android.graphics.Rect;

import androidx.annotation.RestrictTo;

/**
 * Class to get the epicenter of Transition.
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public abstract class TransitionEpicenterCallback {

    /**
     * Implementers must override to return the epicenter of the Transition in screen
     * coordinates.
     *
     * @param transition The transition for which the epicenter applies.
     * @return The Rect region of the epicenter of <code>transition</code> or null if
     * there is no epicenter.
     */
    public abstract Rect onGetEpicenter(Object transition);
}
