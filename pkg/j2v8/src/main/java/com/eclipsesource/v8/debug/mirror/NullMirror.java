
package com.eclipsesource.v8.debug.mirror;

import com.eclipsesource.v8.V8Object;

/**
 * Represents 'Null' Mirrors
 */
public class NullMirror extends ValueMirror {


    NullMirror(final V8Object v8Object) {
        super(v8Object);
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public String toString() {
        return "null";
    }

}
