package org.apache.commons.io.input;

import java.io.IOException;
import java.io.Reader;

/**
 * Broken reader. This reader always throws an {@link IOException} from
 * all the {@link Reader} methods where the exception is declared.
 * <p>
 * This class is mostly useful for testing error handling in code that uses a
 * reader.
 * </p>
 *
 * @since 2.7
 */
public class BrokenReader extends Reader {

    /**
     * The exception that is thrown by all methods of this class.
     */
    private final IOException exception;

    /**
     * Creates a new reader that always throws the given exception.
     *
     * @param exception the exception to be thrown
     */
    public BrokenReader(final IOException exception) {
        this.exception = exception;
    }

    /**
     * Creates a new reader that always throws an {@link IOException}
     */
    public BrokenReader() {
        this(new IOException("Broken reader"));
    }

    /**
     * Throws the configured exception.
     *
     * @param cbuf ignored
     * @param off ignored
     * @param len ignored
     * @return nothing
     * @throws IOException always thrown
     */
    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        throw exception;
    }

    /**
     * Throws the configured exception.
     *
     * @param n ignored
     * @return nothing
     * @throws IOException always thrown
     */
    @Override
    public long skip(final long n) throws IOException {
        throw exception;
    }

    /**
     * Throws the configured exception.
     *
     * @return nothing
     * @throws IOException always thrown
     */
    @Override
    public boolean ready() throws IOException {
        throw exception;
    }

    /**
     * Throws the configured exception.
     *
     * @param readAheadLimit ignored
     * @throws IOException always thrown
     */
    @Override
    public void mark(final int readAheadLimit) throws IOException {
        throw exception;
    }

    /**
     * Throws the configured exception.
     *
     * @throws IOException always thrown
     */
    @Override
    public synchronized void reset() throws IOException {
        throw exception;
    }

    /**
     * Throws the configured exception.
     *
     * @throws IOException always thrown
     */
    @Override
    public void close() throws IOException {
        throw exception;
    }

}
