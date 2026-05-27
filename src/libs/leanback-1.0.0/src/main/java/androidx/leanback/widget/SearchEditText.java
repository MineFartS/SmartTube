package androidx.leanback.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;

import androidx.core.widget.TextViewCompat;
import androidx.leanback.R;

/**
 * EditText widget that monitors keyboard changes.
 */
public class SearchEditText extends StreamingTextView {
    private static final String TAG = SearchEditText.class.getSimpleName();
    private static final boolean DEBUG = false;

    /**
     * Interface for receiving notification when the keyboard is dismissed.
     */
    public interface OnKeyboardDismissListener {
        /**
         * Method invoked when the keyboard is dismissed.
         */
        public void onKeyboardDismiss();
    }

    private OnKeyboardDismissListener mKeyboardDismissListener;

    public SearchEditText(Context context) {
        this(context, null);
    }

    public SearchEditText(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.TextAppearance_Leanback_SearchTextEdit);
    }

    public SearchEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (DEBUG) Log.v(TAG, "Keyboard being dismissed");
            if (mKeyboardDismissListener != null) {
                mKeyboardDismissListener.onKeyboardDismiss();
            }
            return false;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    /**
     * Sets a keyboard dismissed listener.
     *
     * @param listener The listener.
     */
    public void setOnKeyboardDismissListener(OnKeyboardDismissListener listener) {
        mKeyboardDismissListener = listener;
    }
}
