package minefarts.smarttube.io.output;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Throws an exception on all attempts to write to the stream.
 * <p>
 * Typically uses of this class include testing for corner cases in methods that accept an output stream and acting as a
 * sentinel value instead of a {@code null} output stream.
 * </p>
 *
 * @since 1.4
 */
public class ClosedOutputStream extends OutputStream {

    /**
     * A singleton.
     */
    public static final ClosedOutputStream CLOSED_OUTPUT_STREAM = new ClosedOutputStream();

    /**
     * Throws an {@link IOException} to indicate that the stream is closed.
     *
     * @param b ignored
     * @throws IOException always thrown
     */
    @Override
    public void write(final int b) throws IOException {
        throw new IOException("write(" + b + ") failed: stream is closed");
    }

    /**
     * Throws an {@link IOException} to indicate that the stream is closed.
     *
     * @throws IOException always thrown
     */
    @Override
    public void flush() throws IOException {
        throw new IOException("flush() failed: stream is closed");
    }
}
