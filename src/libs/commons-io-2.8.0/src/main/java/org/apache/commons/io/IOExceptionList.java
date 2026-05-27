package org.apache.commons.io;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * A IOException based on a list of Throwable causes.
 * <p>
 * The first exception in the list is used as this exception's cause and is accessible with the usual
 * {@link #getCause()} while the complete list is accessible with {@link #getCauseList()}.
 * </p>
 *
 * @since 2.7
 */
@SuppressWarnings("unchecked")
public class IOExceptionList extends IOException {

    private static final long serialVersionUID = 1L;
    private final List<? extends Throwable> causeList;

    /**
     * Creates a new exception caused by a list of exceptions.
     *
     * @param causeList a list of cause exceptions.
     */
    public IOExceptionList(final List<? extends Throwable> causeList) {
        super(String.format("%,d exceptions: %s", causeList == null ? 0 : causeList.size(), causeList),
                causeList == null ? null : causeList.get(0));
        this.causeList = causeList == null ? Collections.emptyList() : causeList;
    }

    /**
     * Gets the cause list.
     *
     * @param <T> type of exception to return.
     * @return The list of causes.
     */
    public <T extends Throwable> List<T> getCauseList() {
        return (List<T>) causeList;
    }

    /**
     * Gets the cause exception at the given index.
     *
     * @param <T> type of exception to return.
     * @param index index in the cause list.
     * @return The list of causes.
     */
    public <T extends Throwable> T getCause(final int index) {
        return (T) causeList.get(index);
    }

    /**
     * Gets the cause exception at the given index.
     *
     * @param <T> type of exception to return.
     * @param index index in the cause list.
     * @param clazz type of exception to return.
     * @return The list of causes.
     */
    public <T extends Throwable> T getCause(final int index, final Class<T> clazz) {
        return (T) causeList.get(index);
    }

    /**
     * Works around Throwable and Generics, may fail at runtime depending on the argument value.
     *
     * @param <T> type of exception to return.
     * @param clazz the target type
     * @return The list of causes.
     */
    public <T extends Throwable> List<T> getCauseList(final Class<T> clazz) {
        return (List<T>) causeList;
    }

}
