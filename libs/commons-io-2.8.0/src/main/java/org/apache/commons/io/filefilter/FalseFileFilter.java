
package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;

/**
 * A file filter that always returns false.
 *
 * @since 1.0
 *
 *
 * @see FileFilterUtils#falseFileFilter()
 */
public class FalseFileFilter implements IOFileFilter, Serializable {

    private static final long serialVersionUID = 6210271677940926200L;
    /**
     * Singleton instance of false filter.
     * @since 1.3
     */
    public static final IOFileFilter FALSE = new FalseFileFilter();
    /**
     * Singleton instance of false filter.
     * Please use the identical FalseFileFilter.FALSE constant.
     * The new name is more JDK 1.5 friendly as it doesn't clash with other
     * values when using static imports.
     */
    public static final IOFileFilter INSTANCE = FALSE;

    /**
     * Restrictive constructor.
     */
    protected FalseFileFilter() {
    }

    /**
     * Returns false.
     *
     * @param file  the file to check (ignored)
     * @return false
     */
    @Override
    public boolean accept(final File file) {
        return false;
    }

    /**
     * Returns false.
     *
     * @param dir  the directory to check (ignored)
     * @param name  the file name (ignored)
     * @return false
     */
    @Override
    public boolean accept(final File dir, final String name) {
        return false;
    }

}
