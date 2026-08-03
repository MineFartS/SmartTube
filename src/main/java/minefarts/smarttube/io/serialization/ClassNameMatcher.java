package minefarts.smarttube.io.serialization;

/**
 * An object that matches a Class name to a condition.
 */
public interface ClassNameMatcher {

    /**
     * Returns <code>true</code> if the supplied class name matches this object's condition.
     *
     * @param className fully qualified class name
     * @return <code>true</code> if the class name matches this object's condition
     */
    boolean matches(String className);
}