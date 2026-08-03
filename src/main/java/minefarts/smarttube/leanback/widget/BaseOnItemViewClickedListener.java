package minefarts.smarttube.leanback.widget;

/**
 * Interface for receiving notification when an item view holder is clicked.
 */
public interface BaseOnItemViewClickedListener<T> {

    /**
     * Called when an item inside a row gets clicked.
     * @param itemViewHolder The view holder of the item that is clicked.
     * @param item The item that is currently selected.
     * @param rowViewHolder The view holder of the row which the clicked item belongs to.
     * @param row The row which the clicked item belongs to.
     */
    void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                              RowPresenter.ViewHolder rowViewHolder, T row);
}
