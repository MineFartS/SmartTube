package minefarts.smarttube.v8.debug;

import minefarts.smarttube.v8.V8Object;

/**
 * Holds information about break events.
 */
public class BreakEvent extends EventData {

    private static final String SOURCE_LINE_TEXT = "sourceLineText";
    private static final String SOURCE_COLUMN    = "sourceColumn";
    private static final String SOURCE_LINE      = "sourceLine";

    BreakEvent(final V8Object eventData) {
        super(eventData);
    }

    /**
     * Returns the source line that this break event occurred on.
     *
     * @return The line number that this break event occurred on.
     */
    public int getSourceLine() {
        return v8Object.executeIntegerFunction(SOURCE_LINE, null);
    }

    /**
     * Returns the source column that this break event occurred on.
     *
     * @return The column number that this break event occurred on.
     */
    public int getSourceColumn() {
        return v8Object.executeIntegerFunction(SOURCE_COLUMN, null);
    }

    /**
     * Returns the text of the line that this event occurred on.
     *
     * @return The text of the line that this event occurred on.
     */
    public String getSourceLineText() {
        return v8Object.executeStringFunction(SOURCE_LINE_TEXT, null);
    }

}
