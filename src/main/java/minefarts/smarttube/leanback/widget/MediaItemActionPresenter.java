package minefarts.smarttube.leanback.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import minefarts.smarttube.R;

/**
 * The presenter displaying a custom action in {@link AbstractMediaItemPresenter}.
 * This is the default presenter for actions in media rows if no action presenter is provided by the
 * user.
 *
 * Binds to items of type {@link MultiActionsProvider.MultiAction}.
 */
class MediaItemActionPresenter extends Presenter {

    MediaItemActionPresenter() {
    }

    static class ViewHolder extends Presenter.ViewHolder {
        final ImageView mIcon;

        public ViewHolder(View view) {
            super(view);
            mIcon = (ImageView) view.findViewById(R.id.actionIcon);
        }

        public ImageView getIcon() {
            return mIcon;
        }
    }

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        View actionView = LayoutInflater.from(context)
                .inflate(R.layout.lb_row_media_item_action, parent, false);
        return new ViewHolder(actionView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        ViewHolder actionViewHolder = (ViewHolder) viewHolder;
        MultiActionsProvider.MultiAction action = (MultiActionsProvider.MultiAction) item;
        actionViewHolder.getIcon().setImageDrawable(action.getCurrentDrawable());
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
    }
}
