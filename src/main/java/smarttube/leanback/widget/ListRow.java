package minefarts.smarttube.leanback.widget;

/**
 * A {@link Row} composed of a optional {@link HeaderItem}, and an {@link ObjectAdapter}
 * describing the items in the list.
 */
public class ListRow extends Row {
    private final ObjectAdapter mAdapter;
    private CharSequence mContentDescription;

    /**
     * Returns the {@link ObjectAdapter} that represents a list of objects.
     */
    public final ObjectAdapter getAdapter() {
        return mAdapter;
    }

    public ListRow(HeaderItem header, ObjectAdapter adapter) {
        super(header);
        mAdapter = adapter;
        verify();
    }

    public ListRow(long id, HeaderItem header, ObjectAdapter adapter) {
        super(id, header);
        mAdapter = adapter;
        verify();
    }

    public ListRow(ObjectAdapter adapter) {
        super();
        mAdapter = adapter;
        verify();
    }

    private void verify() {
        if (mAdapter == null) {
            throw new IllegalArgumentException("ObjectAdapter cannot be null");
        }
    }

    /**
     * Returns content description for the ListRow.  By default it returns
     * {@link HeaderItem#getContentDescription()} or {@link HeaderItem#getName()},
     * unless {@link #setContentDescription(CharSequence)} was explicitly called.
     *
     * @return Content description for the ListRow.
     */
    public CharSequence getContentDescription() {
        if (mContentDescription != null) {
            return mContentDescription;
        }
        final HeaderItem headerItem = getHeaderItem();
        if (headerItem != null) {
            CharSequence contentDescription = headerItem.getContentDescription();
            if (contentDescription != null) {
                return contentDescription;
            }
            return headerItem.getName();
        }
        return null;
    }

    /**
     * Explicitly set content description for the ListRow, {@link #getContentDescription()} will
     * ignore values from HeaderItem.
     *
     * @param contentDescription Content description sets on the ListRow.
     */
    public void setContentDescription(CharSequence contentDescription) {
        mContentDescription = contentDescription;
    }
}
