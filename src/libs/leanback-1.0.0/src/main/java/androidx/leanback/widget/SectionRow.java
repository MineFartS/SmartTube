
package androidx.leanback.widget;

/**
 * Used to represent section item in HeadersFragment.  Unlike a normal Row, it's not focusable.
 */
public class SectionRow extends Row {

    public SectionRow(HeaderItem headerItem) {
        super(headerItem);
    }

    public SectionRow(long id, String name) {
        super(new HeaderItem(id, name));
    }

    public SectionRow(String name) {
        super(new HeaderItem(name));
    }

    @Override
    final public boolean isRenderedAsRowView() {
        return false;
    }
}
