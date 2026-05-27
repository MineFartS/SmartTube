package com.eclipsesource.v8.debug;

import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8Object;

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
