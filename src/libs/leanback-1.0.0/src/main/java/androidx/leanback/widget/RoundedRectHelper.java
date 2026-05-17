
package androidx.leanback.widget;

import android.os.Build;
import android.view.View;

import androidx.leanback.R;

/**
 * Helper for setting rounded rectangle backgrounds on a view.
 */
final class RoundedRectHelper {
    static boolean supportsRoundedCorner() {
        return true;
    }

    /**
     * Sets or removes a rounded rectangle outline on the given view.
     */
    static void setClipToRoundedOutline(View view, boolean clip, int radius) {
        RoundedRectHelperApi21.setClipToRoundedOutline(view, clip, radius);
    }

    /**
     * Sets or removes a rounded rectangle outline on the given view.
     */
    static void setClipToRoundedOutline(View view, boolean clip) {
        int radius = view.getResources().getDimensionPixelSize(
                R.dimen.lb_rounded_rect_corner_radius);
        RoundedRectHelperApi21.setClipToRoundedOutline(view, clip, radius);
    }

}
