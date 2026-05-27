package androidx.leanback.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.leanback.R;

/**
 * ListRowHoverCardView contains a title and description.
 */
public final class ListRowHoverCardView extends LinearLayout {

    private final TextView mTitleView;
    private final TextView mDescriptionView;

    public ListRowHoverCardView(Context context) {
       this(context, null);
    }

    public ListRowHoverCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListRowHoverCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.lb_list_row_hovercard, this);
        mTitleView = findViewById(R.id.title);
        mDescriptionView = findViewById(R.id.description);
    }

    /**
     * Returns the title text.
     */
    public final CharSequence getTitle() {
        return mTitleView.getText();
    }

    /**
     * Sets the title text.
     */
    public final void setTitle(CharSequence text) {
        if (!TextUtils.isEmpty(text)) {
            mTitleView.setText(text);
            mTitleView.setVisibility(View.VISIBLE);
        } else {
            mTitleView.setVisibility(View.GONE);
        }
    }

    /**
     * Returns the description text.
     */
    public final CharSequence getDescription() {
        return mDescriptionView.getText();
    }

    /**
     * Sets the description text.
     */
    public final void setDescription(CharSequence text) {
        if (!TextUtils.isEmpty(text)) {
            mDescriptionView.setText(text);
            mDescriptionView.setVisibility(View.VISIBLE);
        } else {
            mDescriptionView.setVisibility(View.GONE);
        }
    }
}
