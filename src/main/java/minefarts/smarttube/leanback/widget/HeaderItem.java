package minefarts.smarttube.leanback.widget;

import static minefarts.smarttube.leanback.widget.ObjectAdapter.NO_ID;

/**
 * A header item describes the metadata of a {@link Row}, such as a category
 * of media items.  May be subclassed to add more information.
 */
public class HeaderItem {

    private final long mId;
    private final String mName;
    private CharSequence mDescription;
    private CharSequence mContentDescription;

    /**
     * Create a header item.  All fields are optional.
     */
    public HeaderItem(long id, String name) {
        mId = id;
        mName = name;
    }

    /**
     * Create a header item.
     */
    public HeaderItem(String name) {
        this(NO_ID, name);
    }

    /**
     * Returns a unique identifier for this item.
     */
    public final long getId() {
        return mId;
    }

    /**
     * Returns the name of this header item.
     */
    public final String getName() {
        return mName;
    }

    /**
     * Returns optional content description for the HeaderItem.  When it is null, {@link #getName()}
     * should be used for the content description.
     * @return Content description for the HeaderItem.
     */
    public CharSequence getContentDescription() {
        return mContentDescription;
    }

    /**
     * Sets optional content description for the HeaderItem.
     * @param contentDescription Content description sets on the HeaderItem.
     */
    public void setContentDescription(CharSequence contentDescription) {
        mContentDescription = contentDescription;
    }

    /**
     * Sets the description for the current header item. This will be visible when
     * the row receives focus.
     */
    public void setDescription(CharSequence description) {
        this.mDescription = description;
    }

    /**
     * Returns the description for the current row.
     */
    public CharSequence getDescription() {
        return mDescription;
    }
}
