package minefarts.smarttube.utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.liskovsoft.mediaserviceinterfaces.data.Command;
import com.liskovsoft.youtubeapi.service.data.YouTubeCommand;
import com.liskovsoft.youtubeapi.lounge.LoungeService;
import com.liskovsoft.youtubeapi.lounge.models.commands.CommandItem;
import com.liskovsoft.sharedutils.rx.RxHelper;

import io.reactivex.Observable;

public class RemoteControlService extends Service {

    private static RemoteControlService sInstance = null;
    
    private static final LoungeService mLoungeService = LoungeService.instance();

    public static RemoteControlService instance() {
        if (sInstance == null)
            sInstance = new RemoteControlService();

        return sInstance;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public String getPairingCode() {
        return mLoungeService.getPairingCode();
    }

    public Observable<String> getPairingCodeObserve() {
        return RxHelper.fromCallable(this::getPairingCode);
    }

    public Observable<Command> getCommandObserve() {
        return RxHelper.createLong(emitter -> {
            mLoungeService.startListening(
                info -> emitter.onNext(YouTubeCommand.from(info))
            );

            emitter.onComplete();
        });
    }

    public Observable<Void> postStartPlayingObserve(String videoId, long positionMs, long durationMs, boolean isPlaying) {
        return RxHelper.fromRunnable(() -> mLoungeService.postStartPlaying(videoId, positionMs, durationMs, toState(isPlaying)));
    }

    public Observable<Void> postStateChangeObserve(long positionMs, long durationMs, boolean isPlaying) {
        return RxHelper.fromRunnable(() -> mLoungeService.postStateChange(positionMs, durationMs, toState(isPlaying)));
    }

    public Observable<Void> postVolumeChangeObserve(int volume) {
        return RxHelper.fromRunnable(() -> mLoungeService.postVolumeChange(volume));
    }

    public Observable<Void> postSubtitleChangeObserve(String vssId, String languageCode) {
        return RxHelper.fromRunnable(() -> mLoungeService.postSubtitleChange(vssId, languageCode));
    }

    public Observable<Void> resetDataObserve() {
        return RxHelper.fromRunnable(mLoungeService::resetData);
    }

    private static int toState(boolean isPlaying) {
        return isPlaying ? STATE_PLAYING : STATE_PAUSED;
    }

    private static final int STATE_PLAYING = com.liskovsoft.mediaserviceinterfaces.RemoteControlService.STATE_PLAYING;
    private static final int STATE_PAUSED = com.liskovsoft.mediaserviceinterfaces.RemoteControlService.STATE_PAUSED;

}
