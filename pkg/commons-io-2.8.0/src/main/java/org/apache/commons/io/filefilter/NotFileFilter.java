
package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;

/**
 * This filter produces a logical NOT of the filters specified.
 *
 * @since 1.0
 *
 * @see FileFilterUtils#notFileFilter(IOFileFilter)
 */
public class NotFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = 6131563330944994230L;
    /** The filter */
    private final IOFileFilter filter;

    /**
     * Constructs a new file filter that NOTs the result of another filter.
     *
     * @param filter  the filter, must not be null
     * @throws IllegalArgumentException if the filter is null
     */
    public NotFileFilter(final IOFileFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("The filter must not be null");
        }
        this.filter = filter;
    }

    /**
     * Returns the logical NOT of the underlying filter's return value for the same File.
     *
     * @param file  the File to check
     * @return true if the filter returns false
     */
    @Override
    public boolean accept(final File file) {
        return ! filter.accept(file);
    }

    /**
     * Returns the logical NOT of the underlying filter's return value for the same arguments.
     *
     * @param file  the File directory
     * @param name  the file name
     * @return true if the filter returns false
     */
    @Override
    public boolean accept(final File file, final String name) {
        return ! filter.accept(file, name);
    }

    /**
     * Provide a String representation of this file filter.
     *
     * @return a String representation
     */
    @Override
    public String toString() {
        return super.toString() + "(" + filter.toString()  + ")";
    }

}
