
package org.apache.commons.io.input;

import java.io.InputStream;

/**
 * Proxy stream that prevents the underlying input stream from being closed.
 * <p>
 * This class is typically used in cases where an input stream needs to be
 * passed to a component that wants to explicitly close the stream even if
 * more input would still be available to other components.
 * </p>
 *
 * @since 1.4
 */
public class CloseShieldInputStream extends ProxyInputStream {

    /**
     * Creates a proxy that shields the given input stream from being
     * closed.
     *
     * @param in underlying input stream
     */
    public CloseShieldInputStream(final InputStream in) {
        super(in);
    }

    /**
     * Replaces the underlying input stream with a {@link ClosedInputStream}
     * sentinel. The original input stream will remain open, but this proxy
     * will appear closed.
     */
    @Override
    public void close() {
        in = ClosedInputStream.CLOSED_INPUT_STREAM;
    }

}
