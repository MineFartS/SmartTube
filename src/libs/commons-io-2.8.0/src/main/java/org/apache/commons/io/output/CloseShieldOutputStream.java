package org.apache.commons.io.output;

import java.io.OutputStream;

/**
 * Proxy stream that prevents the underlying output stream from being closed.
 * <p>
 * This class is typically used in cases where an output stream needs to be
 * passed to a component that wants to explicitly close the stream even if
 * other components would still use the stream for output.
 * </p>
 *
 * @since 1.4
 */
public class CloseShieldOutputStream extends ProxyOutputStream {

    /**
     * Creates a proxy that shields the given output stream from being
     * closed.
     *
     * @param out underlying output stream
     */
    public CloseShieldOutputStream(final OutputStream out) {
        super(out);
    }

    /**
     * Replaces the underlying output stream with a {@link ClosedOutputStream}
     * sentinel. The original output stream will remain open, but this proxy
     * will appear closed.
     */
    @Override
    public void close() {
        out = ClosedOutputStream.CLOSED_OUTPUT_STREAM;
    }

}
