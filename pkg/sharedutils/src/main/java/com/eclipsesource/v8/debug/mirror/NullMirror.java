
package com.liskovsoft.sharedutils.debug.mirror;

import com.liskovsoft.sharedutils.V8Object;

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
