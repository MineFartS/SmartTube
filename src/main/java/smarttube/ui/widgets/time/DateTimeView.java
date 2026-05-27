package minefarts.smarttube.ui.widgets.time;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import minefarts.smarttube.utils.helpers.DateHelper;
import minefarts.smarttube.utils.TickleManager;
import minefarts.smarttube.utils.TickleManager.TickleListener;

/**
 * Note, same view is used inside player and in as global time view
 */
@SuppressLint("AppCompatCustomView")
public class DateTimeView extends TextView implements TickleListener {
    
    private TickleManager mTickleManager;

    public DateTimeView(Context context) {
        super(context);
        init();
    }

    public DateTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DateTimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mTickleManager = TickleManager.instance();
        enableListener(true);
    }

    private void enableListener(boolean enabled) {
        if (enabled) {
            mTickleManager.addListener(this);
        } else {
            mTickleManager.removeListener(this);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        enableListener(visibility == View.VISIBLE);
    }

    @Override
    public void onTickle() {
        if (getVisibility() == View.VISIBLE) {
            setText(DateHelper.getCurrentDateTimeShort());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        enableListener(false);
    }

}
