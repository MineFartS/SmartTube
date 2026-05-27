package minefarts.smarttube.io.input;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * An {@link InputStream} that infinitely repeats the provided bytes.
 * <p>
 * Closing this input stream has no effect. The methods in this class can be called after the stream has been closed
 * without generating an {@link IOException}.
 * </p>
 *
 * @since 2.6
 */
public class InfiniteCircularInputStream extends CircularInputStream {

    /**
     * Creates an instance from the specified array of bytes.
     *
     * @param repeatContent Input buffer to be repeated this buffer is not copied.
     */
    public InfiniteCircularInputStream(final byte[] repeatContent) {
        // A negative number means an infinite target count.
        super(repeatContent, -1);
    }

}
