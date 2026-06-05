package minefarts.smarttube.leanback.media;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import minefarts.smarttube.leanback.media.MediaPlayerAdapter;
import minefarts.smarttube.leanback.media.PlaybackGlueHost;
import minefarts.smarttube.leanback.media.PlayerAdapter;
import minefarts.smarttube.leanback.widget.AbstractDetailsDescriptionPresenter;
import minefarts.smarttube.leanback.widget.Action;
import minefarts.smarttube.leanback.widget.ArrayObjectAdapter;
import minefarts.smarttube.leanback.widget.ObjectAdapter;
import minefarts.smarttube.leanback.widget.PlaybackControlsRow;
import minefarts.smarttube.leanback.widget.PlaybackRowPresenter;
import minefarts.smarttube.leanback.widget.PlaybackSeekDataProvider;
import minefarts.smarttube.leanback.widget.PlaybackSeekUi;
import minefarts.smarttube.leanback.widget.PlaybackTransportRowPresenter;
import minefarts.smarttube.leanback.widget.RowPresenter;

import java.lang.ref.WeakReference;

public class PlaybackTransportControlGlue<T extends PlayerAdapter>
        extends PlaybackBaseControlGlue<T> {

    static final String TAG = "PlaybackTransportGlue";
    static final boolean DEBUG = false;

    static final int MSG_UPDATE_PLAYBACK_STATE = 100;
    static final int UPDATE_PLAYBACK_STATE_DELAY_MS = 2000;

    PlaybackSeekDataProvider mSeekProvider;
    boolean mSeekEnabled;

    static class UpdatePlaybackStateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE_PLAYBACK_STATE) {
                PlaybackTransportControlGlue glue =
                        ((WeakReference<PlaybackTransportControlGlue>) msg.obj).get();
                if (glue != null) {
                    glue.onUpdatePlaybackState();
                }
            }
        }
    }

    static final Handler sHandler = new UpdatePlaybackStateHandler();

    final WeakReference<PlaybackBaseControlGlue> mGlueWeakReference =  new WeakReference(this);

    /**
     * Constructor for the glue.
     *
     * @param context
     * @param impl Implementation to underlying media player.
     */
    public PlaybackTransportControlGlue(Context context, T impl) {
        super(context, impl);
    }

    @Override
    public void setControlsRow(PlaybackControlsRow controlsRow) {
        super.setControlsRow(controlsRow);
        sHandler.removeMessages(MSG_UPDATE_PLAYBACK_STATE, mGlueWeakReference);
        onUpdatePlaybackState();
    }

    @Override
    protected void onCreatePrimaryActions(ArrayObjectAdapter primaryActionsAdapter) {
        primaryActionsAdapter.add(mPlayPauseAction =
                new PlaybackControlsRow.PlayPauseAction(getContext()));
    }

    @Override
    protected PlaybackRowPresenter onCreateRowPresenter() {
        final AbstractDetailsDescriptionPresenter detailsPresenter =
                new AbstractDetailsDescriptionPresenter() {
                    @Override
                    protected void onBindDescription(ViewHolder
                            viewHolder, Object obj) {
                        PlaybackBaseControlGlue glue = (PlaybackBaseControlGlue) obj;
                        viewHolder.getTitle().setText(glue.getTitle());
                        viewHolder.getSubtitle().setText(glue.getSubtitle());
                        // MOD: add extra title line
                        viewHolder.getBody().setText(glue.getBody());
                    }
                };

        PlaybackTransportRowPresenter rowPresenter = new PlaybackTransportRowPresenter() {
            @Override
            protected void onBindRowViewHolder(RowPresenter.ViewHolder vh, Object item) {
                super.onBindRowViewHolder(vh, item);
                vh.setOnKeyListener(PlaybackTransportControlGlue.this);
            }
            @Override
            protected void onUnbindRowViewHolder(RowPresenter.ViewHolder vh) {
                super.onUnbindRowViewHolder(vh);
                vh.setOnKeyListener(null);
            }
        };
        rowPresenter.setDescriptionPresenter(detailsPresenter);
        return rowPresenter;
    }

    @Override
    protected void onAttachedToHost(PlaybackGlueHost host) {
        super.onAttachedToHost(host);

        if (host instanceof PlaybackSeekUi) {
            ((PlaybackSeekUi) host).setPlaybackSeekUiClient(mPlaybackSeekUiClient);
        }
    }

    @Override
    protected void onDetachedFromHost() {
        super.onDetachedFromHost();

        if (getHost() instanceof PlaybackSeekUi) {
            ((PlaybackSeekUi) getHost()).setPlaybackSeekUiClient(null);
        }
    }

    @Override
    protected void onUpdateProgress() {
        if (!mPlaybackSeekUiClient.mIsSeek) {
            super.onUpdateProgress();
        }
    }

    @Override
    public void onActionClicked(Action action) {
        dispatchAction(action, null);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
                return false;
        }

        final ObjectAdapter primaryActionsAdapter = mControlsRow.getPrimaryActionsAdapter();
        Action action = mControlsRow.getActionForKeyCode(primaryActionsAdapter, keyCode);
        if (action == null) {
            action = mControlsRow.getActionForKeyCode(mControlsRow.getSecondaryActionsAdapter(),
                    keyCode);
        }

        if (action != null) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                dispatchAction(action, event);
            }
            return true;
        }
        return false;
    }

    void onUpdatePlaybackStatusAfterUserAction() {
        updatePlaybackState(mIsPlaying);

        // Sync playback state after a delay
        sHandler.removeMessages(MSG_UPDATE_PLAYBACK_STATE, mGlueWeakReference);
        sHandler.sendMessageDelayed(sHandler.obtainMessage(MSG_UPDATE_PLAYBACK_STATE,
                mGlueWeakReference), UPDATE_PLAYBACK_STATE_DELAY_MS);
    }

    /**
     * Called when the given action is invoked, either by click or keyevent.
     */
    boolean dispatchAction(Action action, KeyEvent keyEvent) {
        boolean handled = false;
        if (action instanceof PlaybackControlsRow.PlayPauseAction) {
            boolean canPlay = keyEvent == null
                    || keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                    || keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY;
            boolean canPause = keyEvent == null
                    || keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                    || keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PAUSE;
            //            PLAY_PAUSE    PLAY      PAUSE
            // playing    paused                  paused
            // paused     playing       playing
            // ff/rw      playing       playing   paused
            if (canPause && mIsPlaying) {
                mIsPlaying = false;
                pause();
            } else if (canPlay && !mIsPlaying) {
                mIsPlaying = true;
                play();
            }
            onUpdatePlaybackStatusAfterUserAction();
            handled = true;
        } else if (action instanceof PlaybackControlsRow.SkipNextAction) {
            next();
            handled = true;
        } else if (action instanceof PlaybackControlsRow.SkipPreviousAction) {
            previous();
            handled = true;
        }
        return handled;
    }

    @Override
    protected void onPlayStateChanged() {
        if (DEBUG) Log.v(TAG, "onStateChanged");

        if (sHandler.hasMessages(MSG_UPDATE_PLAYBACK_STATE, mGlueWeakReference)) {
            sHandler.removeMessages(MSG_UPDATE_PLAYBACK_STATE, mGlueWeakReference);
            if (mPlayerAdapter.isPlaying() != mIsPlaying) {
                if (DEBUG) Log.v(TAG, "Status expectation mismatch, delaying update");
                sHandler.sendMessageDelayed(sHandler.obtainMessage(MSG_UPDATE_PLAYBACK_STATE,
                        mGlueWeakReference), UPDATE_PLAYBACK_STATE_DELAY_MS);
            } else {
                if (DEBUG) Log.v(TAG, "Update state matches expectation");
                onUpdatePlaybackState();
            }
        } else {
            onUpdatePlaybackState();
        }

        super.onPlayStateChanged();
    }

    void onUpdatePlaybackState() {
        mIsPlaying = mPlayerAdapter.isPlaying();
        updatePlaybackState(mIsPlaying);
    }

    private void updatePlaybackState(boolean isPlaying) {
        if (mControlsRow == null) {
            return;
        }

        if (!isPlaying) {
            onUpdateProgress();
            mPlayerAdapter.setProgressUpdatingEnabled(mPlaybackSeekUiClient.mIsSeek);
        } else {
            mPlayerAdapter.setProgressUpdatingEnabled(true);
        }

        if (mFadeWhenPlaying && getHost() != null) {
            getHost().setControlsOverlayAutoHideEnabled(isPlaying);
        }

        if (mPlayPauseAction != null) {
            int index = !isPlaying
                    ? PlaybackControlsRow.PlayPauseAction.INDEX_PLAY
                    : PlaybackControlsRow.PlayPauseAction.INDEX_PAUSE;
            if (mPlayPauseAction.getIndex() != index) {
                mPlayPauseAction.setIndex(index);
                notifyItemChanged((ArrayObjectAdapter) getControlsRow().getPrimaryActionsAdapter(),
                        mPlayPauseAction);
            }
        }
    }

    final SeekUiClient mPlaybackSeekUiClient = new SeekUiClient();

    class SeekUiClient extends PlaybackSeekUi.Client {
        boolean mPausedBeforeSeek;
        long mPositionBeforeSeek;
        long mLastUserPosition;
        boolean mIsSeek;

        @Override
        public PlaybackSeekDataProvider getPlaybackSeekDataProvider() {
            return mSeekProvider;
        }

        @Override
        public boolean isSeekEnabled() {
            return mSeekProvider != null || mSeekEnabled;
        }

        @Override
        public void onSeekStarted() {
            mIsSeek = true;
            mPausedBeforeSeek = !isPlaying();
            mPlayerAdapter.setProgressUpdatingEnabled(true);
            // if we seek thumbnails, we don't need save original position because current
            // position is not changed during seeking.
            // otherwise we will call seekTo() and may need to restore the original position.
            mPositionBeforeSeek = mSeekProvider == null ? mPlayerAdapter.getCurrentPosition() : -1;
            mLastUserPosition = -1;
            pause();

        }

        @Override
        public void onSeekPositionChanged(long pos) {
            if (mSeekProvider == null) {
                mPlayerAdapter.seekTo(pos);
            } else {
                mLastUserPosition = pos;
            }
            if (mControlsRow != null) {
                mControlsRow.setCurrentPosition(pos);
            }
        }

        @Override
        public void onSeekFinished(boolean cancelled) {
            if (!cancelled) {
                if (mLastUserPosition >= 0) {
                    seekTo(mLastUserPosition);
                }
            } else {
                if (mPositionBeforeSeek >= 0) {
                    seekTo(mPositionBeforeSeek);
                }
            }
            mIsSeek = false;
            if (!mPausedBeforeSeek) {
                play();
            } else {
                mPlayerAdapter.setProgressUpdatingEnabled(false);
                // we neeed update UI since PlaybackControlRow still saves previous position.
                onUpdateProgress();
            }
        }
    };

    /**
     * Set seek data provider used during user seeking.
     * @param seekProvider Seek data provider used during user seeking.
     */
    public final void setSeekProvider(PlaybackSeekDataProvider seekProvider) {
        mSeekProvider = seekProvider;
    }

    /**
     * Get seek data provider used during user seeking.
     * @return Seek data provider used during user seeking.
     */
    public final PlaybackSeekDataProvider getSeekProvider() {
        return mSeekProvider;
    }

    /**
     * Enable or disable seek when {@link #getSeekProvider()} is null. When true,
     * {@link PlayerAdapter#seekTo(long)} will be called during user seeking.
     *
     * @param seekEnabled True to enable seek, false otherwise
     */
    public final void setSeekEnabled(boolean seekEnabled) {
        mSeekEnabled = seekEnabled;
    }

    /**
     * @return True if seek is enabled without {@link PlaybackSeekDataProvider}, false otherwise.
     */
    public final boolean isSeekEnabled() {
        return mSeekEnabled;
    }
}
