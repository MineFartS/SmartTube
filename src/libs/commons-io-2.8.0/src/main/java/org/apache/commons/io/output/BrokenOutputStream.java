package org.apache.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Broken output stream. This stream always throws an {@link IOException} from
 * all {@link OutputStream} methods.
 * <p>
 * This class is mostly useful for testing error handling in code that uses an
 * output stream.
 *
 * @since 2.0
 */
public class BrokenOutputStream extends OutputStream {

    /**
     * The exception that is thrown by all methods of this class.
     */
    private final IOException exception;

    /**
     * Creates a new stream that always throws the given exception.
     *
     * @param exception the exception to be thrown
     */
    public BrokenOutputStream(final IOException exception) {
        this.exception = exception;
    }

    /**
     * Creates a new stream that always throws an {@link IOException}
     */
    public BrokenOutputStream() {
        this(new IOException("Broken output stream"));
    }

    /**
     * Throws the configured exception.
     *
     * @param b ignored
     * @throws IOException always thrown
     */
    @Override
    public void write(final int b) throws IOException {
        throw exception;
    }

    /**
     * Throws the configured exception.
     *
     * @throws IOException always thrown
     */
    @Override
    public void flush() throws IOException {
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
