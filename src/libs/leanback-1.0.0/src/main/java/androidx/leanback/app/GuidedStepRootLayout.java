package androidx.leanback.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.leanback.widget.Util;

/**
 * Utility class used by GuidedStepFragment to disable focus out left/right.
 */
class GuidedStepRootLayout extends LinearLayout {

    private boolean mFocusOutStart = false;
    private boolean mFocusOutEnd = false;

    public GuidedStepRootLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GuidedStepRootLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setFocusOutStart(boolean focusOutStart) {
        mFocusOutStart = focusOutStart;
    }

    public void setFocusOutEnd(boolean focusOutEnd) {
        mFocusOutEnd = focusOutEnd;
    }

    @Override
    public View focusSearch(View focused, int direction) {
        View newFocus = super.focusSearch(focused, direction);
        if (direction == FOCUS_LEFT || direction == FOCUS_RIGHT) {
            if (Util.isDescendant(this, newFocus)) {
                return newFocus;
            }
            if (getLayoutDirection() == ViewGroup.LAYOUT_DIRECTION_LTR
                    ? direction == FOCUS_LEFT : direction == FOCUS_RIGHT) {
                if (!mFocusOutStart) {
                    return focused;
                }
            } else {
                if (!mFocusOutEnd) {
                    return focused;
                }
            }
        }
        return newFocus;
    }
}
