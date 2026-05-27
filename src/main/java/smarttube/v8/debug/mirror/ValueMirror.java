package minefarts.smarttube.v8.debug.mirror;

import minefarts.smarttube.v8.V8Object;

/**
 * Represents 'Value' Mirrors (Objects, Numbers, Strings, ...).
 */
public class ValueMirror extends Mirror {

    private static final String VALUE = "value";

    ValueMirror(final V8Object v8Object) {
        super(v8Object);
    }

    /**
     * Returns the Object that this mirror represents.
     *
     * @return The object that this mirror represents.
     */
    public Object getValue() {
        return v8Object.executeFunction(VALUE, null);
    }

    @Override
    public boolean isValue() {
        return true;
    }

}
