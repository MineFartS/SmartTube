package androidx.leanback.database;

import android.database.Cursor;

/**
 * Abstract class used to convert the current {@link Cursor} row to a single
 * object.
 */
public abstract class CursorMapper {

    private Cursor mCursor;

    /**
     * Called once when the associated {@link Cursor} is changed. A subclass
     * should bind column indexes to column names in this method. This method is
     * not intended to be called outside of CursorMapper.
     */
    protected abstract void bindColumns(Cursor cursor);

    /**
     * A subclass should implement this method to create a single object using
     * binding information. This method is not intended to be called
     * outside of CursorMapper.
     */
    protected abstract Object bind(Cursor cursor);

    /**
     * Convert a {@link Cursor} at its current position to an Object.
     */
    public Object convert(Cursor cursor) {
        if (cursor != mCursor) {
            mCursor = cursor;
            bindColumns(mCursor);
        }
        return bind(mCursor);
    }
}
