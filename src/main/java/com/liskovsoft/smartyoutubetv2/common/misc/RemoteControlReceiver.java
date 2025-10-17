package com.liskovsoft.smartyoutubetv2.common.misc;

/**
 * BroadcastReceiver that listens to system/boot events relevant to remote-control and background services.
 *
 * Responsibilities:
 * - React to BOOT_COMPLETED, TIMEZONE_CHANGED and other system broadcasts to re-schedule RemoteControl/Reminders.
 * - Keep handling minimal: start services or schedule WorkManager jobs rather than performing heavy work here.
 *
 * Security:
 * - Only receive/export the intents that are necessary; be conservative with exported receivers.
 */
public class RemoteControlReceiver extends BroadcastReceiver {
    private static final String TAG = RemoteControlReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Initializing remote control listener...");

        // Fix unload from the memory on some devices?
        Utils.updateRemoteControlService(context);

        //Utils.startRemoteControlWorkRequest(context);

        // Couldn't success inside periodic work request
        //PlaybackPresenter.instance(context); // init RemoteControlListener
    }
}
