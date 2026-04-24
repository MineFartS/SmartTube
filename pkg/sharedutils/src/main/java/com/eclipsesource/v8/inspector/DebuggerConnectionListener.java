package com.liskovsoft.sharedutils.inspector;

public interface DebuggerConnectionListener {
    public void onDebuggerConnected();

    public void onDebuggerDisconnected();
}
