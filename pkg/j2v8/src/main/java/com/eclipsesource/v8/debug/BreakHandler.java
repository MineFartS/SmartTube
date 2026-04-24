
package com.eclipsesource.v8.debug;

import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.debug.DebugHandler.DebugEvent;

public interface BreakHandler {

    public void onBreak(DebugEvent type, ExecutionState state, EventData eventData, V8Object data);

}
