
package com.eclipsesource.v8.debug.mirror;

import com.eclipsesource.v8.V8Object;

/**
 * Represents JavaScript 'Boolean' Mirrors
 */
public class BooleanMirror extends ValueMirror {

    BooleanMirror(final V8Object v8Object) {
        super(v8Object);
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public String toString() {
        return v8Object.executeStringFunction("toText", null);
    }
}
