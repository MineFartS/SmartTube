package org.apache.commons.io.output;

import java.io.IOException;
import java.io.Writer;

/**
 * Broken writer. This writer always throws an {@link IOException} from
 * all {@link Writer} methods.
 * <p>
 * This class is mostly useful for testing error handling in code that uses a
 * writer.
 *
 * @since 2.0
 */
public class BrokenWriter extends Writer {

    /**
     * The exception that is thrown by all methods of this class.
     */
    private final IOException exception;

    /**
     * Creates a new writer that always throws the given exception.
     *
     * @param exception the exception to be thrown
     */
    public BrokenWriter(final IOException exception) {
        this.exception = exception;
    }

    /**
     * Creates a new writer that always throws an {@link IOException}
     */
    public BrokenWriter() {
        this(new IOException("Broken writer"));
    }

    /**
     * Throws the configured exception.
     *
     * @param cbuf ignored
     * @param off ignored
     * @param len ignored
     * @throws IOException always thrown
     */
    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
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
