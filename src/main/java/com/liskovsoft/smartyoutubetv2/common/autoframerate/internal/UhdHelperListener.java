package com.liskovsoft.smartyoutubetv2.common.autoframerate.internal;

/**
 * Listener for UHD/display helper events (mode availability/changes).
 * Implementations receive lightweight callbacks, typically posted on UI thread.
 *
 * To unregister the listener, use
 * {@link UhdHelper#unregisterDisplayModeChangeListener(UhdHelperListener) unregisterDisplayModeChangeListener}
 */
public interface UhdHelperListener {
    /**
     * Callback containing the result of the mode change after
     * {@link UhdHelper#setPreferredDisplayModeId(Window, int,boolean) setPreferredDisplayModeId}
     * returns a true.
     *
     * @param mode The {@link DisplayHolder.Mode Mode} object containing
     *             the mode switched to OR NULL if there was a timeout
     *             or internal error while changing the mode.
     */
    void onModeChanged(DisplayHolder.Mode mode);

}

