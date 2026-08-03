package minefarts.smarttube.io.input;

import minefarts.smarttube.io.mod.Objects;

import static minefarts.smarttube.io.IOUtils.EOF;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Provides the contents of multiple Readers in sequence.
 *
 * @since 2.7
 */
public class SequenceReader extends Reader {

    private Reader reader;
    private Iterator<? extends Reader> readers;

    /**
     * Construct a new instance with readers
     *
     * @param readers the readers to read
     */
    public SequenceReader(final Iterable<? extends Reader> readers) {
        this.readers = Objects.requireNonNull(readers, "readers").iterator();
        this.reader = nextReader();
    }

    /**
     * Construct a new instance with readers
     *
     * @param readers the readers to read
     */
    public SequenceReader(final Reader... readers) {
        this(Arrays.asList(readers));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Reader#close()
     */
    @Override
    public void close() throws IOException {
        this.readers = null;
        this.reader = null;
    }

    /**
     * Returns the next available reader or null if done.
     *
     * @return the next available reader or null
     */
    private Reader nextReader() {
        return this.readers.hasNext() ? this.readers.next() : null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Reader#read(char[], int, int)
     */
    @Override
    public int read() throws IOException {
        int c = EOF;
        while (reader != null) {
            c = reader.read();
            if (c == EOF) {
                reader = nextReader();
            } else {
                break;
            }
        }
        return c;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Reader#read()
     */
    @Override
    public int read(final char[] cbuf, int off, int len) throws IOException {
        Objects.requireNonNull(cbuf, "cbuf");
        if (len < 0 || off < 0 || off + len > cbuf.length) {
            throw new IndexOutOfBoundsException("Array Size=" + cbuf.length + ", offset=" + off + ", length=" + len);
        }
        int count = 0;
        while (reader != null) {
            final int readLen = reader.read(cbuf, off, len);
            if (readLen == EOF) {
                reader = nextReader();
            } else {
                count += readLen;
                off += readLen;
                len -= readLen;
                if (len <= 0) {
                    break;
                }
            }
        }
        if (count > 0) {
            return count;
        }
        return EOF;
    }
}
