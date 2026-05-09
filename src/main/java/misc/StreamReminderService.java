package minefarts.smarttube.misc;

import android.content.Context;
import com.liskovsoft.sharedutils.MediaItemService;
import minefarts.smarttube.misc.ServiceManager;
import com.liskovsoft.sharedutils.data.MediaItemFormatInfo;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.Queue;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.presenters.PlaybackPresenter;
import minefarts.smarttube.app.views.ViewManager;
import minefarts.smarttube.misc.TickleManager.TickleListener;
import minefarts.smarttube.prefs.GeneralData;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.util.ArrayList;
import java.util.List;

public class StreamReminderService implements TickleListener {
    private static final String TAG = StreamReminderService.class.getSimpleName();
    private static StreamReminderService sInstance;
    private final MediaItemService mMediaItemService;
    private final Context mContext;
    private final GeneralData mGeneralData;
    private Disposable mReminderAction;

    private StreamReminderService(Context context) {
        mMediaItemService = ServiceManager.getMediaItemService();
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

            Video current = Queue.getCurrent();

            if (current != null && current.isPending && ViewManager.instance(mContext).isPlayerInForeground()) {
                Queue.add(video);
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
            result.add(mMediaItemService.getFormatInfoObserve(item.videoId));
        }

        return result;
    }
}
