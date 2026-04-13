
package org.apache.commons.io.output;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream which breaks larger output blocks into chunks.
 * Native code may need to copy the input array; if the write buffer
 * is very large this can cause OOME.
 *
 * @since 2.5
 */
public class ChunkedOutputStream extends FilterOutputStream {

    /**
     * The default chunk size to use, i.e. {@value} bytes.
     */
    private static final int DEFAULT_CHUNK_SIZE = 1024 * 4;

    /**
     * The maximum chunk size to us when writing data arrays
     */
    private final int chunkSize;

    /**
     * Creates a new stream that uses the specified chunk size.
     *
     * @param stream the stream to wrap
     * @param chunkSize the chunk size to use; must be a positive number.
     * @throws IllegalArgumentException if the chunk size is &lt;= 0
     */
    public ChunkedOutputStream(final OutputStream stream, final int chunkSize) {
       super(stream);
       if (chunkSize <= 0) {
           throw new IllegalArgumentException();
       }
       this.chunkSize = chunkSize;
    }

    /**
     * Creates a new stream that uses a chunk size of {@link #DEFAULT_CHUNK_SIZE}.
     *
     * @param stream the stream to wrap
     */
    public ChunkedOutputStream(final OutputStream stream) {
        this(stream, DEFAULT_CHUNK_SIZE);
    }

    /**
     * Writes the data buffer in chunks to the underlying stream
     *
     * @param data the data to write
     * @param srcOffset the offset
     * @param length the length of data to write
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(final byte[] data, final int srcOffset, final int length) throws IOException {
        int bytes = length;
        int dstOffset = srcOffset;
        while(bytes > 0) {
            final int chunk = Math.min(bytes, chunkSize);
            out.write(data, dstOffset, chunk);
            bytes -= chunk;
            dstOffset += chunk;
        }
    }

}
