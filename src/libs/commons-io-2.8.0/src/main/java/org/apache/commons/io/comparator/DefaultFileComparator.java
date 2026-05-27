package org.apache.commons.io.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Compare two files using the <b>default</b> {@link File#compareTo(File)} method.
 * <p>
 * This comparator can be used to sort lists or arrays of files
 * by using the default file comparison.
 * <p>
 * Example of sorting a list of files using the
 * {@link #DEFAULT_COMPARATOR} singleton instance:
 * <pre>
 *       List&lt;File&gt; list = ...
 *       ((AbstractFileComparator) DefaultFileComparator.DEFAULT_COMPARATOR).sort(list);
 * </pre>
 * <p>
 * Example of doing a <i>reverse</i> sort of an array of files using the
 * {@link #DEFAULT_REVERSE} singleton instance:
 * <pre>
 *       File[] array = ...
 *       ((AbstractFileComparator) DefaultFileComparator.DEFAULT_REVERSE).sort(array);
 * </pre>
 * <p>
 *
 * @since 1.4
 */
public class DefaultFileComparator extends AbstractFileComparator implements Serializable {

    private static final long serialVersionUID = 3260141861365313518L;

    /** Singleton default comparator instance */
    public static final Comparator<File> DEFAULT_COMPARATOR = new DefaultFileComparator();

    /** Singleton reverse default comparator instance */
    public static final Comparator<File> DEFAULT_REVERSE = new ReverseFileComparator(DEFAULT_COMPARATOR);

    /**
     * Compare the two files using the {@link File#compareTo(File)} method.
     *
     * @param file1 The first file to compare
     * @param file2 The second file to compare
     * @return the result of calling file1's
     * {@link File#compareTo(File)} with file2 as the parameter.
     */
    @Override
    public int compare(final File file1, final File file2) {
        return file1.compareTo(file2);
    }
}
