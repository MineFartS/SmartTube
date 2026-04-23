
package org.apache.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes all data to the famous <b>/dev/null</b>.
 * <p>
 * This output stream has no destination (file/socket etc.) and all bytes written to it are ignored and lost.
 * </p>
 */
public class NullOutputStream extends OutputStream {

    /**
     * Deprecated in favor of {@link #NULL_OUTPUT_STREAM}.
     *
     * TODO: Will be private in 3.0.
     *
     * @deprecated Use {@link #NULL_OUTPUT_STREAM}.
     */
    @Deprecated
    public NullOutputStream() {
        super();
    }

    /**
     * A singleton.
     */
    public static final NullOutputStream NULL_OUTPUT_STREAM = new NullOutputStream();

    /**
     * Does nothing - output to <code>/dev/null</code>.
     *
     * @param b The bytes to write
     * @param off The start offset
     * @param len The number of bytes to write
     */
    @Override
    public void write(final byte[] b, final int off, final int len) {
        // To /dev/null
    }

    /**
     * Does nothing - output to <code>/dev/null</code>.
     *
     * @param b The byte to write
     */
    @Override
    public void write(final int b) {
        // To /dev/null
    }

    /**
     * Does nothing - output to <code>/dev/null</code>.
     *
     * @param b The bytes to write
     * @throws IOException never
     */
    @Override
    public void write(final byte[] b) throws IOException {
        // To /dev/null
    }

}
