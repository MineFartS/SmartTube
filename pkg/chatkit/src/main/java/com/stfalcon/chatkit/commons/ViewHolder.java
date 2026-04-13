

package com.stfalcon.chatkit.commons;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Base ViewHolder
 */
public abstract class ViewHolder<DATA> extends RecyclerView.ViewHolder {

    public abstract void onBind(DATA data);

    public ViewHolder(View itemView) {
        super(itemView);
    }

}
