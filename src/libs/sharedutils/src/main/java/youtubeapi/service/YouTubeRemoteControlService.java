package minefarts.sharedutils.service;

import minefarts.sharedutils.RemoteControlService;
import minefarts.sharedutils.service.data.Command;
import minefarts.sharedutils.prefs.GlobalPreferences;
import minefarts.sharedutils.rx.RxHelper;
import minefarts.sharedutils.lounge.LoungeService;
import io.reactivex.Observable;

public class YouTubeRemoteControlService implements RemoteControlService {

    private static YouTubeRemoteControlService sInstance;
    private final LoungeService mLoungeService;

    private YouTubeRemoteControlService() {
        mLoungeService = LoungeService.instance();

        GlobalPreferences.setOnInit(() -> {
            //mAccountManager.init();
            //this.updateAuthorizationHeader();
        });
    }

    public static YouTubeRemoteControlService instance() {
        if (sInstance == null) {
            sInstance = new YouTubeRemoteControlService();
        }

        return sInstance;
    }

    @Override
    public String getPairingCode() {
        return mLoungeService.getPairingCode();
    }

    @Override
    public Observable<String> getPairingCodeObserve() {
        return RxHelper.fromCallable(this::getPairingCode);
    }

    @Override
    public Observable<Command> getCommandObserve() {
        return RxHelper.createLong(emitter -> {
            mLoungeService.startListening(
                    info -> emitter.onNext(Command.from(info))
            );

            emitter.onComplete();
        });
    }

    @Override
    public Observable<Void> postStartPlayingObserve(String videoId, long positionMs, long durationMs, boolean isPlaying) {
        return RxHelper.fromRunnable(() -> mLoungeService.postStartPlaying(videoId, positionMs, durationMs, isPlaying));
    }

    @Override
    public Observable<Void> postStateChangeObserve(long positionMs, long durationMs, boolean isPlaying) {
        return RxHelper.fromRunnable(() -> mLoungeService.postStateChange(positionMs, durationMs, isPlaying));
    }

    @Override
    public Observable<Void> postVolumeChangeObserve(int volume) {
        return RxHelper.fromRunnable(() -> mLoungeService.postVolumeChange(volume));
    }

    @Override
    public Observable<Void> resetDataObserve() {
        return RxHelper.fromRunnable(mLoungeService::resetData);
    }
}
