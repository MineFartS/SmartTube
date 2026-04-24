
package com.eclipsesource.v8.debug;

import com.eclipsesource.v8.V8Object;

/**
 * Holds information about Exception Events.
 */
public class ExceptionEvent extends EventData {

    ExceptionEvent(final V8Object eventData) {
        super(eventData);
    }

}
