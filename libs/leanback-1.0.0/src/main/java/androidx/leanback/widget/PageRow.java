
package androidx.leanback.widget;

/**
 * Used to represent content spanning full page.
 */
public class PageRow extends Row {

    public PageRow(HeaderItem headerItem) {
        super(headerItem);
    }

    @Override
    final public boolean isRenderedAsRowView() {
        return false;
    }
}
