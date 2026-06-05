package minefarts.smarttube.leanback.widget;

import android.os.Build;
import android.view.View;

import minefarts.smarttube.R;

/**
 * Helper for setting rounded rectangle backgrounds on a view.
 */
final class RoundedRectHelper {
    static boolean supportsRoundedCorner() {
        return Build.VERSION.SDK_INT >= 21;
    }

    /**
     * Sets or removes a rounded rectangle outline on the given view.
     */
    static void setClipToRoundedOutline(View view, boolean clip, int radius) {
        if (Build.VERSION.SDK_INT >= 21) {
            RoundedRectHelperApi21.setClipToRoundedOutline(view, clip, radius);
        }
    }

    /**
     * Sets or removes a rounded rectangle outline on the given view.
     */
    static void setClipToRoundedOutline(View view, boolean clip) {
        if (Build.VERSION.SDK_INT >= 21) {
            int radius = view.getResources().getDimensionPixelSize(
                    R.dimen.lb_rounded_rect_corner_radius);
            RoundedRectHelperApi21.setClipToRoundedOutline(view, clip, radius);
        }
    }

    private RoundedRectHelper() {
    }
}
