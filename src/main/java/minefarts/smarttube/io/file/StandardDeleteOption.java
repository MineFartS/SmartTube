package minefarts.smarttube.io.file;

/**
 * Defines the standard delete options.
 *
 * @since 2.8.0
 */
public enum StandardDeleteOption implements DeleteOption {

    /**
     * Overrides the read-only attribute to allow deletion.
     */
    OVERRIDE_READ_ONLY;

    /**
     * Returns true if the given options contain {@link StandardDeleteOption#OVERRIDE_READ_ONLY}.
     *
     * For now, assume the array is not sorted.
     *
     * @param options the array to test
     * @return true if the given options contain {@link StandardDeleteOption#OVERRIDE_READ_ONLY}.
     */
    public static boolean overrideReadOnly(final DeleteOption[] options) {
        if (options == null || options.length == 0) {
            return false;
        }
        for (final DeleteOption deleteOption : options) {
            if (deleteOption == StandardDeleteOption.OVERRIDE_READ_ONLY) {
                return true;
            }
        }
        return false;
    }

}
