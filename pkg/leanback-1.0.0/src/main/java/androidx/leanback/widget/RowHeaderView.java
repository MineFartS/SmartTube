
package androidx.leanback.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.widget.TextView;

import androidx.core.widget.TextViewCompat;
import androidx.leanback.R;

/**
 * RowHeaderView is a header text view.
 */
public final class RowHeaderView extends TextView {

    public RowHeaderView(Context context) {
        this(context, null);
    }

    public RowHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.rowHeaderStyle);
    }

    public RowHeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * See
     * {@link TextViewCompat#setCustomSelectionActionModeCallback(TextView, ActionMode.Callback)}
     */
    @Override
    public void setCustomSelectionActionModeCallback(ActionMode.Callback actionModeCallback) {
        super.setCustomSelectionActionModeCallback(TextViewCompat
                .wrapCustomSelectionActionModeCallback(this, actionModeCallback));
    }

}
