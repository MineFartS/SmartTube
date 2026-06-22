package minefarts.smarttube.app.models.playback.controllers;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import androidx.annotation.Nullable;
import minefarts.smarttube.utils.RemoteControlService;
import minefarts.smarttube.utils.service.data.Command;
import minefarts.smarttube.utils.helpers.MessageHelpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.rx.RxHelper;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.playback.BasePlayerController;
import minefarts.smarttube.ui.playback.PlaybackFragment2;
import minefarts.smarttube.exoplayer.selector.FormatItem;
import minefarts.smarttube.prefs.common.DataChangeBase.OnDataChange;
import minefarts.smarttube.prefs.RemoteControlData;
import minefarts.smarttube.utils.Utils;
import io.reactivex.disposables.Disposable;
import java.util.List;
import java.util.Locale;

public class RemoteController extends BasePlayerController implements OnDataChange {
    private static final String TAG = RemoteController.class.getSimpleName();
    private static final long APP_INIT_DELAY_MS = 10_000;
    private final Runnable mStartListeningInt = this::startListeningInt;
    private final RemoteControlService mRemoteControlService;
    private final RemoteControlData mRemoteControlData;
    private Disposable mListeningAction;
    private Disposable mPostStartPlayAction;
    private Disposable mPostStateAction;
    private Disposable mPostVolumeAction;
    private boolean mConnected;
    private long mNewVideoPositionMs;
    private Disposable mActionDown;
    private Disposable mActionUp;
    private ContentObserver mVolumeObserver;
    private long mVolumeSelfChangeMs;

    public RemoteController(Context context) {

        // Start receiving a commands as early as possible
        mRemoteControlService = getRemoteControlService();

        mRemoteControlData = RemoteControlData.instance(context);
        mRemoteControlData.setOnChange(this);
        
        onInit();
    }

    @Override
    public void onDataChange() { onInit(); }

    @Override
    public void onNewVideo(Video item) {
        if (item != null) {
            Log.d(TAG, "Open video. Is remote connected: %s", mConnected);
            item.isRemote = mConnected;
        }
    }

    @Override
    public void onInit() {
        if (mRemoteControlData.isDeviceLinkEnabled()) {
            if (mListeningAction != null && !mListeningAction.isDisposed()) return;
            Utils.postDelayed(mStartListeningInt, APP_INIT_DELAY_MS);
        } else {
            RxHelper.disposeActions(mListeningAction, mPostStartPlayAction, mPostStateAction, mPostVolumeAction);
            unregisterVolumeObserver();
            Utils.removeCallbacks(mStartListeningInt);
        }
    }

    @Override
    public void onViewResumed() { onInit(); }

    @Override
    public void onVideoLoaded(Video item) {
        if (getPlayer() == null) return;

        if (mNewVideoPositionMs > 0) {
            getPlayer().setPositionMs(mNewVideoPositionMs);
            mNewVideoPositionMs = 0;
        }

        postStartPlaying(item, getPlayer().getPlayWhenReady());
        if (mConnected) {
            mRemoteControlData.setLastVideo(item);
        }
    }

    @Override
    public void onPlay() {
        postPlay(true);
    }

    @Override
    public void onPause() {
        postPlay(false);
    }

    @Override
    public void onPlayEnd() {
        switch (getPlayerData().getPlaybackMode()) {
            case PlaybackFragment2.PLAYBACK_MODE_CLOSE:
            case PlaybackFragment2.PLAYBACK_MODE_PAUSE:
            case PlaybackFragment2.PLAYBACK_MODE_ALL:
                postPlay(false);
                break;
            case PlaybackFragment2.PLAYBACK_MODE_ONE:
                postStartPlaying(getVideo(), true);
                break;
        }
    }

    @Override
    public void onEngineReleased() {
        postPlay(false);
        // Below doesn't work on Vanced
        //postStartPlaying(null);
    }

    @Override
    public void onFinish() {
        // User action detected. Hide remote playlist.
        mConnected = false;
    }

    private void postStartPlaying(@Nullable Video item, boolean isPlaying) {
        if (isRemoteDisabled()) return;

        String videoId = null;
        long positionMs = -1;
        long durationMs = -1;

        if (item != null && getPlayer() != null) {
            videoId = item.videoId;
            positionMs = getPlayer().getPositionMs();
            durationMs = getPlayer().getDurationMs();
        }

        postStartPlaying(videoId, positionMs, durationMs, isPlaying);
    }

    private void postStartPlaying(String videoId, long positionMs, long durationMs, boolean isPlaying) {
        if (isRemoteDisabled()) return;

        RxHelper.disposeActions(mPostStartPlayAction);

        mPostStartPlayAction = RxHelper.execute(
                mRemoteControlService.postStartPlayingObserve(videoId, positionMs, durationMs, isPlaying)
        );
    }

    private void postState(long positionMs, long durationMs, boolean isPlaying) {
        if (isRemoteDisabled()) return;

        RxHelper.disposeActions(mPostStateAction);

        mPostStateAction = RxHelper.execute(
                mRemoteControlService.postStateChangeObserve(positionMs, durationMs, isPlaying)
        );
    }

    private void postVolumeChange(int volume) {
        if (isRemoteDisabled()) return;

        RxHelper.disposeActions(mPostVolumeAction);

        mPostVolumeAction = RxHelper.execute(
                mRemoteControlService.postVolumeChangeObserve(volume)
        );
    }

    private void postPlay(boolean isPlaying) {
        if (getPlayer() == null) return;

        postState(getPlayer().getPositionMs(), getPlayer().getDurationMs(), isPlaying);
    }

    private void startListeningInt() {
        if (mListeningAction != null && !mListeningAction.isDisposed()) return;

        mListeningAction = mRemoteControlService.getCommandObserve()
                .subscribe(
                        this::processCommand,
                        error -> {
                            String msg = "startListening error: " + error.getMessage();
                            Log.e(TAG, msg);
                            MessageHelpers.showLongMessage(getContext(), msg);
                        },
                        () -> {
                            // Some users seeing this.
                            // This msg couldn't appear in normal situation.
                            Log.d(TAG, "Remote session has been closed");
                            //MessageHelpers.showMessage(getActivity(), R.string.remote_session_closed);
                        }
                );
    }

    private void processCommand(Command command) {
        switch (command.getType()) {
            case Command.TYPE_IDLE:
            case Command.TYPE_UNDEFINED:
            case Command.TYPE_UPDATE_PLAYLIST:
                break;
            case Command.TYPE_STOP:
            case Command.TYPE_DISCONNECTED:
                mConnected = false;
                break;
            default:
                mConnected = true;
        }

        Log.d(TAG, "Is remote connected: %s, command type: %s", mConnected, command.getType());

        switch (command.getType()) {
            case Command.TYPE_OPEN_VIDEO:
                if (getPlayer() != null) {
                    getPlayer().showOverlay(false);
                }
                movePlayerToForeground();
                Video newVideo = Video.from(command.getVideoId());
                newVideo.remotePlaylistId = command.getPlaylistId();
                newVideo.playlistIndex = command.getPlaylistIndex();
                newVideo.isRemote = true;
                mNewVideoPositionMs = command.getCurrentTimeMs();
                openNewVideo(newVideo);
                break;
            case Command.TYPE_SUBTITLES:
                if (getPlayer() != null) {
                    getPlayer().showOverlay(false);
                }
                movePlayerToForeground();
                Video newVideo2 = Video.from(command.getVideoId());
                newVideo2.remotePlaylistId = command.getPlaylistId();
                newVideo2.playlistIndex = command.getPlaylistIndex();
                newVideo2.isRemote = true;
                mNewVideoPositionMs = command.getCurrentTimeMs();

                String langCode = command.getSubLanguageCode();
                if (langCode != null && !langCode.trim().isEmpty() && getPlayer() != null) {
                    List<FormatItem> subs = getPlayer().getSubtitleFormats();
                    if (subs != null) {
                        FormatItem selected = null;
                        for (FormatItem item : subs) {
                            Locale languageLocale = new Locale(langCode);
                            String currentLocale = languageLocale.getDisplayLanguage().toLowerCase();
                            if (item.getLanguage() != null && item.getLanguage().toLowerCase().contains(currentLocale)) {
                                selected = item;
                                break;
                            }
                        }

                        if (selected != null) {
                            getPlayer().setFormat(selected);
                        }
                    }
                    getPlayer().showSubtitles(true);
                    getPlayer().setButtonState(R.id.lb_control_closed_captioning, 1);
                 } else if (getPlayer() != null) {
                    getPlayer().showSubtitles(false);
                    getPlayer().setButtonState(R.id.lb_control_closed_captioning, 0);
                 }
                 openNewVideo(newVideo2);
                 break;
            case Command.TYPE_UPDATE_PLAYLIST:
                if (getPlayer() != null && mConnected) {
                    Video video = getVideo();
                    // Ensure that remote playlist already playing
                    if (video != null && video.remotePlaylistId != null) {
                        video.remotePlaylistId = command.getPlaylistId();
                        video.playlistParams = null;
                        video.isRemote = true;
                        getController(SuggestionsController.class).loadSuggestions(video);
                    }
                }
                break;
            case Command.TYPE_SEEK:
                if (getPlayer() != null) {
                    getPlayer().showOverlay(false);
                    movePlayerToForeground();
                    getPlayer().setPositionMs(command.getCurrentTimeMs());
                    postState(
                        command.getCurrentTimeMs(), 
                        getPlayer().getDurationMs(), 
                        getPlayer().isPlaying()
                    );
                } else {
                    openNewVideo(getVideo());
                }
                break;
            case Command.TYPE_PLAY:
                if (getPlayer() != null) {
                    movePlayerToForeground();
                    getPlayer().setPlayWhenReady(true);
                    //postStartPlaying(getController().getVideo(), true);
                    postPlay(true);
                } else {
                    // Already connected
                    openNewVideo(getVideo() != null ? getVideo() : mRemoteControlData.getLastVideo());
                }
                break;
            case Command.TYPE_PAUSE:
                if (getPlayer() != null) {
                    movePlayerToForeground();
                    getPlayer().setPlayWhenReady(false);
                    //postStartPlaying(getController().getVideo(), false);
                    postPlay(false);
                } else {
                    // Already connected
                    openNewVideo(getVideo() != null ? getVideo() : mRemoteControlData.getLastVideo());
                }
                break;
            case Command.TYPE_NEXT:
                if (getMainController() != null) {
                    movePlayerToForeground();
                    getController(VideoLoaderController.class).onNextClicked();
                } else {
                    openNewVideo(getVideo());
                }
                break;
            case Command.TYPE_PREVIOUS:
                if (getMainController() != null && getPlayer() != null) {
                    movePlayerToForeground();
                    // Switch immediately. Skip position reset logic.
                    getController(VideoLoaderController.class).onPreviousClicked();
                } else {
                    openNewVideo(getVideo());
                }
                break;
            case Command.TYPE_GET_STATE:
                if (getPlayer() != null) {
                    getViewManager().moveAppToForeground();
                    postStartPlaying(getVideo(), getPlayer().isPlaying());
                } else {
                    postStartPlaying(null, false);
                }
                break;
            case Command.TYPE_VOLUME:
                int volume = command.getVolume();

                if (command.getDelta() != -1) { // using phone volume sliders
                    volume = Utils.getVolume(getPlayer()) + command.getDelta();
                }

                Utils.setVolume(getContext(), getPlayer(), volume);
                mVolumeSelfChangeMs = System.currentTimeMillis();

                if (command.getDelta() != -1) { // using phone volume sliders
                    postVolumeChange(Utils.getVolume(getPlayer()));
                } else {
                    postVolumeChange(volume);
                }
                break;
            case Command.TYPE_STOP:
                // Close player
                if (getPlayer() != null) {
                    getPlayer().finish();
                }

                break;
            case Command.TYPE_CONNECTED:
                registerVolumeObserver();
                mRemoteControlData.setConnectedBefore(true);
                break;
            case Command.TYPE_DISCONNECTED:
                // NOTE: there are possible false calls when mobile client unloaded from the memory.
                if (getContext() != null && mRemoteControlData.isFinishOnDisconnectEnabled()) {
                    // NOTE: It's not a good idea to remember connection state (mConnected) at this point.
                    MessageHelpers.showLongMessage(getContext(), getContext().getString(R.string.device_disconnected, command.getDeviceName()));
                    Utils.properlyFinishTheApp(getContext());
                }

                unregisterVolumeObserver();
                mRemoteControlData.setConnectedBefore(false);
                break;
            case Command.TYPE_IDLE:
                // Already connected
                if (isConnectedBefore()) {
                    registerVolumeObserver();
                }
                break;
            case Command.TYPE_DPAD:
                int key = KeyEvent.KEYCODE_UNKNOWN;
                boolean isLongAction = false;
                switch (command.getKey()) {
                    case Command.KEY_UP:
                        key = KeyEvent.KEYCODE_DPAD_UP;
                        break;
                    case Command.KEY_DOWN:
                        key = KeyEvent.KEYCODE_DPAD_DOWN;
                        break;
                    case Command.KEY_LEFT:
                        key = KeyEvent.KEYCODE_DPAD_LEFT;
                        isLongAction = true; // enable fast seeking
                        break;
                    case Command.KEY_RIGHT:
                        key = KeyEvent.KEYCODE_DPAD_RIGHT;
                        isLongAction = true; // enable fast seeking
                        break;
                    case Command.KEY_ENTER:
                        key = KeyEvent.KEYCODE_DPAD_CENTER;
                        break;
                    case Command.KEY_BACK:
                        key = KeyEvent.KEYCODE_BACK;
                        break;
                }
                if (key != KeyEvent.KEYCODE_UNKNOWN) {
                    RxHelper.disposeActions(mActionDown, mActionUp);

                    final int resultKey = key;

                    if (isLongAction) {
                        mActionDown = RxHelper.runAsync(() ->
                                Utils.sendKey(new KeyEvent(KeyEvent.ACTION_DOWN, resultKey)));
                        mActionUp = RxHelper.runAsync(() ->
                                Utils.sendKey(new KeyEvent(KeyEvent.ACTION_UP, resultKey)), 500);
                    } else {
                        mActionDown = RxHelper.runAsync(() -> Utils.sendKey(resultKey));
                    }
                }
                break;
            case Command.TYPE_VOICE:
                if (command.isVoiceStarted()) {
                    getSearchPresenter().startVoice();
                } else {
                    getSearchPresenter().forceFinish();
                }
                break;
        }
    }

    private void openNewVideo(Video newVideo) {
        if (Video.equals(getVideo(), newVideo) && getViewManager().isPlayerInForeground()) { // same video already playing
            //getVideo().playlistId = newVideo.playlistId;
            //getVideo().playlistIndex = newVideo.playlistIndex;
            //getVideo().playlistParams = newVideo.playlistParams;
            if (mNewVideoPositionMs > 0) {
                getPlayer().setPositionMs(mNewVideoPositionMs);
                mNewVideoPositionMs = 0;
            }
            postStartPlaying(getVideo(), getPlayer().isPlaying());
        } else if (newVideo != null) {
            newVideo.isRemote = true;
            getPlaybackPresenter().openVideo(newVideo);
        }
    }

    private void movePlayerToForeground() {
        getViewManager().movePlayerToForeground();
        // Device wake fix when player isn't started yet or been closed
        if (getPlayer() == null || !Utils.checkActivity(getActivity())) {
            new Handler(Looper.myLooper()).postDelayed(() -> getViewManager().movePlayerToForeground(), 5_000);
        }
    }

    private void registerVolumeObserver() {
        if (mVolumeObserver != null) return;

        mVolumeObserver = new ContentObserver(Utils.sHandler) {
            @Override
            public void onChange(boolean selfChange) {
                if (System.currentTimeMillis() - mVolumeSelfChangeMs > 1_000) {
                    postVolumeChange(Utils.getVolume(getPlayer()));
                }
            }
        };
        Utils.registerAudioObserver(getContext(), mVolumeObserver);

        postVolumeChange(Utils.getVolume(getPlayer()));
    }

    private void unregisterVolumeObserver() {
        if (mVolumeObserver != null) {
            Utils.unregisterAudioObserver(getContext(), mVolumeObserver);
            mVolumeObserver = null;
        }
    }

    private boolean isConnectedBefore() {
        return mConnected || mRemoteControlData.isConnectedBefore();
    }

    private boolean isRemoteDisabled() {
        return !mRemoteControlData.isDeviceLinkEnabled() || !isConnectedBefore();
    }
}
