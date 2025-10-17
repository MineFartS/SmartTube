package com.liskovsoft.smartyoutubetv2.common.utils;

/**
 * Thin wrapper to send intents/commands to the TvQuickActions helper app.
 * Used to start/stop external AFR daemon and similar quick actions.
 */
import android.content.Context;
import android.content.Intent;
import com.liskovsoft.smartyoutubetv2.common.exoplayer.selector.FormatItem;

public class TvQuickActions {
    private final static String PACKAGE = "dev.vodik7.tvquickactions";

    public static void sendStopAFR(Context context) {
        if (context == null) {
            return;
        }

        Intent intent = new Intent();
        intent.setPackage(PACKAGE);
        intent.setAction(PACKAGE + ".STOP_AFR");
        context.sendBroadcast(intent);
    }

    public static void sendStartAFR(Context context, FormatItem videoFormat) {
        if (videoFormat != null) {
            Intent intent = new Intent();
            intent.setPackage(PACKAGE);
            intent.setAction(PACKAGE + ".START_AFR");
            intent.putExtra("fps", videoFormat.getFrameRate());
            intent.putExtra("height", videoFormat.getHeight());
            context.sendBroadcast(intent);
        }
    }
}