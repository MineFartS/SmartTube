package minefarts.smarttube.io.input.buffer;

import minefarts.smarttube.io.mod.Objects;

import java.io.IOException;
import java.io.InputStream;


/**
 * Implementation of a buffered input stream, which allows to peek into
 * the buffers first bytes. This comes in handy when manually implementing
 * scanners, lexers, parsers, or the like.
 */
public class PeekableInputStream extends CircularBufferInputStream {

    /**
     * Creates a new instance, which filters the given input stream, and
     * uses the given buffer size.
     *
     * @param inputStream         The input stream, which is being buffered.
     * @param bufferSize The size of the {@link CircularByteBuffer}, which is
     *                    used internally.
     */
    public PeekableInputStream(final InputStream inputStream, final int bufferSize) {
        super(inputStream, bufferSize);
    }

    /**
     * Creates a new instance, which filters the given input stream, and
     * uses a reasonable default buffer size (8192).
     *
     * @param inputStream The input stream, which is being buffered.
     */
    public PeekableInputStream(final InputStream inputStream) {
        super(inputStream);
    }

    /**
     * Returns, whether the next bytes in the buffer are as given by
     * {@code sourceBuffer}. This is equivalent to {@link #peek(byte[], int, int)}
     * with {@code offset} == 0, and {@code length} == {@code sourceBuffer.length}
     *
     * @param sourceBuffer the buffer to compare against
     * @return true if the next bytes are as given
     * @throws IOException Refilling the buffer failed.
     */
    public boolean peek(final byte[] sourceBuffer) throws IOException {
        Objects.requireNonNull(sourceBuffer, "Buffer");
        if (sourceBuffer.length > bufferSize) {
            throw new IllegalArgumentException("Peek request size of " + sourceBuffer.length
                    + " bytes exceeds buffer size of " + bufferSize + " bytes");
        }
        if (buffer.getCurrentNumberOfBytes() < sourceBuffer.length) {
            fillBuffer();
        }
        return buffer.peek(sourceBuffer, 0, sourceBuffer.length);
    }

    /**
     * Returns, whether the next bytes in the buffer are as given by
     * {@code sourceBuffer}, {code offset}, and {@code length}.
     *
     * @param sourceBuffer the buffer to compare against
     * @param offset the start offset
     * @param length the length to compare
     * @return true if the next bytes in the buffer are as given
     * @throws IOException if there is a problem calling fillBuffer()
     */
    public boolean peek(final byte[] sourceBuffer, final int offset, final int length) throws IOException {
        Objects.requireNonNull(sourceBuffer, "Buffer");
        if (sourceBuffer.length > bufferSize) {
            throw new IllegalArgumentException("Peek request size of " + sourceBuffer.length
                    + " bytes exceeds buffer size of " + bufferSize + " bytes");
        }
        if (buffer.getCurrentNumberOfBytes() < sourceBuffer.length) {
            fillBuffer();
        }
        return buffer.peek(sourceBuffer, offset, length);
    }
}
