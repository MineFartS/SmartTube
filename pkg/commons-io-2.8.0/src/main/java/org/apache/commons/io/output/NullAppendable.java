

package org.apache.commons.io.output;

import java.io.IOException;

/**
 * Appends all data to the famous <b>/dev/null</b>.
 * <p>
 * This Appendable has no destination (file/socket etc.) and all characters written to it are ignored and lost.
 * </p>
 *
 * @since 2.8.0
 */
public class NullAppendable implements Appendable {

    /**
     * A singleton.
     */
    public static final NullAppendable INSTANCE = new NullAppendable();

    /** Use the singleton. */
    private NullAppendable() {
        // no instances.
    }

    @Override
    public Appendable append(final char c) throws IOException {
        return this;
    }

    @Override
    public Appendable append(final CharSequence csq) throws IOException {
        return this;
    }

    @Override
    public Appendable append(final CharSequence csq, final int start, final int end) throws IOException {
        return this;
    }

}
