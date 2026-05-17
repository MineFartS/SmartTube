
package androidx.leanback.widget;

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
        
        if (view.getBackground() != null) {
            drawable.setAlpha(view.getBackground().getAlpha());
        }
        view.setBackground(drawable);

    }

}
