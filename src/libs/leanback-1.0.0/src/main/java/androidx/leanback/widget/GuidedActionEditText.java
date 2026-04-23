
package androidx.leanback.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.autofill.AutofillValue;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.widget.TextViewCompat;

/**
 * A custom EditText that satisfies the IME key monitoring requirements of GuidedStepFragment.
 */
public class GuidedActionEditText extends EditText implements ImeKeyMonitor,
        GuidedActionAutofillSupport {

    /**
     * Workaround for b/26990627 forcing recompute the padding for the View when we turn on/off
     * the default background of EditText
     */
    static final class NoPaddingDrawable extends Drawable {
        @Override
        public boolean getPadding(Rect padding) {
            padding.set(0, 0, 0, 0);
            return true;
        }

        @Override
        public void draw(Canvas canvas) {
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSPARENT;
        }
    }

    private ImeKeyListener mKeyListener;
    private OnAutofillListener mAutofillListener;
    private final Drawable mSavedBackground;
    private final Drawable mNoPaddingDrawable;

    public GuidedActionEditText(Context ctx) {
        this(ctx, null);
    }

    public GuidedActionEditText(Context ctx, AttributeSet attrs) {
        this(ctx, attrs, android.R.attr.editTextStyle);
    }

    public GuidedActionEditText(Context ctx, AttributeSet attrs, int defStyleAttr) {
        super(ctx, attrs, defStyleAttr);
        mSavedBackground = getBackground();
        mNoPaddingDrawable = new NoPaddingDrawable();
        setBackground(mNoPaddingDrawable);
    }

    @Override
    public void setImeKeyListener(ImeKeyListener listener) {
        mKeyListener = listener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        boolean result = false;
        if (mKeyListener != null) {
            result = mKeyListener.onKeyPreIme(this, keyCode, event);
        }
        if (!result) {
            result = super.onKeyPreIme(keyCode, event);
        }
        return result;
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(isFocused() ? EditText.class.getName() : TextView.class.getName());
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            setBackground(mSavedBackground);
        } else {
            setBackground(mNoPaddingDrawable);
        }
        // Make the TextView focusable during editing, avoid the TextView gets accessibility focus
        // before editing started. see also GuidedActionAdapterGroup where setFocusable(true).
        if (!focused) {
            setFocusable(false);
        }
    }

    @Override
    public int getAutofillType() {
        // make it always autofillable as Guided fragment switches InputType when user clicks
        // on the field.
        return AUTOFILL_TYPE_TEXT;
    }

    @Override
    public void setOnAutofillListener(OnAutofillListener autofillListener) {
        mAutofillListener = autofillListener;
    }

    @Override
    public void autofill(AutofillValue values) {
        super.autofill(values);
        if (mAutofillListener != null) {
            mAutofillListener.onAutofill(this);
        }
    }

    /**
     * See
     * {@link TextViewCompat#setCustomSelectionActionModeCallback(TextView, ActionMode.Callback)}
     */
    @SuppressLint("RestrictedApi")
    @Override
    public void setCustomSelectionActionModeCallback(ActionMode.Callback actionModeCallback) {
        super.setCustomSelectionActionModeCallback(TextViewCompat
                .wrapCustomSelectionActionModeCallback(this, actionModeCallback));
    }
}
