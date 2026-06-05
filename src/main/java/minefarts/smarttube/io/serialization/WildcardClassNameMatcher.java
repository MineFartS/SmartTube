package minefarts.smarttube.io.serialization;

import minefarts.smarttube.io.FilenameUtils;

/**
 * A {@link ClassNameMatcher} that uses simplified regular expressions
 *  provided by {@link minefarts.smarttube.io.FilenameUtils#wildcardMatch(String, String) FilenameUtils.wildcardMatch}
 * <p>
 * This object is immutable and thread-safe.
 * </p>
 */
final class WildcardClassNameMatcher implements ClassNameMatcher {

    private final String pattern;

    /**
     * Constructs an object based on the specified simplified regular expression.
     *
     * @param pattern a {@link FilenameUtils#wildcardMatch} pattern.
     */
    public WildcardClassNameMatcher(final String pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean matches(final String className) {
        return FilenameUtils.wildcardMatch(className, pattern);
    }
}