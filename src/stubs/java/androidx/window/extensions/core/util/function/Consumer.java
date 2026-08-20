package androidx.window.extensions.core.util.function;

/**
 * Stub interface implemented directly in the application source tree
 * to satisfy the signature requirements of modern Chromium WebViews 
 * on older Android devices.
 */
public interface Consumer<T> {
    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t);
}
