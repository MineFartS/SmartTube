
package com.liskovsoft.sharedutils.debug.mirror;

import com.liskovsoft.sharedutils.V8Object;

/**
 * Represents JavaScript 'Property' Mirrors
 */
public class PropertyMirror extends Mirror {

    PropertyMirror(final V8Object v8Object) {
        super(v8Object);
    }

    /**
     * Returns the name of this property.
     *
     * @return The name of this property.
     */
    public String getName() {
        return v8Object.executeStringFunction("name", null);
    }

    /**
     * Returns the value of this property.
     *
     * @return The value of this property.
     */
    public Mirror getValue() {
        V8Object mirror = v8Object.executeObjectFunction("value", null);
        try {
            return createMirror(mirror);
        } finally {
            mirror.close();
        }
    }

    @Override
    public boolean isProperty() {
        return true;
    }

}
