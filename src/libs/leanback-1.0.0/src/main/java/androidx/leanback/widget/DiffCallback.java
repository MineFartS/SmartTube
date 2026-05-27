package androidx.leanback.widget;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

/**
 * Callback that informs {@link ArrayObjectAdapter} how to compute list updates when using
 * {@link DiffUtil} in {@link ArrayObjectAdapter#setItems(List,
 * DiffCallback)} method.
 * <p>
 * The {@link ArrayObjectAdapter#setItems(List,
 * DiffCallback)} method will pass items from different
 * lists to this callback in order to implement
 * the {@link DiffUtil.Callback} it uses to compute differences between
 * lists.
 *
 * @param <Value> Type of items to compare.
 */
public abstract class DiffCallback<Value> {
    /**
     * Called to decide whether two objects represent the same item.
     *
     * @param oldItem The item in the old list.
     * @param newItem The item in the new list.
     * @return True if the two items represent the same object or false if they are different.
     * @see DiffUtil.Callback#areItemsTheSame(int, int)
     */
    public abstract boolean areItemsTheSame(@NonNull Value oldItem, @NonNull Value newItem);

    /**
     * Called to decide whether two items have the same data. This information is used to detect if
     * the contents of an item have changed.
     *
     * @param oldItem The item in the old list.
     * @param newItem The item in the new list.
     * @return True if the contents of the items are the same or false if they are different.
     * @see DiffUtil.Callback#areContentsTheSame(int, int)
     */
    public abstract boolean areContentsTheSame(@NonNull Value oldItem, @NonNull Value newItem);

    /**
     * Called to get a change payload between an old and new version of an item.
     *
     * @see DiffUtil.Callback#getChangePayload(int, int)
     */
    @SuppressWarnings("WeakerAccess")
    public Object getChangePayload(@NonNull Value oldItem, @NonNull Value newItem) {
        return null;
    }
}
