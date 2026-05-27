package org.apache.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream implementation that writes the data to an {@link Appendable}
 * Object.
 * <p>
 * For example, can be used with any {@link java.io.Writer} or a {@link java.lang.StringBuilder}
 * or {@link java.lang.StringBuffer}.
 *
 * @since 2.5
 * @see java.lang.Appendable
 *
 * @param <T> The type of the {@link Appendable} wrapped by this AppendableOutputStream.
 */
public class AppendableOutputStream <T extends Appendable> extends OutputStream {

    private final T appendable;

    /**
     * Construct a new instance with the specified appendable.
     *
     * @param appendable the appendable to write to
     */
    public AppendableOutputStream(final T appendable) {
        this.appendable = appendable;
    }

    /**
     * Write a character to the underlying appendable.
     *
     * @param b the character to write
     * @throws IOException upon error
     */
    @Override
    public void write(final int b) throws IOException {
        appendable.append((char)b);
    }

    /**
     * Return the target appendable.
     *
     * @return the target appendable
     */
    public T getAppendable() {
        return appendable;
    }

}
