package minefarts.smarttube.v8.debug;

import minefarts.smarttube.v8.V8Object;
import minefarts.smarttube.v8.debug.DebugHandler.DebugEvent;

public interface BreakHandler {

    public void onBreak(DebugEvent type, ExecutionState state, EventData eventData, V8Object data);

}
