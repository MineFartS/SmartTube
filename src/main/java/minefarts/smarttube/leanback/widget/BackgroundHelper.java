package minefarts.smarttube.leanback.widget;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import androidx.annotation.RestrictTo;

/**
 * Helper for view backgrounds.
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class BackgroundHelper {
    public static void setBackgroundPreservingAlpha(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= 19) {
            if (view.getBackground() != null) {
                drawable.setAlpha(view.getBackground().getAlpha());
            }
            view.setBackground(drawable);
        } else {
            // Cannot query drawable alpha
            view.setBackground(drawable);
        }
    }

    private BackgroundHelper() {
    }
}
