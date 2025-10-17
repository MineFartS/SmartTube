package com.liskovsoft.smartyoutubetv2.common.misc;

/**
 * Worker that performs Google Drive backups (uploading backup archives to GDrive).
 *
 * Responsibilities:
 * - Authenticate (via service), upload backup files, handle resumable uploads and retry logic.
 * - Report progress and final status back to BackupManager/UI.
 *
 * Security/permissions:
 * - Use Android recommended sign-in/auth flows; do not store plain tokens.
 * - Run long uploads via WorkManager to survive process restarts.
 */
public class GDriveBackupWorker extends Worker {
    private static final String TAG = GDriveBackupWorker.class.getSimpleName();
    private static final String WORK_NAME = TAG;
    private static final String BLOCKED_FILE_NAME = "blocked";
    private static final long REPEAT_INTERVAL_DAYS = 1;
    private static Disposable sAction;
    private final GDriveBackupManager mTask;

    public GDriveBackupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        mTask = GDriveBackupManager.instance(context);
    }

    public static void schedule(Context context) {
        if (VERSION.SDK_INT >= 23 && GeneralData.instance(context).isAutoBackupEnabled()) {
            WorkManager workManager = WorkManager.getInstance(context);

            // https://stackoverflow.com/questions/50943056/avoiding-duplicating-periodicworkrequest-from-workmanager
            workManager.enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE, // fix duplicates (when old worker is running)
                    new PeriodicWorkRequest.Builder(GDriveBackupWorker.class, REPEAT_INTERVAL_DAYS, TimeUnit.DAYS).addTag(WORK_NAME).build()
            );
        }
    }

    public static void forceSchedule(Context context) {
        RxHelper.disposeActions(sAction);

        // get local id
        String id = Utils.getUniqueId(context);

        // get backup path
        String backupDir = GDriveBackupManager.instance(context).getBackupDir();

        // then persist id to gdrive
        sAction = DriveService.uploadFile(id, Uri.parse(String.format("%s/%s", backupDir, BLOCKED_FILE_NAME)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(unused -> {
                    // NOP
                }, throwable -> {
                    // NOP
                }, () -> {
                    // then run schedule
                    schedule(context);
                });
    }

    public static void cancel(Context context) {
        RxHelper.disposeActions(sAction);

        if (VERSION.SDK_INT >= 23 && GeneralData.instance(context).isAutoBackupEnabled()) {
            Log.d(TAG, "Unregistering worker job...");

            WorkManager workManager = WorkManager.getInstance(context);
            workManager.cancelUniqueWork(WORK_NAME);
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting worker %s...", this);

        checkedRunBackup();

        return Result.success();
    }

    private void runBackup() {
        mTask.backupBlocking();
        GDriveBackupManager.unhold();
    }

    private void checkedRunBackup() {
        // get local id
        String id = Utils.getUniqueId(getApplicationContext());

        // get backup path
        String backupDir = GDriveBackupManager.instance(getApplicationContext()).getBackupDir();

        // get id form gdrive
        DriveService.getFile(Uri.parse(String.format("%s/%s", backupDir, BLOCKED_FILE_NAME)))
                .blockingSubscribe(inputStream -> {
                    // if id match run work as usual
                    String actualId = Helpers.toString(inputStream);
                    if (Helpers.equals(id, actualId)) {
                        runBackup();
                    } else {
                        // if id not found then disable auto backup in settings
                        GeneralData.instance(getApplicationContext()).setAutoBackupEnabled(false);
                    }
                }, error -> Log.e(TAG, error.getMessage()));
    }
}
