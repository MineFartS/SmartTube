package minefarts.smarttube.commons;

import android.view.View;

/**
 * Stub for chatkit ViewHolder.
 */
public abstract class ViewHolder<DATA> {
    public View itemView;

    public ViewHolder(View itemView) {
        this.itemView = itemView;
    }

    public abstract void onBind(DATA data);
}