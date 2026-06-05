package minefarts.smarttube.io.filefilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * An interface which brings the FileFilter and FilenameFilter
 * interfaces together.
 *
 * @since 1.0
 *
 */
public interface IOFileFilter extends FileFilter, FilenameFilter {

    /**
     * An empty String array.
     */
    String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Checks to see if the File should be accepted by this filter.
     * <p>
     * Defined in {@link java.io.FileFilter}.
     *
     * @param file  the File to check
     * @return true if this file matches the test
     */
    @Override
    boolean accept(File file);

    /**
     * Checks to see if the File should be accepted by this filter.
     * <p>
     * Defined in {@link java.io.FilenameFilter}.
     *
     * @param dir  the directory File to check
     * @param name  the file name within the directory to check
     * @return true if this file matches the test
     */
    @Override
    boolean accept(File dir, String name);

}
