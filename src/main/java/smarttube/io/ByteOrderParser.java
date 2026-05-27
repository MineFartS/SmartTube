package minefarts.smarttube.io;

import java.nio.ByteOrder;

/**
 * Converts Strings to {@link ByteOrder} instances.
 *
 * @since 2.6
 */
public final class ByteOrderParser {

    /**
     * ByteOrderUtils is a static utility class, so prevent construction with a private constructor.
     */
    private ByteOrderParser() {
    }

    /**
     * Parses the String argument as a {@link ByteOrder}.
     * <p>
     * Returns {@code ByteOrder.LITTLE_ENDIAN} if the given value is {@code "LITTLE_ENDIAN"}.
     * </p>
     * <p>
     * Returns {@code ByteOrder.BIG_ENDIAN} if the given value is {@code "BIG_ENDIAN"}.
     * </p>
     * Examples:
     * <ul>
     * <li>{@code ByteOrderParser.parseByteOrder("LITTLE_ENDIAN")} returns {@code ByteOrder.LITTLE_ENDIAN}</li>
     * <li>{@code ByteOrderParser.parseByteOrder("BIG_ENDIAN")} returns {@code ByteOrder.BIG_ENDIAN}</li>
     * </ul>
     *
     * @param value
     *            the {@code String} containing the ByteOrder representation to be parsed
     * @return the ByteOrder represented by the string argument
     * @throws IllegalArgumentException
     *             if the {@code String} containing the ByteOrder representation to be parsed is unknown.
     */
    public static ByteOrder parseByteOrder(final String value) {
        if (ByteOrder.BIG_ENDIAN.toString().equals(value)) {
            return ByteOrder.BIG_ENDIAN;
        }
        if (ByteOrder.LITTLE_ENDIAN.toString().equals(value)) {
            return ByteOrder.LITTLE_ENDIAN;
        }
        throw new IllegalArgumentException("Unsupported byte order setting: " + value + ", expected one of " + ByteOrder.LITTLE_ENDIAN +
                 ", " + ByteOrder.BIG_ENDIAN);
    }

}
