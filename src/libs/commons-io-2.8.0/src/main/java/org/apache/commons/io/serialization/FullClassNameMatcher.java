package org.apache.commons.io.serialization;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link ClassNameMatcher} that matches on full class names.
 * <p>
 * This object is immutable and thread-safe.
 * </p>
 */
final class FullClassNameMatcher implements ClassNameMatcher {

    private final Set<String> classesSet;

    /**
     * Constructs an object based on the specified class names.
     *
     * @param classes a list of class names
     */
    public FullClassNameMatcher(final String... classes) {
        classesSet = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(classes)));
    }

    @Override
    public boolean matches(final String className) {
        return classesSet.contains(className);
    }
}