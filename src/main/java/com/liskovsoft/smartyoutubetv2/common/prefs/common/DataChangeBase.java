package com.liskovsoft.smartyoutubetv2.common.prefs.common;

/**
 * Small observable base for data-change notifications.
 * Allows registering weak listeners and dispatching onDataChange events.
 */
public abstract class DataChangeBase {
    public interface OnDataChange {
        void onDataChange();
    }

    private final WeakHashSet<OnDataChange> mOnChangeList = new WeakHashSet<>();

    public final void setOnChange(OnDataChange callback) {
        mOnChangeList.add(callback);
    }

    public final void removeOnChange(OnDataChange callback) {
        mOnChangeList.remove(callback);
    }

    public final void onDataChange() {
        mOnChangeList.forEach(OnDataChange::onDataChange);
    }
}
