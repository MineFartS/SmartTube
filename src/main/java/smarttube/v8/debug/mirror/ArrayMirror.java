package minefarts.smarttube.v8.debug.mirror;

import minefarts.smarttube.v8.V8Object;

/**
 * Represents 'Array' mirrors.
 */
public class ArrayMirror extends ObjectMirror {

    private static final String LENGTH = "length";

    ArrayMirror(final V8Object v8Object) {
        super(v8Object);
    }

    @Override
    public boolean isArray() {
        return true;
    }

    /**
     * Returns the length of the array pointed to by this Array Mirror
     *
     * @return The length of the array.
     */
    public int length() {
        return v8Object.executeIntegerFunction(LENGTH, null);
    }

}
