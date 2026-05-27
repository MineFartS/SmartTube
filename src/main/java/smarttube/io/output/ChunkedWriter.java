package minefarts.smarttube.io.output;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Writer which breaks larger output blocks into chunks.
 * Native code may need to copy the input array; if the write buffer
 * is very large this can cause OOME.
 *
 * @since 2.5
 */
public class ChunkedWriter extends FilterWriter {

    /**
     * The default chunk size to use, i.e. {@value} bytes.
     */
    private static final int DEFAULT_CHUNK_SIZE = 1024 * 4;

    /**
     * The maximum chunk size to us when writing data arrays
     */
    private final int chunkSize;

    /**
     * Creates a new writer that uses the specified chunk size.
     *
     * @param writer the writer to wrap
     * @param chunkSize the chunk size to use; must be a positive number.
     * @throws IllegalArgumentException if the chunk size is &lt;= 0
     */
    public ChunkedWriter(final Writer writer, final int chunkSize) {
       super(writer);
       if (chunkSize <= 0) {
           throw new IllegalArgumentException();
       }
       this.chunkSize = chunkSize;
    }

    /**
     * Creates a new writer that uses a chunk size of {@link #DEFAULT_CHUNK_SIZE}
     * @param writer the writer to wrap
     */
    public ChunkedWriter(final Writer writer) {
        this(writer, DEFAULT_CHUNK_SIZE);
    }

    /**
     * writes the data buffer in chunks to the underlying writer
     * @param data The data
     * @param srcOffset the offset
     * @param length the number of bytes to write
     *
     * @throws IOException upon error
     */
    @Override
    public void write(final char[] data, final int srcOffset, final int length) throws IOException {
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
