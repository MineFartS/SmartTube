package minefarts.smarttube.io.filefilter;

import java.io.File;
import java.io.Serializable;

/**
 * A file filter that always returns true.
 *
 * @since 1.0
 *
 * @see FileFilterUtils#trueFileFilter()
 */
public class TrueFileFilter implements IOFileFilter, Serializable {

    private static final long serialVersionUID = 8782512160909720199L;
    /**
     * Singleton instance of true filter.
     * @since 1.3
     */
    public static final IOFileFilter TRUE = new TrueFileFilter();
    /**
     * Singleton instance of true filter.
     * Please use the identical TrueFileFilter.TRUE constant.
     * The new name is more JDK 1.5 friendly as it doesn't clash with other
     * values when using static imports.
     */
    public static final IOFileFilter INSTANCE = TRUE;

    /**
     * Restrictive constructor.
     */
    protected TrueFileFilter() {
    }

    /**
     * Returns true.
     *
     * @param file  the file to check (ignored)
     * @return true
     */
    @Override
    public boolean accept(final File file) {
        return true;
    }

    /**
     * Returns true.
     *
     * @param dir  the directory to check (ignored)
     * @param name  the file name (ignored)
     * @return true
     */
    @Override
    public boolean accept(final File dir, final String name) {
        return true;
    }

}
