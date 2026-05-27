package org.apache.commons.io;

import java.io.IOException;

/**
 * A IOException associated with a source index.
 *
 * @since 2.7
 */
public class IOIndexedException extends IOException {

    private static final long serialVersionUID = 1L;
    private final int index;

    /**
     * Creates a new exception.
     *
     * @param index index of this exception.
     * @param cause cause exceptions.
     */
    public IOIndexedException(final int index, final Throwable cause) {
        super(toMessage(index, cause), cause);
        this.index = index;
    }

    /**
     * Converts input to a suitable String for exception message.
     *
     * @param index An index into a source collection.
     * @param cause A cause.
     * @return A message.
     */
    protected static String toMessage(final int index, final Throwable cause) {
        // Letting index be any int
        final String unspecified = "Null";
        final String name = cause == null ? unspecified : cause.getClass().getSimpleName();
        final String msg = cause == null ? unspecified : cause.getMessage();
        return String.format("%s #%,d: %s", name, index, msg);
    }

    /**
     * The index of this exception.
     *
     * @return index of this exception.
     */
    public int getIndex() {
        return index;
    }

}
