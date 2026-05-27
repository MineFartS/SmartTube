package org.apache.commons.io.input;

import static org.apache.commons.io.IOUtils.EOF;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * Data written to this stream is forwarded to a stream that has been associated with this thread.
 */
public class DemuxInputStream extends InputStream {
    private final InheritableThreadLocal<InputStream> inputStream = new InheritableThreadLocal<>();

    /**
     * Binds the specified stream to the current thread.
     *
     * @param input the stream to bind
     * @return the InputStream that was previously active
     */
    public InputStream bindStream(final InputStream input) {
        final InputStream oldValue = inputStream.get();
        inputStream.set(input);
        return oldValue;
    }

    /**
     * Closes stream associated with current thread.
     *
     * @throws IOException if an error occurs
     */
    @Override
    public void close() throws IOException {
        IOUtils.close(inputStream.get());
    }

    /**
     * Reads byte from stream associated with current thread.
     *
     * @return the byte read from stream
     * @throws IOException if an error occurs
     */
    @Override
    public int read() throws IOException {
        final InputStream input = inputStream.get();
        if (null != input) {
            return input.read();
        }
        return EOF;
    }
}
