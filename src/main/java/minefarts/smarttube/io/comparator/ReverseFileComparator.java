package minefarts.smarttube.io.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Reverses the result of comparing two {@link File} objects using the delegate {@link Comparator}.
 *
 * @since 1.4
 */
class ReverseFileComparator extends AbstractFileComparator implements Serializable {

    private static final long serialVersionUID = -4808255005272229056L;
    private final Comparator<File> delegate;

    /**
     * Construct an instance with the specified delegate {@link Comparator}.
     *
     * @param delegate The comparator to delegate to.
     */
    public ReverseFileComparator(final Comparator<File> delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate comparator is missing");
        }
        this.delegate = delegate;
    }

    /**
     * Compares using the delegate Comparator, reversing the result.
     *
     * @param file1 The first file to compare.
     * @param file2 The second file to compare.
     * @return the result from the delegate {@link Comparator#compare(Object, Object)} reversing the value (i.e.
     *         positive becomes negative and vice versa).
     */
    @Override
    public int compare(final File file1, final File file2) {
        return delegate.compare(file2, file1); // parameters switched round
    }

    /**
     * Returns the String representation of this file comparator.
     *
     * @return String representation of this file comparator.
     */
    @Override
    public String toString() {
        return super.toString() + "[" + delegate.toString() + "]";
    }

}
