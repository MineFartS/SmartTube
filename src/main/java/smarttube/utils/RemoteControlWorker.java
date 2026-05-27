package minefarts.smarttube.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.app.presenters.PlaybackPresenter;

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
