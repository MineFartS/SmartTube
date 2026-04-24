
package com.liskovsoft.sharedutils.debug.mirror;

import com.liskovsoft.sharedutils.Releasable;
import com.liskovsoft.sharedutils.V8Array;
import com.liskovsoft.sharedutils.V8Object;

/**
 * Provides typed access to a set of properties.
 */
public class PropertiesArray implements Releasable {

    private V8Array v8Array;

    PropertiesArray(final V8Array v8Object) {
        v8Array = v8Object.twin();
    }

    /**
     * Returns the PropertyMiror at a given index.
     *
     * @param index The index of the property
     * @return The property at the given index
     */
    public PropertyMirror getProperty(final int index) {
        V8Object result = v8Array.getObject(index);
        try {
            return new PropertyMirror(result);
        } finally {
            result.close();
        }
    }

    /*
     * (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() {
        if (!v8Array.isReleased()) {
            v8Array.close();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.liskovsoft.sharedutils.Releasable#release()
     */
    @Override
    @Deprecated
    public void release() {
        close();
    }

    /**
     * Returns the number of properties contained in this array.
     *
     * @return The length of this array.
     */
    public int length() {
        return v8Array.length();
    }

}
