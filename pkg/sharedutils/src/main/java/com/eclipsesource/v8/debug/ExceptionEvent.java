
package com.liskovsoft.sharedutils.debug;

import com.liskovsoft.sharedutils.V8Object;

/**
 * Holds information about Exception Events.
 */
public class ExceptionEvent extends EventData {

    ExceptionEvent(final V8Object eventData) {
        super(eventData);
    }

}
