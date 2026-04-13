
package com.eclipsesource.v8.debug.mirror;

import com.eclipsesource.v8.V8Object;

/**
 * Represents JavaScript 'Number' Mirrors
 */
public class NumberMirror extends ValueMirror {

    NumberMirror(final V8Object v8Object) {
        super(v8Object);
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public String toString() {
        return v8Object.executeStringFunction("toText", null);
    }

}
