
package androidx.leanback.transition;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.animation.AnimationUtils;

import androidx.annotation.RestrictTo;
import androidx.leanback.R;

/**
 * Helper class to load Leanback specific transition.
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class LeanbackTransitionHelper {

    public static Object loadTitleInTransition(Context context) {
        return TransitionHelper.loadTransition(context, R.transition.lb_title_in);
    }

    public static Object loadTitleOutTransition(Context context) {
        return TransitionHelper.loadTransition(context, R.transition.lb_title_out);
    }

}
