package minefarts.smarttube.io.output;

import java.io.PrintStream;

/**
 * Writes all data to the famous <b>/dev/null</b>.
 * <p>
 * This print stream has no destination (file/socket etc.) and all bytes written to it are ignored and lost.
 * </p>
 *
 * @since 2.7
 */
public class NullPrintStream extends PrintStream {

    /**
     * The singleton instance.
     */
    public static final NullPrintStream NULL_PRINT_STREAM = new NullPrintStream();

    /**
     * Constructs an instance.
     */
    public NullPrintStream() {
        // Relies on the default charset which is OK since we are not actually writing.
        super(NullOutputStream.NULL_OUTPUT_STREAM);
    }

}
