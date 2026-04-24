
package com.liskovsoft.sharedutils;

/**
 * A top-level exception used to indicate that a script failed. In most cases
 * a more specific exception will be thrown.
 */
@SuppressWarnings("serial")
public class V8RuntimeException extends RuntimeException {

    V8RuntimeException() {
    }

    V8RuntimeException(final String message) {
        super(message);
    }

}
