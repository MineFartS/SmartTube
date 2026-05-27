package minefarts.smarttube.v8.utils;


import java.nio.ByteBuffer;

import minefarts.smarttube.v8.V8;
import minefarts.smarttube.v8.V8ArrayBuffer;

/**
 * A lightweight handle to a V8TypedArray. This handle provides
 * access to a V8TypedArray. This handle does not need to be
 * closed, but if the type array is accessed using getV8TypedArray
 * then the result must be closed.
 *
 * The underlying V8TypedArray may be reclaimed by the JavaScript
 * garbage collector. To check if it's still available, use
 * isAvailable.
 */
public class ArrayBuffer {

    private V8ArrayBuffer arrayBuffer;

    ArrayBuffer(final V8ArrayBuffer arrayBuffer) {
        this.arrayBuffer = (V8ArrayBuffer) arrayBuffer.twin().setWeak();
    }

    /**
     * Create a new ArrayBuffer from a java.nio.ByteBuffer
     *
     * @param v8 the Runtime on which to create the ArrayBuffer
     * @param byteBuffer the ByteBuffer to use to back the ArrayBuffer
     */
    public ArrayBuffer(final V8 v8, final ByteBuffer byteBuffer) {
        V8ArrayBuffer v8ArrayBuffer = new V8ArrayBuffer(v8, byteBuffer);
        try {
            arrayBuffer = (V8ArrayBuffer) v8ArrayBuffer.twin().setWeak();
        } finally {
            v8ArrayBuffer.close();
        }
    }

    /**
     * Determine if the underlying V8ArrayBuffer is still available, or if it's been cleaned up by the JavaScript
     * garbage collector.
     *
     * @return true if the underlying V8ArrayBuffer is still available, false otherwise.
     */
    public boolean isAvailable() {
        return !arrayBuffer.isReleased();
    }

    /**
     * Returns the underlying V8ArrayBuffer.
     * @return the underlying V8ArrayBuffer.
     */
    public V8ArrayBuffer getV8ArrayBuffer() {
        return arrayBuffer.twin();
    }

}
