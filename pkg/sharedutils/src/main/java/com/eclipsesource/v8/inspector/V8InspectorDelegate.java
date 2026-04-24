package com.liskovsoft.sharedutils.inspector;

public interface V8InspectorDelegate {
    public void onResponse(String message);

    public void waitFrontendMessageOnPause();
}
