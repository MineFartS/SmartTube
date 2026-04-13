
package com.eclipsesource.v8;

import java.io.Closeable;

/**
 * An interface used to denote all V8 Classes which can be released.
 */
public interface Releasable extends Closeable {

    /**
     * Release the underlying resources. Once an object is released
     * it typically cannot be used again.
     */
    void close();

    /**
     * Synonym for {@link #close()}.
     */
    void release();
}
