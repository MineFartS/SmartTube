package minefarts.smarttube.leanback.widget;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.RestrictTo;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class InvisibleRowPresenter extends RowPresenter {

    public InvisibleRowPresenter() {
        setHeaderPresenter(null);
    }

    @Override
    protected ViewHolder createRowViewHolder(ViewGroup parent) {
        RelativeLayout root = new RelativeLayout(parent.getContext());
        root.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
        return new ViewHolder(root);
    }
}
