
package com.liskovsoft.sharedutils.debug;

import com.liskovsoft.sharedutils.V8Object;
import com.liskovsoft.sharedutils.debug.DebugHandler.DebugEvent;

public interface BreakHandler {

    public void onBreak(DebugEvent type, ExecutionState state, EventData eventData, V8Object data);

}
