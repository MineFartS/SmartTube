
package androidx.leanback.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Root view of GuidedAction item, it supports a foreground drawable and can disable focus out
 * of view.
 */
class GuidedActionItemContainer extends NonOverlappingLinearLayoutWithForeground {

    private boolean mFocusOutAllowed = true;

    public GuidedActionItemContainer(Context context) {
        this(context, null);
    }

    public GuidedActionItemContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GuidedActionItemContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public View focusSearch(View focused, int direction) {
        if (mFocusOutAllowed || !Util.isDescendant(this, focused)) {
            return super.focusSearch(focused, direction);
        }
        View view = super.focusSearch(focused, direction);
        if (Util.isDescendant(this, view)) {
            return view;
        }
        return null;
    }

    public void setFocusOutAllowed(boolean focusOutAllowed) {
        mFocusOutAllowed = focusOutAllowed;
    }
}
