
package androidx.leanback.widget;

/**
 * Interface for receiving notification when an {@link Action} is clicked.
 */
public interface OnActionClickedListener {

    /**
     * Callback fired when the host fragment receives an action.
     */
    void onActionClicked(Action action);
}
