
package com.eclipsesource.v8;

/**
 * An exception that's used to indicate that method that should have returned a
 * primitive, returned an Undefined instead.
 *
 * In Java, Undefined cannot be returned for all methods, especially if
 * the method returns a primitive (int, double, boolean) or a String.
 * In this case, if an Undefined should be returned from JS, then an instance
 * of this exception is thrown.
 */
@SuppressWarnings("serial")
public class V8ResultUndefined extends V8RuntimeException {

    V8ResultUndefined(final String message) {
        super(message);
    }

    V8ResultUndefined() {
        super();
    }
}
