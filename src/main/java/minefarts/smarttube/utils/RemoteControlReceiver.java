package minefarts.smarttube.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;

import minefarts.smarttube.utils.helpers.AppInfoHelpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.Utils;

public class RemoteControlReceiver extends BroadcastReceiver {
    private static final String TAG = RemoteControlReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // Starting from Android 12 foreground service not supported
        if (AppInfoHelpers.getTargetSdkVersion(context) < 31) {
            Log.d(TAG, "Initializing remote control listener...");

            // Fix unload from the memory on some devices?
            Utils.updateRemoteControlService(context);
        }

        //Utils.startRemoteControlWorkRequest(context);

        // Couldn't success inside periodic work request
        //PlaybackPresenter.instance(context); // init RemoteControlListener
    }
}
