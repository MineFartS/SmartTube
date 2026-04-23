
package androidx.leanback.widget;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.view.View;

import androidx.annotation.RestrictTo;

/**
 * Interface for highlighting the item that has focus.
 */
interface FocusHighlightHandler {
    /**
     * Called when an item gains or loses focus.
     * @hide
     *
     * @param view The view whose focus is changing.
     * @param hasFocus True if focus is gained; false otherwise.
     */
    @RestrictTo(LIBRARY_GROUP)
    void onItemFocused(View view, boolean hasFocus);

    /**
     * Called when the view is being created.
     */
    void onInitializeView(View view);
}
