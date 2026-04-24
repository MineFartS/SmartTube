
package com.liskovsoft.sharedutils.debug.mirror;

import com.liskovsoft.sharedutils.V8Object;

/**
 * Represents JavaScript 'String' Mirrors
 */
public class StringMirror extends ValueMirror {

    StringMirror(final V8Object v8Object) {
        super(v8Object);
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public String toString() {
        return v8Object.executeStringFunction("toText", null);
    }

}
