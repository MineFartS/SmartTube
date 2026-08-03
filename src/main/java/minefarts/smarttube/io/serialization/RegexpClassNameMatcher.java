package minefarts.smarttube.io.serialization;

import java.util.regex.Pattern;

/**
 * A {@link ClassNameMatcher} that uses regular expressions.
 * <p>
 * This object is immutable and thread-safe.
 * </p>
 */
final class RegexpClassNameMatcher implements ClassNameMatcher {

    private final Pattern pattern; // Class is thread-safe

    /**
     * Constructs an object based on the specified regular expression.
     *
     * @param regex a regular expression for evaluating acceptable class names
     */
    public RegexpClassNameMatcher(final String regex) {
        this(Pattern.compile(regex));
    }

    /**
     * Constructs an object based on the specified pattern.
     *
     * @param pattern a pattern for evaluating acceptable class names
     * @throws IllegalArgumentException if <code>pattern</code> is null
     */
    public RegexpClassNameMatcher(final Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Null pattern");
        }
        this.pattern = pattern;
    }

    @Override
    public boolean matches(final String className) {
        return pattern.matcher(className).matches();
    }
}