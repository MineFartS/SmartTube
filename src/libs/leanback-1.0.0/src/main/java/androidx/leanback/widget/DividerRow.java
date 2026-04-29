
package androidx.leanback.widget;

/**
 * Used to represent divider in HeadersFragment.
 */
public class DividerRow extends Row {

    public DividerRow() {
    }

    @Override
    final public boolean isRenderedAsRowView() {
        return false;
    }
}
