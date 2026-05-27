package minefarts.smarttube.io.filefilter;

import java.io.File;

/**
 * An abstract class which implements the Java FileFilter and FilenameFilter
 * interfaces via the IOFileFilter interface.
 * <p>
 * Note that a subclass <b>must</b> override one of the accept methods,
 * otherwise your class will infinitely loop.
 *
 * @since 1.0
 *
 */
public abstract class AbstractFileFilter implements IOFileFilter {

    /**
     * Checks to see if the File should be accepted by this filter.
     *
     * @param file  the File to check
     * @return true if this file matches the test
     */
    @Override
    public boolean accept(final File file) {
        return accept(file.getParentFile(), file.getName());
    }

    /**
     * Checks to see if the File should be accepted by this filter.
     *
     * @param dir  the directory File to check
     * @param name  the file name within the directory to check
     * @return true if this file matches the test
     */
    @Override
    public boolean accept(final File dir, final String name) {
        return accept(new File(dir, name));
    }

    /**
     * Provide a String representation of this file filter.
     *
     * @return a String representation
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
