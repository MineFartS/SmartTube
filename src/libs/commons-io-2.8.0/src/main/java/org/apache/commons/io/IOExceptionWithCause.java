package org.apache.commons.io;

import java.io.IOException;

/**
 * Subclasses IOException with the {@link Throwable} constructors missing before Java 6.
 *
 * @since 1.4
 * @deprecated (since 2.5) use {@link IOException} instead
 */
@Deprecated
public class IOExceptionWithCause extends IOException {

    /**
     * Defines the serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new instance with the given message and cause.
     * <p>
     * As specified in {@link Throwable}, the message in the given <code>cause</code> is not used in this instance's
     * message.
     * </p>
     *
     * @param message
     *            the message (see {@link #getMessage()})
     * @param cause
     *            the cause (see {@link #getCause()}). A {@code null} value is allowed.
     */
    public IOExceptionWithCause(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new instance with the given cause.
     * <p>
     * The message is set to <code>cause==null ? null : cause.toString()</code>, which by default contains the class
     * and message of <code>cause</code>. This constructor is useful for call sites that just wrap another throwable.
     * </p>
     *
     * @param cause
     *            the cause (see {@link #getCause()}). A {@code null} value is allowed.
     */
    public IOExceptionWithCause(final Throwable cause) {
        super(cause);
    }

}
