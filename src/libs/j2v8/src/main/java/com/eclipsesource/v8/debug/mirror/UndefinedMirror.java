package com.eclipsesource.v8.debug.mirror;

import com.eclipsesource.v8.V8Object;

/**
 * Represents 'Undefined' Mirrors
 */
public class UndefinedMirror extends ValueMirror {


    UndefinedMirror(final V8Object v8Object) {
        super(v8Object);
    }

    @Override
    public boolean isUndefined() {
        return true;
    }

    @Override
    public String toString() {
        return "undefined";
    }

}
