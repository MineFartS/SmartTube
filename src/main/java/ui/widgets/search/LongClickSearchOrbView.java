package SmartTubeApp.ui.widgets.search;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.leanback.widget.SearchOrbView;
import SmartTubeApp.prefs.GeneralData;

/**
 *     1) Add long click listener <br/>
 *     2) Disable short click if corresponding option enabled
 */
public class LongClickSearchOrbView extends SearchOrbView implements View.OnLongClickListener {
    private OnLongClickListener mListener2;

    public LongClickSearchOrbView(Context context) {
        this(context, null);
    }

    public LongClickSearchOrbView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LongClickSearchOrbView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View view) {
        if (null != mListener2) {
            return mListener2.onLongClick(view);
        } else {
            super.onClick(view);
            return true;
        }
    }

    public void setOnOrbLongClickedListener(OnLongClickListener listener) {
        mListener2 = listener;
    }
}
