package com.liskovsoft.smartyoutubetv2.common.misc;

/**
 * Service responsible for managing stream reminders (schedule notifications for upcoming live streams).
 *
 * Responsibilities:
 * - Schedule/cancel reminders for upcoming streams using AlarmManager/WorkManager as appropriate.
 * - Persist reminder metadata and provide APIs to query scheduled reminders.
 * - Handle device reboots (re-schedule reminders on BOOT_COMPLETED).
 *
 * Permissions & lifecycle:
 * - Should run as a foreground/background worker depending on platform; avoid long-running tasks on main thread.
 * - Respect Doze/standby and use recommended scheduling APIs (WorkManager) for reliability.
 */
public class StreamReminderService implements TickleListener {
    private static final String TAG = StreamReminderService.class.getSimpleName();
    private static StreamReminderService sInstance;
    private final MediaItemService mItemManager;
    private final Context mContext;
    private final GeneralData mGeneralData;
    private Disposable mReminderAction;

    private StreamReminderService(Context context) {
        ServiceManager service = YouTubeServiceManager.instance();
        mItemManager = service.getMediaItemService();
        mContext = context.getApplicationContext();
        mGeneralData = GeneralData.instance(context);
    }

    public static StreamReminderService instance(Context context) {
        if (sInstance == null) {
            sInstance = new StreamReminderService(context);
        }

        return sInstance;
    }

    public boolean isReminderSet(Video video) {
        return mGeneralData.containsPendingStream(video);
    }

    public void toggleReminder(Video video) {
        if (video.videoId == null || !video.isUpcoming) {
            return;
        }

        if (mGeneralData.containsPendingStream(video)) {
            mGeneralData.removePendingStream(video);
        } else {
            mGeneralData.addPendingStream(video);
        }

        start();
    }

    public void start() {
        if (mGeneralData.getPendingStreams().isEmpty()) {
            TickleManager.instance().removeListener(this);
            sInstance = null;
        } else {
            TickleManager.instance().addListener(this);
        }
    }

    @Override
    public void onTickle() {
        if (mGeneralData.getPendingStreams().isEmpty()) {
            start();
            return;
        }

        RxHelper.disposeActions(mReminderAction);

        List<Observable<MediaItemFormatInfo>> observables = toObservables();

        mReminderAction = Observable.mergeDelayError(observables)
                .subscribe(
                        this::processMetadata,
                        error -> Log.e(TAG, "loadMetadata error: %s", error.getMessage())
                );
    }

    private void processMetadata(MediaItemFormatInfo formatInfo) {
        String videoId = formatInfo.getVideoId();
        if (formatInfo.containsMedia() && videoId != null) {
            Video video = new Video();
            video.title = formatInfo.getTitle();
            video.videoId = videoId;
            video.isPending = true;

            Playlist playlist = Playlist.instance();
            Video current = playlist.getCurrent();

            if (current != null && current.isPending && ViewManager.instance(mContext).isPlayerInForeground()) {
                playlist.add(video);
            } else {
                ViewManager.instance(mContext).movePlayerToForeground();
                PlaybackPresenter.instance(mContext).openVideo(video);
                MessageHelpers.showLongMessage(mContext, R.string.starting_stream);
            }

            mGeneralData.removePendingStream(video);
            start();
        }
    }

    /**
     * NOTE: don't use MediaItemMetadata because it has contains isLive and isUpcoming flags
     */
    private List<Observable<MediaItemFormatInfo>> toObservables() {
        List<Observable<MediaItemFormatInfo>> result = new ArrayList<>();

        for (Video item : mGeneralData.getPendingStreams()) {
            result.add(mItemManager.getFormatInfoObserve(item.videoId));
        }

        return result;
    }
}
