
package androidx.leanback.widget;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.RestrictTo;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class Util {

    /**
     * Returns true if child == parent or is descendant of the parent.
     */
    public static boolean isDescendant(ViewGroup parent, View child) {
        while (child != null) {
            if (child == parent) {
                return true;
            }
            ViewParent p = child.getParent();
            if (!(p instanceof View)) {
                return false;
            }
            child = (View) p;
        }
        return false;
    }

    private Util() {
    }
}
