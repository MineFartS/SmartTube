package com.liskovsoft.smartyoutubetv2.common.misc;

/**
 * Background worker that handles remote-control related periodic work and retries.
 *
 * Responsibilities:
 * - Process queued remote commands that must be handled off the main Rx stream.
 * - Execute retries/cleanup tasks (e.g. resending failed state updates).
 *
 * Execution:
 * - Use WorkManager or a dedicated background thread; avoid holding app context longer than needed.
 * - Ensure proper cancellation and idempotence: tasks may be executed multiple times by WorkManager.
 */

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.PlaybackPresenter;

public class RemoteControlWorker extends Worker {
    private static final String TAG = RemoteControlWorker.class.getSimpleName();

    public RemoteControlWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Doing some work...");

        PlaybackPresenter.instance(getApplicationContext()); // init RemoteControlListener

        return Result.success();
    }
}
