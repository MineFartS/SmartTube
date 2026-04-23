
package org.apache.commons.io.input;

import java.io.Reader;

/**
 * Proxy stream that prevents the underlying reader from being closed.
 * <p>
 * This class is typically used in cases where a reader needs to be
 * passed to a component that wants to explicitly close the reader even if
 * more input would still be available to other components.
 * </p>
 *
 * @since 2.7
 */
public class CloseShieldReader extends ProxyReader {

    /**
     * Creates a proxy that shields the given reader from being
     * closed.
     *
     * @param in underlying reader
     */
    public CloseShieldReader(final Reader in) {
        super(in);
    }

    /**
     * Replaces the underlying reader with a {@link ClosedReader}
     * sentinel. The original reader will remain open, but this proxy
     * will appear closed.
     */
    @Override
    public void close() {
        in = ClosedReader.CLOSED_READER;
    }

}
