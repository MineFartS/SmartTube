package minefarts.smarttube.io.output;

import java.io.Writer;

/**
 * Proxy stream that prevents the underlying writer from being closed.
 * <p>
 * This class is typically used in cases where a writer needs to be passed to a component that wants to explicitly close
 * the writer even if other components would still use the writer for output.
 * </p>
 *
 * @since 2.7
 */
public class CloseShieldWriter extends ProxyWriter {

    /**
     * Creates a proxy that shields the given writer from being closed.
     *
     * @param out underlying writer
     */
    public CloseShieldWriter(final Writer out) {
        super(out);
    }

    /**
     * Replaces the underlying writer with a {@link ClosedWriter} sentinel. The original writer will remain open, but
     * this proxy will appear closed.
     */
    @Override
    public void close() {
        out = ClosedWriter.CLOSED_WRITER;
    }

}
