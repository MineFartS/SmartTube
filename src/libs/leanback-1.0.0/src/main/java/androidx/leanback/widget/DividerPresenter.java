
package androidx.leanback.widget;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RestrictTo;
import androidx.leanback.R;

/**
 * DividerPresenter provides a default presentation for {@link DividerRow} in HeadersFragment.
 */
public class DividerPresenter extends Presenter {

    private final int mLayoutResourceId;

    public DividerPresenter() {
        this(R.layout.lb_divider);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    public DividerPresenter(int layoutResourceId) {
        mLayoutResourceId = layoutResourceId;
    }

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View headerView = LayoutInflater.from(parent.getContext())
                .inflate(mLayoutResourceId, parent, false);

        return new ViewHolder(headerView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
    }

}
