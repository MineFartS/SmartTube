package org.apache.commons.io.function;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Like {@link Supplier} but throws {@link IOException}.
 *
 * @param <T> the return type of the operations.
 * @since 2.7
 */
@FunctionalInterface
public interface IOSupplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     *
     * @throws IOException if an IO error occurs whilst supplying the value.
     */
    T get() throws IOException;
}
