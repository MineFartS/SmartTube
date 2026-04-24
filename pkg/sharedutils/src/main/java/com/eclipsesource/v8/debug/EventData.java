
package com.liskovsoft.sharedutils.debug;

import com.liskovsoft.sharedutils.Releasable;
import com.liskovsoft.sharedutils.V8Object;

/**
 * Typed information about different debug events.
 */
public class EventData implements Releasable {

    protected V8Object v8Object;

    EventData(final V8Object eventData) {
        v8Object = eventData.twin();
    }

    @Override
    public void close() {
        if (!v8Object.isReleased()) {
            v8Object.close();
        }
    }

    @Override
    @Deprecated
    public void release() {
        close();
    }

}
