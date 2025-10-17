package com.liskovsoft.smartyoutubetv2.common.autoframerate;

/**
 * Coordinates application-requested auto-frame-rate (AFR) modes and notifies registered listeners.
 * Keeps desired AFR mode centralized and dispatches changes to platform helpers.
 */
public class ModeSyncManager {
    private static ModeSyncManager sInstance;
    private FormatItem mFormatItem;
    private AutoFrameRateHelper mFrameRateHelper;

    private ModeSyncManager() {
        // NOP
    }

    public static ModeSyncManager instance() {
        if (sInstance == null) {
            sInstance = new ModeSyncManager();
        }

        return sInstance;
    }

    public void save(FormatItem formatItem) {
        mFormatItem = formatItem;
    }

    public void restore(Activity activity) {
        if (mFrameRateHelper == null) {
            return;
        }

        new Handler(Looper.myLooper()).postDelayed(() -> applyAfr(activity), 1_000);
    }

    private void applyAfr(Activity activity) {
        if (mFormatItem != null) {
            mFrameRateHelper.apply(activity, mFormatItem);
        } else {
            //mFrameRateHelper.restoreOriginalState(activity);
        }
    }

    public void setAfrHelper(AutoFrameRateHelper frameRateHelper) {
        mFrameRateHelper = frameRateHelper;
    }
}
