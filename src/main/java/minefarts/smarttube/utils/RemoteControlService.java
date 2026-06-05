package minefarts.smarttube.utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import minefarts.smarttube.utils.service.data.Command;
import minefarts.smarttube.utils.RemoteControlService;
import minefarts.smarttube.utils.service.data.Command;
import minefarts.smarttube.utils.prefs.GlobalPreferences;
import minefarts.smarttube.utils.rx.RxHelper;
import minefarts.smarttube.utils.lounge.LoungeService;

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
                    info -> emitter.onNext(Command.from(info))
            );

            emitter.onComplete();
        });
    }

    public Observable<Void> postStartPlayingObserve(String videoId, long positionMs, long durationMs, boolean isPlaying) {
        return RxHelper.fromRunnable(() -> mLoungeService.postStartPlaying(videoId, positionMs, durationMs, isPlaying));
    }

    public Observable<Void> postStateChangeObserve(long positionMs, long durationMs, boolean isPlaying) {
        return RxHelper.fromRunnable(() -> mLoungeService.postStateChange(positionMs, durationMs, isPlaying));
    }

    public Observable<Void> postVolumeChangeObserve(int volume) {
        return RxHelper.fromRunnable(() -> mLoungeService.postVolumeChange(volume));
    }

    public Observable<Void> resetDataObserve() {
        return RxHelper.fromRunnable(mLoungeService::resetData);
    }

}
