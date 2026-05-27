package org.apache.commons.io.input;

import static org.apache.commons.io.IOUtils.EOF;

import java.io.IOException;
import java.io.Reader;

/**
 * Closed reader. This reader returns EOF to all attempts to read something from it.
 * <p>
 * Typically uses of this class include testing for corner cases in methods that accept readers and acting as a sentinel
 * value instead of a {@code null} reader.
 * </p>
 *
 * @since 2.7
 */
public class ClosedReader extends Reader {

    /**
     * A singleton.
     */
    public static final ClosedReader CLOSED_READER = new ClosedReader();

    /**
     * Returns -1 to indicate that the stream is closed.
     *
     * @param cbuf ignored
     * @param off  ignored
     * @param len  ignored
     * @return always -1
     */
    @Override
    public int read(final char[] cbuf, final int off, final int len) {
        return EOF;
    }

    @Override
    public void close() throws IOException {
        // noop
    }

}
