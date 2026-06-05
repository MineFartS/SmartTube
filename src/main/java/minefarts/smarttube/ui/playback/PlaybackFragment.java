package minefarts.smarttube.ui.playback;

import android.app.Activity;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.SurfaceHolder;
import android.os.Handler;
import android.view.InputEvent;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import minefarts.smarttube.leanback.app.RowsSupportFragment;
import minefarts.smarttube.leanback.media.PlayerAdapter;
import minefarts.smarttube.leanback.widget.ArrayObjectAdapter;
import minefarts.smarttube.leanback.widget.ClassPresenterSelector;
import minefarts.smarttube.leanback.widget.HeaderItem;
import minefarts.smarttube.leanback.widget.ListRow;
import minefarts.smarttube.leanback.widget.ListRowPresenter;
import minefarts.smarttube.leanback.widget.ObjectAdapter;
import minefarts.smarttube.leanback.widget.OnItemViewSelectedListener;
import minefarts.smarttube.leanback.widget.Presenter;
import minefarts.smarttube.leanback.widget.Row;
import minefarts.smarttube.leanback.widget.RowPresenter;
import minefarts.smarttube.leanback.widget.RowPresenter.ViewHolder;
import minefarts.smarttube.leanback.widget.PlaybackSeekDataProvider;
import minefarts.smarttube.leanback.widget.PlaybackSeekUi;
import minefarts.smarttube.leanback.widget.VerticalGridView;
import minefarts.smarttube.leanback.media.LeanbackPlayerAdapter;
import minefarts.smarttube.ControlDispatcher;
import minefarts.smarttube.DefaultControlDispatcher;
import minefarts.smarttube.DefaultRenderersFactory;
import minefarts.smarttube.Player;
import minefarts.smarttube.SimpleExoPlayer;
import minefarts.smarttube.ms.MediaSessionConnector;
import minefarts.smarttube.trackselection.AdaptiveTrackSelection;
import minefarts.smarttube.trackselection.DefaultTrackSelector;
import minefarts.smarttube.utils.Utils;
import minefarts.smarttube.utils.data.MediaItemFormatInfo;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.data.VideoGroup;
import minefarts.smarttube.app.models.playback.ui.ChatReceiver;
import minefarts.smarttube.app.models.playback.ui.SeekBarSegment;
import minefarts.smarttube.app.presenters.PlaybackPresenter;
import minefarts.smarttube.exoplayer.controller.ExoPlayerController;
import minefarts.smarttube.exoplayer.other.SubtitleManager;
import minefarts.smarttube.exoplayer.selector.FormatItem;
import minefarts.smarttube.exoplayer.versions.selector.RestoreTrackSelector;
import minefarts.smarttube.R;
import minefarts.smarttube.adapter.VideoGroupObjectAdapter;
import minefarts.smarttube.presenter.CustomListRowPresenter;
import minefarts.smarttube.presenter.ShortsCardPresenter;
import minefarts.smarttube.presenter.VideoCardPresenter;
import minefarts.smarttube.presenter.base.OnItemLongPressedListener;
import minefarts.smarttube.ui.browse.video.GridFragmentHelper;
import minefarts.smarttube.ui.common.LeanbackActivity;
import minefarts.smarttube.ui.common.UriBackgroundManager;
import minefarts.smarttube.ui.mod.leanback.misc.ProgressBarManager;
import minefarts.smarttube.ui.playback.other.BackboneQueueNavigator;
import minefarts.smarttube.ui.playback.other.VideoPlayerGlue;
import minefarts.smarttube.ui.playback.other.VideoPlayerGlue.OnActionClickedListener;
import minefarts.smarttube.ui.playback.previewtimebar.StoryboardSeekDataProvider;
import minefarts.smarttube.ui.widgets.chat.LiveChatView;
import minefarts.smarttube.google.common.helpers.YouTubeHelper;
import minefarts.smarttube.ui.playback.mod.surface.SurfacePlaybackFragment;
import minefarts.smarttube.text.CaptionStyleCompat;
import minefarts.smarttube.utils.helpers.DeviceHelpers;
import minefarts.smarttube.utils.locale.LocaleUtility;
import minefarts.smarttube.exoplayer.selector.ExoFormatItem;
import minefarts.smarttube.exoplayer.selector.track.MediaTrack;
import minefarts.smarttube.prefs.AppPrefs.ProfileChangeListener;
import minefarts.smarttube.prefs.AppPrefs;
import minefarts.smarttube.exoplayer.selector.TrackSelectorManager;
import minefarts.smarttube.C;
import minefarts.smarttube.DefaultLoadControl;
import minefarts.smarttube.audio.AudioAttributes;
import minefarts.smarttube.upstream.BandwidthMeter;
import minefarts.smarttube.ui.playback.PlaybackFragment;
import minefarts.smarttube.prefs.PlayerTweaksData;
import minefarts.smarttube.analytics.AnalyticsCollector;
import minefarts.smarttube.trackselection.DefaultTrackSelector;
import minefarts.smarttube.upstream.DefaultBandwidthMeter;
import minefarts.smarttube.exoplayer.other.VolumeBooster;
import minefarts.smarttube.leanback.app.PlaybackSupportFragmentGlueHost;
import minefarts.smarttube.leanback.media.PlaybackGlue;
import minefarts.smarttube.leanback.media.PlaybackGlueHost;
import minefarts.smarttube.leanback.media.SurfaceHolderGlueHost;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * {@link PlaybackGlueHost} implementation
 * the interaction between {@link PlaybackGlue} and {@link minefarts.smarttube.leanback.app.VideoSupportFragment}.
 */
class SurfacePlaybackFragmentGlueHost extends PlaybackSupportFragmentGlueHost implements SurfaceHolderGlueHost {

    @SuppressWarnings("HidingField") // Supertype field is package scope to avoid synthetic accessor
    private final SurfacePlaybackFragment mFragment;

    public SurfacePlaybackFragmentGlueHost(SurfacePlaybackFragment fragment) {
        super(fragment);
        this.mFragment = fragment;
    }

    /**
     * Sets the {@link SurfaceHolder.Callback} on the host.
     * {@link PlaybackGlueHost} is assumed to either host the {@link SurfaceHolder} or
     * have a reference to the component hosting it for rendering the video.
     */
    @Override
    public void setSurfaceHolderCallback(SurfaceHolder.Callback callback) {
        mFragment.setSurfaceHolderCallback(callback);
    }

}

public class PlaybackFragment 
    extends SurfacePlaybackFragment 
    implements ProfileChangeListener
{

    public static final int PLAYBACK_MODE_PAUSE = 0;
    public static final int PLAYBACK_MODE_CLOSE = 1;
    public static final int PLAYBACK_MODE_ALL = 2;
    public static final int PLAYBACK_MODE_ONE = 3;
    public static final int PLAYBACK_MODE_SHUFFLE = 4;
    public static final int PLAYBACK_MODE_LIST = 5;
    public static final int PLAYBACK_MODE_REVERSE_LIST = 6;
    
    public static final float ASPECT_RATIO_1_1 = 1f;
    public static final float ASPECT_RATIO_4_3 = 1.33f;
    public static final float ASPECT_RATIO_5_4 = 1.25f;
    public static final float ASPECT_RATIO_16_9 = 1.77f;
    public static final float ASPECT_RATIO_16_10 = 1.6f;
    public static final float ASPECT_RATIO_21_9 = 2.33f;
    public static final float ASPECT_RATIO_64_27 = 2.37f;
    public static final float ASPECT_RATIO_221_1 = 2.21f;
    public static final float ASPECT_RATIO_235_1 = 2.35f;
    public static final float ASPECT_RATIO_239_1 = 2.39f;

    private static final String TAG = PlaybackFragment.class.getSimpleName();

    private static final String SELECTED_VIDEO_ID = "SelectedVideoId";
    private static final int UPDATE_DELAY_MS = 100;
    private static final int SUGGESTIONS_START_INDEX = 1;
    private static final int START_FADE_OUT = 1;

    private PlaybackSeekUi.Client mSeekUiClient2;
    private boolean mInSeek;
    private VideoPlayerGlue mPlayerGlue;
    private SimpleExoPlayer mPlayer;
    private PlaybackPresenter mPlaybackPresenter;
    private ArrayObjectAdapter mRowsAdapter;
    private ListRowPresenter mRowPresenter;
    private VideoCardPresenter mCardPresenter;
    private ShortsCardPresenter mShortsPresenter;
    private Map<Integer, VideoGroupObjectAdapter> mVideoGroupAdapters;
    private ExoPlayerController mExoPlayerController;
    private TrackSelectorManager mTrackSelectorManager;
    private SubtitleManager mSubtitleManager;
    private UriBackgroundManager mBackgroundManager;
    private RowsSupportFragment mRowsSupportFragment;
    private boolean mIsEngineBlocked;
    private MediaSessionCompat mMediaSession;
    private MediaSessionConnector mMediaSessionConnector;
    private Boolean mIsControlsShownPreviously;
    private Video mPendingFocus;
    private long mProgressShowTimeMs;
    private String mSelectedVideoId;

    private static final String VIDEO_PLAYER_DATA = "video_player_data";

    public static final int ONLY_UI = 0;
    public static final int UI_AND_PAUSE = 1;
    public static final int ONLY_PAUSE = 2;
    public static final int AUTO_HIDE_NEVER = 0;

    @SuppressLint("StaticFieldLeak")
    private static PlaybackFragment sInstance;
    private AppPrefs mPrefs;

    private FormatItem mVideoFormat;
    private FormatItem mAudioFormat;
    private FormatItem mSubtitleFormat;
    private boolean mIsAfrEnabled;
    private boolean mIsAfrFpsCorrectionEnabled;
    private boolean mIsAfrResSwitchEnabled;
    private int mAudioDelayMs;
    private String mAudioLanguage;
    private String mSubtitleLanguage;
    private int mPlaybackMode;
    private boolean mIsTimeCorrectionEnabled;
    private boolean mIsDoubleRefreshRateEnabled;
    private float mSubtitleScale;
    private float mPlayerVolume;
    private float mSubtitlePosition;
    private boolean mIsSkip24RateEnabled;
    private boolean mIsSkipShortsEnabled;
    private boolean mIsLiveChatEnabled;
    private List<FormatItem> mLastSubtitleFormats;
    private float mPitch;
    private List<String> mLastAudioLanguages;

    private final int mMaxBufferBytes;
    private final PlaybackFragment mPlayerData;
    private final PlayerTweaksData mPlayerTweaksData;
    private static AudioAttributes sAudioAttributes;

    private static BandwidthMeter mBandwidthMeter;

    // Required for Android XML fragment inflation
    public PlaybackFragment() {
        this(null);
    }

    private PlaybackFragment(Context context) {
        
        mPrefs = AppPrefs.instance(context);
        mPrefs.addListener(this);

        mPlayerData = PlaybackFragment.instance(context);
        mPlayerTweaksData = PlayerTweaksData.instance(context);

        mBandwidthMeter = new DefaultBandwidthMeter.Builder(context).build();

        long deviceRam = DeviceHelpers.getDeviceRam(context);

        // If ram is too big, bigger then max int value DeviceRam will return a negative number...
        // use 196MB as that can only happens if device has more than 17GB of RAM, so 196 is enough and safe
        // https://github.com/yuliskov/SmartYouTubeTV/issues/532
        mMaxBufferBytes = deviceRam <= 0 ? 196_000_000 : (int)(deviceRam / 18);

        restoreState();
    }

    public static PlaybackFragment instance(Context context) {
        
        if (context != null) 
            context = context.getApplicationContext();

        if (sInstance == null)
            sInstance = new PlaybackFragment(context);
        
        return sInstance;
    }

    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);

        Object onTouchInterceptListener = Helpers.getField(this, "mOnTouchInterceptListener");

        if (onTouchInterceptListener != null) {
            Helpers.setField(this, "mOnTouchInterceptListener", (VerticalGridView.OnTouchInterceptListener) this::onInterceptInputEvent);
        }

        Object onKeyInterceptListener = Helpers.getField(this, "mOnKeyInterceptListener");

        if (onKeyInterceptListener != null) {
            Helpers.setField(this, "mOnKeyInterceptListener", (VerticalGridView.OnKeyInterceptListener) this::onInterceptInputEvent);
        }

        Object chainedClient = Helpers.getField(this, "mChainedClient");

        if (chainedClient != null) {
            Helpers.setField(this, "mChainedClient", mChainedClient2);
        }

        mSelectedVideoId = savedInstanceState != null ? savedInstanceState.getString(SELECTED_VIDEO_ID, null) : null;
        mVideoGroupAdapters = new HashMap<>();
        mBackgroundManager = getLeanbackActivity().getBackgroundManager();
        mBackgroundManager.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.player_background));

        mPlaybackPresenter = PlaybackPresenter.instance(getContext());
        mPlaybackPresenter.setView(this);
        mExoPlayerController = new ExoPlayerController(getContext(), mPlaybackPresenter);
        mTrackSelectorManager = mExoPlayerController.mTrackSelectorManager;

        // Fix open previous video
        if (mPlaybackPresenter.getVideo() != null) {
            mSelectedVideoId = null;
        }

        initPresenters();
        setupEventListeners();
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPlaybackPresenter.onViewInitialized();

        if (mSelectedVideoId != null) {
            mPlaybackPresenter.openVideo(mSelectedVideoId);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        // We should use internal progress manager because it's used in many places like Exo engine etc.
        // ProgressBar.setRootView already called at this moment.
        ProgressBarManager.setup(getProgressBarManager(), (ViewGroup) root);

        return root;
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store position in case activity is crashed
        outState.putString(SELECTED_VIDEO_ID, getVideo() != null ? getVideo().videoId : null);
    }

    /**
     * Update background depending what's shown: controls or suggestions
     */
    private void updatePlayerBackground() {
        if (isOverlayShown()) {
            setBackgroundResource(isSuggestionsShown() ? R.drawable.player_background_suggestions : R.drawable.player_background_controls);
        }
    }

    // NOTE: depending of SDK version Start/Stop may be called with delay (SDK_INT > 23) or not called at all (PIP/Dialogs)!

    /**
     * Not called when using PIP or Dialogs on API >= 24
     */
    public void onStart() {
        super.onStart();

        // Fix controls pop-up on Activity start/resume.
        // Should be called on earlier stage.
        hideControlsOverlay(true);

        if (Utils.SDK_INT > 23) {
            initializePlayer();
        }
    }

    /**
     * Not called when using PIP or Dialogs on API >= 24
     */
    public void onStop() {
        super.onStop();

        if (Utils.SDK_INT > 23) {
            maybeReleasePlayer();
        }
    }

    public void onResume() {
        super.onResume();

        if ((Utils.SDK_INT <= 23 || mPlayer == null)) {
            initializePlayer();
        }

        // NOTE: don't move this into another place! Multiple components rely on it.
        mPlaybackPresenter.onViewResumed();

        showHideWidgets(true); // PIP mode fix
        blockEngine(false); // reset bg mode
    }

    public void onPause() {
        super.onPause();

        // NOTE: don't move this into another place! Multiple components rely on it.
        mPlaybackPresenter.onViewPaused();

        if (Utils.SDK_INT <= 23) {
            maybeReleasePlayer();
        }

        showHideWidgets(false); // PIP mode fix
    }

    public void onDispatchKeyEvent(KeyEvent event) {
        // NOP
    }

    public void onDispatchTouchEvent(MotionEvent event) {
        applyTickle(event);
    }

    public void onDispatchGenericMotionEvent(MotionEvent event) {
        applyTickle(event);
    }

    private void applyTickle(MotionEvent event) {
        int gestureAreaWidthPx = 100;

        // Reserve left area for gestures
        if (event.getAxisValue(MotionEvent.AXIS_X) < gestureAreaWidthPx) {
            return;
        }

        // Reserve right area for gestures
        if (getActivity() != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            if (event.getAxisValue(MotionEvent.AXIS_X) > (displayMetrics.widthPixels - gestureAreaWidthPx)) {
                return;
            }
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            tickle(); // show Player UI
        }

        mPlaybackPresenter.onKeyDown(-1); // reset ui timer
    }

    public void onFinish() {
        if (Utils.SDK_INT > 23) {
            maybeReleasePlayer();
        }

        mPlaybackPresenter.onFinish();
    }

    public void onPIPChanged(boolean isInPIP) {
        if (!isInPIP) {
            // Fix partially disappeared buttons after exit from PIP???
            notifyPlaybackRowChanged();
        }
    }

    protected void onSeekPositionChanged(long positionMs) {
        mPlaybackPresenter.onSeekPositionChanged(positionMs);
    }

    public void skipToNext() {
        if (mPlayerGlue != null) {
            mPlayerGlue.next();
        }
    }

    public void skipToPrevious() {
        if (mPlayerGlue != null) {
            mPlayerGlue.previous();
        }
    }

    public void rewind() {
        if (mPlayerGlue != null) {
            mPlayerGlue.rewind();
        }
    }

    public void fastForward() {
        if (mPlayerGlue != null) {
            mPlayerGlue.fastForward();
        }
    }

    private int getPlayerRowIndex() {
        int selectedPosition = 0;

        if (mRowsSupportFragment != null && mRowsSupportFragment.getVerticalGridView() != null) {
            selectedPosition = mRowsSupportFragment.getVerticalGridView().getSelectedPosition();
        }

        return selectedPosition;
    }

    private void setPlayerRowIndex(int index) {
        if (mRowsSupportFragment != null && mRowsSupportFragment.getVerticalGridView() != null) {
            mRowsSupportFragment.getVerticalGridView().setSelectedPosition(index);
        }
    }

    public void restartEngine() {
        if (isDetached() || getContext() == null) {
            Log.e(TAG, "Can't restart engine. Seems that player activity is being destroyed.");
            return;
        }

        releasePlayer();
        // Improve memory usage??? Player may hangs on a second after close
        Runtime.getRuntime().gc();
        initializePlayer();
    }

    public void reloadPlayback() {
        if (mPlayer != null) {
            mPlaybackPresenter.onEngineReleased();
            mPlaybackPresenter.onEngineInitialized();
        }
    }

    /**
     * Internal method. Intended for enclosed Activity.
     */
    public void maybeReleasePlayer() {
        if (isEngineBlocked()) {
            Log.d(TAG, "releasePlayer: Playback activity is blocked. Exiting...");
            return;
        }

        releasePlayer();
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            Log.d(TAG, "releasePlayer: Start releasing player engine...");

            // Guard against partially-initialized controller state.
            if (mPlaybackPresenter != null) {
                mPlaybackPresenter.onEngineReleased();
            }

            // Fix access calls when player isn't initialized.
            if (mExoPlayerController != null) {
                mExoPlayerController.release();
            }

            if (mMediaSessionConnector != null) {
                mMediaSessionConnector.setPlayer(null);
            }
            if (mMediaSession != null) {
                mMediaSession.release();
            }
            if (mRowsAdapter != null) {
                mRowsAdapter.clear();
            }

            setAdapter(null); // PlayerGlue->LeanbackPlayerAdapter->Context memory leak fix

            mPlayer = null;
            mPlayerGlue = null;
            mRowsAdapter = null;
            mSubtitleManager = null;
            mMediaSessionConnector = null;
            mMediaSession = null;

        }
    }

    public SimpleExoPlayer createPlayer(
        DefaultRenderersFactory renderersFactory, 
        DefaultTrackSelector trackSelector
    ) {
        DefaultLoadControl.Builder baseBuilder = new DefaultLoadControl.Builder();

        int bufferForPlaybackMs = 2_500;
        int bufferForPlaybackAfterRebufferMs = 5_000;

        int minBufferMs = 50_000;
        int maxBufferMs = 100_000;
        
        baseBuilder.setTargetBufferBytes(mMaxBufferBytes);
        
        baseBuilder.setBackBuffer(minBufferMs, true);

        baseBuilder.setBufferDurationsMs(
            minBufferMs, 
            maxBufferMs, 
            bufferForPlaybackMs, 
            bufferForPlaybackAfterRebufferMs
        );

        DefaultLoadControl loadControl = baseBuilder.createDefaultLoadControl();

        SimpleExoPlayer player = new SimpleExoPlayer(
            getContext(),
            renderersFactory,
            trackSelector,
            loadControl,
            null, // drmSessionManager
            mBandwidthMeter,
            new AnalyticsCollector.Factory(),
            Utils.getLooper()
        );

        if (player != null) {
            try {
                player.setAudioAttributes(getAudioAttributes(), true);
            } catch (SecurityException e) { // uid 10390 not allowed to perform TAKE_AUDIO_FOCUS
                e.printStackTrace();
            }
        }

        float volume = 2.0f;

        if (volume > 1f && Build.VERSION.SDK_INT >= 19) {
            VolumeBooster mVolumeBooster = new VolumeBooster(true, volume, player);
            player.addAudioListener(mVolumeBooster);
        }

        return player;
    }

    private static AudioAttributes getAudioAttributes() {
        if (sAudioAttributes == null) {
            sAudioAttributes = new AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MOVIE)
                    .build();
        }

        return sAudioAttributes;
    }

    private void initializePlayer() {
        if (mPlayer != null) return;

        // -------- Create Player --------
        // Use default or pass your bandwidthMeter here: bandwidthMeter = new DefaultBandwidthMeter.Builder(getContext()).build()
        DefaultTrackSelector trackSelector = new RestoreTrackSelector(new AdaptiveTrackSelection.Factory());
        mTrackSelectorManager.setTrackSelector(trackSelector);

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(getContext());
        mPlayer = createPlayer(renderersFactory, trackSelector);

        mExoPlayerController.setPlayer(mPlayer);

        // -------- Create Player Glue --------
        PlayerAdapter playerAdapter = new LeanbackPlayerAdapter(getContext(), mPlayer, UPDATE_DELAY_MS); // NOTE: possible context memory leak

        OnActionClickedListener playerActionListener = new PlayerActionListener();
        mPlayerGlue = new VideoPlayerGlue(getContext(), playerAdapter, playerActionListener); // NOTE: possible context memory leak
        mPlayerGlue.setHost(new SurfacePlaybackFragmentGlueHost(this));
        mPlayerGlue.setSeekEnabled(true);
        mPlayerGlue.setControlsOverlayAutoHideEnabled(false); // don't show controls on some player events like play/pause/end
        StoryboardSeekDataProvider.setSeekProvider(mPlayerGlue);
        hideControlsOverlay(true); // fix player ui not synced correctly

        mExoPlayerController.setPlayerView(mPlayerGlue);
    
        // -------- Create Subtitle Manager --------
        mSubtitleManager = new SubtitleManager(getActivity(), R.id.leanback_subtitles);

        if (mPlayer.getTextComponent() != null) 
            mPlayer.getTextComponent().addTextOutput(mSubtitleManager);

        // -------- Create Media Session --------
        createMediaSession();

        // -------- Initialize Player Rows --------

        mRowsSupportFragment = (RowsSupportFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_dock);

        ClassPresenterSelector presenterSelector = new ClassPresenterSelector();
        presenterSelector.addClassPresenter(
                mPlayerGlue.getControlsRow().getClass(), 
                mPlayerGlue.getPlaybackRowPresenter()
        );
        presenterSelector.addClassPresenter(ListRow.class, mRowPresenter);

        mRowsAdapter = new ArrayObjectAdapter(presenterSelector);

        mRowsAdapter.add(mPlayerGlue.getControlsRow());

        setAdapter(mRowsAdapter);

        mPlaybackPresenter.setView(this); // replaced by the embed player?
        mPlaybackPresenter.onEngineInitialized();
    }

    private void createMediaSession() {
        if (VERSION.SDK_INT <= 19 || getContext() == null) {
            // Fix Android 4.4 bug: java.lang.IllegalArgumentException: MediaButtonReceiver component may not be null
            return;
        }

        // NOTE: No way to disable only a notifications. We need to disable the media session instead.

        mMediaSession = new MediaSessionCompat(getContext(), getContext().getPackageName());

        mMediaSession.setActive(Helpers.isAndroidTVLauncher(getContext()));
        mMediaSessionConnector = new MediaSessionConnector(mMediaSession);

        try {
            mMediaSessionConnector.setPlayer(mPlayer);
        } catch (NoSuchMethodError e) {
            // Android 9, Sony
            // No virtual method setState(IJFJ)Landroid/media/session/PlaybackState$Builder;
            // in class Landroid/media/session/PlaybackState$Builder;
            return;
        }

        // NOTE: Don't set to null. This won't disable a notifications but makes them empty.
        mMediaSessionConnector.setMediaMetadataProvider(player -> {
            if (getVideo() == null) {
                return null;
            }

            MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, getVideo().getTitleFull());
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, getVideo().getTitleFull());
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, getVideo().getAuthor());
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, Helpers.toString(getVideo().getSecondTitleFull()));
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, getVideo().getCardImageUrl());
            metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getDurationMs());

            return metadataBuilder.build();
        });

        mMediaSessionConnector.setQueueNavigator(new BackboneQueueNavigator() {
            public long getSupportedQueueNavigatorActions(Player player) {
                return PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
            }

            public void onSkipToPrevious(Player player, ControlDispatcher controlDispatcher) {
                mPlaybackPresenter.onPreviousClicked();
            }

            public void onSkipToNext(Player player, ControlDispatcher controlDispatcher) {
                mPlaybackPresenter.onNextClicked();
            }
        });

        mMediaSessionConnector.setControlDispatcher(new DefaultControlDispatcher());

    }

    private void initPresenters() {
        mRowPresenter = new CustomListRowPresenter() {
            protected void onBindRowViewHolder(RowPresenter.ViewHolder holder, Object item) {
                super.onBindRowViewHolder(holder, item);

                focusPendingSuggestedItem(holder);
            }

            protected void onRowViewSelected(RowPresenter.ViewHolder holder, boolean selected) {
                super.onRowViewSelected(holder, selected);

                updatePlayerBackground();

            }
        };

        mCardPresenter = new VideoCardPresenter();
        mShortsPresenter = new ShortsCardPresenter();
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
        mCardPresenter.setOnItemViewLongPressedListener(new ItemViewLongPressedListener());
        mShortsPresenter.setOnItemViewLongPressedListener(new ItemViewLongPressedListener());
    }

    private final class ItemViewLongPressedListener implements OnItemLongPressedListener {
        public void onItemLongPressed(
                Presenter.ViewHolder itemViewHolder,
                Object item) {

            if (item instanceof Video) {
                mPlaybackPresenter.onSuggestionItemLongClicked((Video) item);
            }
        }
    }

    private final class ItemViewClickedListener implements minefarts.smarttube.leanback.widget.OnItemViewClickedListener {
        public void onItemClicked(
                Presenter.ViewHolder itemViewHolder,
                Object item,
                RowPresenter.ViewHolder rowViewHolder,
                Row row) {

            if (item instanceof Video) {
                mPlaybackPresenter.onSuggestionItemClicked((Video) item);
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Video) {
                mBackgroundManager.setBackgroundFrom((Video) item);

                checkScrollEnd((Video)item);
            }
        }

        private void checkScrollEnd(Video item) {
            for (VideoGroupObjectAdapter adapter : mVideoGroupAdapters.values()) {
                int index = adapter.indexOf(item);

                if (index != -1) {
                    int size = adapter.size();
                    if (index > (size - 4)) {
                        mPlaybackPresenter.onScrollEnd(item);
                    }
                    break;
                }
            }
        }

    }

    private class PlayerActionListener implements VideoPlayerGlue.OnActionClickedListener {
        public void onPrevious() {
            mPlaybackPresenter.onPreviousClicked();
        }

        public void onNext() {
            mPlaybackPresenter.onNextClicked();
        }

        public void onPlay() {
            mPlaybackPresenter.onPlayClicked();
        }

        public void onPause() {
            mPlaybackPresenter.onPauseClicked();
        }

        public void onAction(int actionId, int actionIndex) {
            mPlaybackPresenter.onButtonClicked(actionId, actionIndex);
        }

        public void onLongAction(int actionId, int actionIndex) {
            mPlaybackPresenter.onButtonLongClicked(actionId, actionIndex);
        }

        public void onTopEdgeFocused() {
            showOverlay(false);
        }

        public boolean onKeyDown(int keyCode) {
            return mPlaybackPresenter.onKeyDown(keyCode);
        }
    }

    // Begin Ui events

    public void setVideo(Video video) {
        mExoPlayerController.setVideo(video);

        if (mPlayerGlue != null && video != null) {
            // Preserve player formatting
            mPlayerGlue.setTitle(video.getTitleFull() != null ? video.getTitleFull() : "...");
            mPlayerGlue.setSubtitle(video.getSecondTitleFull() != null ? createSubtitle(video) : "...");
            mPlayerGlue.setVideo(video);
        }
    }

    public void showBackground(String url) {
        mBackgroundManager.showBackground(url);
    }

    public void showBackgroundColor(int colorResId) {
        mBackgroundManager.showBackgroundColor(colorResId);
    }

    private CharSequence createSubtitle(Video video) {

        if (getContext() == null) return "";

        List<String> parts = new ArrayList();

        //=================================================

        parts.add(
            video.getSecondTitleFull().replace("Published on ", "")
        );

        //=================================================

        if (video.isLive) {

            CharSequence color = Utils.color(
                getContext().getString(R.string.badge_live), 
                ContextCompat.getColor(
                    getContext(), 
                    R.color.red
                )
            );

            parts.add(color.toString());

        }

        //=================================================

        if (video.likeCount != null) {
            parts.add( 
                video.likeCount +
                Helpers.NON_BREAKING_SPACE +
                Helpers.THUMB_UP
            );
        }

        //=================================================

        if (video.dislikeCount != null) {
            parts.add(
                video.dislikeCount + 
                Helpers.NON_BREAKING_SPACE + 
                Helpers.THUMB_DOWN
            );
        }

        //=================================================

        if (video.subscriberCount != null) {
            parts.add(
                video.subscriberCount.replace(" ", Helpers.NON_BREAKING_SPACE)
            );
        }

        //=================================================

        String result = "";

        for (String part : parts) {
            result += " • ";
            result += part;
        }

        return result.substring(3);
    }

    private CharSequence createNextTitle(Video video) {
        CharSequence result = null;

        if (video != null) {
            result = YouTubeHelper.createInfo(video.getTitle(), video.getAuthor());
        }

        return result;
    }

    public void loadStoryboard() {
        if (mPlayerGlue.getSeekProvider() instanceof StoryboardSeekDataProvider) {
            ((StoryboardSeekDataProvider) mPlayerGlue.getSeekProvider()).init(getVideo(), mExoPlayerController.getDurationMs());
        }
    }

    public void setTitle(String title) {
        mPlayerGlue.setTitle(title);
    }

    public void showProgressBar(boolean show) {
        if (getProgressBarManager() == null) {
            return;
        }

        if (show) {
            getProgressBarManager().show();
            mProgressShowTimeMs = System.currentTimeMillis();
        } else {
            getProgressBarManager().hide();
        }
    }

    protected void onBufferingStateChanged(boolean start) {
        // Fix progress stop when playing videos non-stop (stop buffer event from previous video called)
        if (!start && System.currentTimeMillis() - mProgressShowTimeMs < 100) {
            return;
        }

        super.onBufferingStateChanged(start);
    }

    public void setSeekBarSegments(List<SeekBarSegment> segments) {
        if (mPlayerGlue != null) {
            mPlayerGlue.setSeekBarSegments(segments);
        }
    }

    public void setChatReceiver(ChatReceiver chatReceiver) {
        if (getActivity() != null) {
            LiveChatView liveChat = getActivity().findViewById(R.id.live_chat);
            liveChat.setChatReceiver(chatReceiver);
        }
    }

    // End Ui events

    // Begin Engine Events

    public void openSabr(MediaItemFormatInfo formatInfo) {
        mExoPlayerController.openSabr(formatInfo);
    }

    public void openDash(MediaItemFormatInfo formatInfo) {
        mExoPlayerController.openDash(formatInfo);
    }

    public void openDash(InputStream dashManifest) {
        mExoPlayerController.openDash(dashManifest);
    }

    public void openDashUrl(String dashManifestUrl) {
        mExoPlayerController.openDashUrl(dashManifestUrl);
    }

    public void openHlsUrl(String hlsPlaylistUrl) {
        mExoPlayerController.openHlsUrl(hlsPlaylistUrl);
    }

    public void openUrlList(List<String> urlList) {
        mExoPlayerController.openUrlList(urlList);
    }

    public void openMerged(MediaItemFormatInfo formatInfo, String hlsPlaylistUrl) {
        mExoPlayerController.openMerged(formatInfo, hlsPlaylistUrl);
    }

    public void openMerged(InputStream dashManifest, String hlsPlaylistUrl) {
        mExoPlayerController.openMerged(dashManifest, hlsPlaylistUrl);
    }

    public Long getPositionMs() {
        return mExoPlayerController.getPositionMs();
    }

    public void setPositionMs(long positionMs) {
        mExoPlayerController.setPositionMs(positionMs);
    }

    public Long getDurationMs() {
        long durationMs = mExoPlayerController.getDurationMs();

        long liveDurationMs = getVideo() != null ? getVideo().getLiveDurationMs() : 0;

        if (durationMs > Video.MAX_LIVE_DURATION_MS && liveDurationMs != 0) {
            durationMs = liveDurationMs;
        }

        return durationMs;
    }

    public void setPlayWhenReady(boolean play) {
        mExoPlayerController.setPlayWhenReady(play);
    }

    public Boolean getPlayWhenReady() {
        return mExoPlayerController.getPlayWhenReady();
    }

    public Boolean isPlaying() {
        return mExoPlayerController.isPlaying();
    }

    public Boolean isLoading() {
        return mExoPlayerController.isLoading();
    }

    public List<FormatItem> getVideoFormats() {
        return mExoPlayerController.getVideoFormats();
    }

    public List<FormatItem> getAudioFormats() {
        return mExoPlayerController.getAudioFormats();
    }

    public List<FormatItem> getSubtitleFormats() {
        return mExoPlayerController.getSubtitleFormats();
    }

    public FormatItem getVideoFormat() {
        return mExoPlayerController.getVideoFormat();
    }

    public FormatItem getAudioFormat() {
        return mExoPlayerController.getAudioFormat();
    }

    public FormatItem getSubtitleFormat() {
        return mExoPlayerController.getSubtitleFormat();
    }

    public Boolean isEngineInitialized() {
        return mPlayer != null;
    }

    public void blockEngine(boolean block) {
        mIsEngineBlocked = block;
    }

    public Boolean isEngineBlocked() {
        return mIsEngineBlocked;
    }

    public Boolean containsMedia() {
        return mExoPlayerController.containsMedia();
    }

    public Float getSpeed() {
        return mExoPlayerController.getSpeed();
    }

    public void setSpeed(float speed) {
        mExoPlayerController.setSpeed(speed);
        // NOTE: Real speed isn't changed immediately, so use supplied speed data
        setButtonState(R.id.action_video_speed, speed != 1.0f ? 1 : 0);
    }

    public Float getVolume() {
        return mExoPlayerController.getVolume();
    }

    public void setVolume(float volume) {
        mExoPlayerController.setVolume(volume);
    }

    public void setVideoGravity(int gravity) {
        setGravity(gravity);
    }

    // End Engine Events

    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "Destroying PlaybackFragment...");

        // Fix situations when engine didn't properly destroyed.
        // E.g. after closing dialogs.
        releasePlayer();

        if (mPlaybackPresenter.getView() == this) {
            mPlaybackPresenter.onViewDestroyed();
        }
    }

    public Video getVideo() {
        if (mExoPlayerController == null) {
            return null;
        }

        return mExoPlayerController.getVideo();
    }

    public void finish() {
        LeanbackActivity activity = getLeanbackActivity();

        if (activity != null) {
            activity.finish();
        }
    }

    /**
     * Force finish (PIP etc)
     */
    public void finishReally() {
        LeanbackActivity activity = getLeanbackActivity();

        if (activity != null) {
            activity.finishReally();
        }
    }

    public Boolean isOverlayShown() {
        return isControlsOverlayVisible();
    }

    public Boolean isSuggestionsShown() {
        return isControlsOverlayVisible() && getPlayerRowIndex() != 0;
    }

    public Boolean isControlsShown() {
        return isControlsOverlayVisible() && getPlayerRowIndex() == 0;
    }

    public void showControlsOverlay(boolean runAnimation) {
        
        super.showControlsOverlay(true);

        // Do throttle. Called so many times. Rely on boxing because initial state is unknown.
        if (mIsControlsShownPreviously != null && mIsControlsShownPreviously) {
            return;
        }

        updatePlayerBackground();

        if (mPlayerGlue != null) {
            mPlayerGlue.setControlsVisibility(true);
        }

        if (mPlaybackPresenter != null) {
            mPlaybackPresenter.onControlsShown(true);
        }

        mIsControlsShownPreviously = true;
    }

    public void hideControlsOverlay(boolean runAnimation) {
        
        super.hideControlsOverlay(true);

        // Do throttle. Called so many times. Rely on boxing because initial state is unknown.
        if (mIsControlsShownPreviously != null && !mIsControlsShownPreviously) {
            return;
        }

        if (mPlayerGlue != null) {
            mPlayerGlue.setControlsVisibility(false);
        }

        if (mPlaybackPresenter != null) {
            mPlaybackPresenter.onControlsShown(false);
        }

        mIsControlsShownPreviously = false;
    }

    /**
     * Show controls or suggestions: depending what has been shown last.
     */
    public void showOverlay(boolean show) {

        if (show) {
            showControlsOverlay(true);
        } else {
            hideControlsOverlay(true);
        }

    }

    public void showSuggestions(boolean show) {

        showOverlay(show);

        if (show && !isSuggestionsShown() && !isSuggestionsEmpty()) {
            setPlayerRowIndex(1);
        }

    }

    /**
     * The same as {@link #showOverlay(boolean)} but scrolls from suggestions to controls if needed.
     */
    public void showControls(boolean show) {
        showOverlay(show);
        setPlayerRowIndex(0);
    }

    public void setButtonState(int buttonId, int buttonState) {
        if (mPlayerGlue != null) {
            mPlayerGlue.setButtonState(buttonId, buttonState);
        }
    }

    public void setChannelIcon(String iconUrl) {
        if (mPlayerGlue != null) {
            mPlayerGlue.setChannelIcon(iconUrl);
        }
    }

    public void setSeekPreviewTitle(String title) {
        if (mPlayerGlue != null) {
            mPlayerGlue.setSeekPreviewTitle(title); // seeking ui
            // NOTE: setBody re-renders ui on change
            //mPlayerGlue.setBody(title); // full ui
        }
    }

    public void setNextTitle(Video nextVideo) {
        if (mPlayerGlue != null) {
            mPlayerGlue.setNextTitle(createNextTitle(nextVideo));
        }
    }

    public void showSubtitles(boolean show) {
        if (mSubtitleManager != null) {
            mSubtitleManager.show(show);
        }
    }

    public void updateSuggestions(VideoGroup group) {
        if (mRowsAdapter == null) {
            Log.e(TAG, "Related videos row not initialized yet.");
            return;
        }

        if (group == null || group.isEmpty()) {
            Log.e(TAG, "Suggestions row is empty!");
            return;
        }

        if (group.getAction() == VideoGroup.ACTION_SYNC) {
            VideoGroupObjectAdapter adapter = mVideoGroupAdapters.get(group.getId());
            if (adapter != null) {
                adapter.sync(group);
            }
            return;
        } else if (group.getAction() == VideoGroup.ACTION_REPLACE) {
            VideoGroupObjectAdapter adapter = mVideoGroupAdapters.get(group.getId());
            if (adapter != null) {
                adapter.clear();
                adapter.add(group);
                return;
            }
        }

        VideoGroupObjectAdapter existingAdapter = GridFragmentHelper.findRelatedAdapter(mVideoGroupAdapters, group, this::freeze);

        if (existingAdapter == null) {
            HeaderItem rowHeader = new HeaderItem(group.getTitle());
            int videoGroupId = group.getId(); // Create unique int from category.

            VideoGroupObjectAdapter videoGroupAdapter = new VideoGroupObjectAdapter(group, group.isShorts() ? mShortsPresenter : mCardPresenter);

            mVideoGroupAdapters.put(videoGroupId, videoGroupAdapter);

            ListRow row = new ListRow(rowHeader, videoGroupAdapter);

            int newPosition = group.getPosition() + SUGGESTIONS_START_INDEX;
            if (group.getPosition() == -1 || newPosition > mRowsAdapter.size()) {
                mRowsAdapter.add(row);
            } else {
                mRowsAdapter.add(newPosition, row);
            }
        } else {
            Log.d(TAG, "Continue row %s %s", group.getTitle(), System.currentTimeMillis());

            freeze(true);

            existingAdapter.add(group); // continue

            freeze(false);
        }
    }

    public void removeSuggestions(VideoGroup group) {
        if (group == null) {
            return;
        }

        VideoGroupObjectAdapter adapter = mVideoGroupAdapters.get(group.getId());

        if (adapter != null) {
            adapter.remove(group);

            if (adapter.isEmpty()) {
                int position = getSuggestionsIndex(group);
                if (position != -1) {
                    mVideoGroupAdapters.remove(group.getId());
                    mRowsAdapter.removeItems(position + SUGGESTIONS_START_INDEX, 1);
                }
            }
        }
    }

    public Integer getSuggestionsIndex(VideoGroup group) {
        if (mRowsAdapter == null) {
            Log.e(TAG, "Related videos row not initialized yet.");
            return -1;
        }

        VideoGroupObjectAdapter existingAdapter = mVideoGroupAdapters.get(group.getId());

        int index = getRowAdapterIndex(existingAdapter);

        return index != -1 ? index - SUGGESTIONS_START_INDEX : -1;
    }

    private int getRowAdapterIndex(VideoGroupObjectAdapter adapter) {
        int index = -1;

        for (int i = 0; i < mRowsAdapter.size(); i++) {
            Object row = mRowsAdapter.get(i);

            if (row instanceof ListRow) {
                ObjectAdapter current = ((ListRow) row).getAdapter();

                if (current == adapter) {
                    index = mRowsAdapter.indexOf(row);
                    break;
                }
            }
        }

        return index;
    }

    public VideoGroup getSuggestionsByIndex(int rowIndex) {
        if (getVideo() == null || !getVideo().hasVideo()) {
            return null;
        }

        // NOTE: skip first row. It's PlaybackControlsRow
        int realIndex = rowIndex + SUGGESTIONS_START_INDEX;
        Object row = mRowsAdapter != null && mRowsAdapter.size() > realIndex ? mRowsAdapter.get(realIndex) : null;

        VideoGroup result = null;

        if (row instanceof ListRow) {
            VideoGroupObjectAdapter adapter = (VideoGroupObjectAdapter) ((ListRow) row).getAdapter();
            result = VideoGroup.from(adapter.getAll());
        }

        return result;
    }

    public void focusSuggestedItem(int index) {
        if (mRowsSupportFragment != null) {
            ViewHolder vh = mRowsSupportFragment.getRowViewHolder(SUGGESTIONS_START_INDEX);
            // Skip PlaybackRowPresenter.ViewHolder
            if (vh instanceof ListRowPresenter.ViewHolder) {
                ((ListRowPresenter.ViewHolder) vh).getGridView().setSelectedPosition(index);
            }
        }
    }

    public void focusSuggestedItem(Video video) {
        if (mPendingFocus != null || video == null || video.getGroup() == null) {
            return;
        }

        mPendingFocus = video;

        focusPendingSuggestedItem(null);
    }

    private void focusPendingSuggestedItem(ViewHolder holder) {
        if (mPendingFocus == null || mPendingFocus.getGroup() == null || mRowsSupportFragment == null) {
            return;
        }

        VideoGroupObjectAdapter existingAdapter = mVideoGroupAdapters.get(mPendingFocus.getGroup().getId());

        if (existingAdapter == null) {
            mPendingFocus = null; // probably a leftover from the previous player instance
            return;
        }

        ViewHolder rowViewHolder;

        if (holder != null && holder.getRow() instanceof ListRow && ((ListRow) holder.getRow()).getAdapter() == existingAdapter) {
            rowViewHolder = holder;
        } else {
            int rowIndex = getRowAdapterIndex(existingAdapter);
            rowViewHolder = mRowsSupportFragment.getRowViewHolder(rowIndex);
        }

        // Skip PlaybackRowPresenter.ViewHolder
        if (rowViewHolder instanceof ListRowPresenter.ViewHolder) {
            int index = existingAdapter.indexOf(mPendingFocus);
            ((ListRowPresenter.ViewHolder) rowViewHolder).getGridView().setSelectedPosition(index);
            mPendingFocus = null;
        }
    }

    public void resetSuggestedPosition() {
        setPlayerRowIndex(0);
    }

    public void clearSuggestions() {
        if (mRowsAdapter != null && mRowsAdapter.size() > 1) {
            mRowsAdapter.removeItems(SUGGESTIONS_START_INDEX, mRowsAdapter.size() - 1);
        }

        mVideoGroupAdapters.clear();
        mPendingFocus = null;
    }

    public Boolean isSuggestionsEmpty() {
        // Ignore first row. It's player controls row.
        return mRowsAdapter == null || mRowsAdapter.size() <= SUGGESTIONS_START_INDEX;
    }

    /**
     * Disable scrolling on partially updated rows. This prevent controls from misbehaving.
     */
    private void freeze(boolean freeze) {
        // Disable scrolling on partially updated rows. This prevent controls from misbehaving.
        if (mRowPresenter != null && mRowsSupportFragment != null) {
            ViewHolder vh = mRowsSupportFragment.getRowViewHolder(mRowsSupportFragment.getSelectedPosition());
            // Skip PlaybackRowPresenter.ViewHolder
            if (vh instanceof ListRowPresenter.ViewHolder) {
                mRowPresenter.freeze(vh, freeze);
            }
        }
    }

    /* End PlayerController */

    private LeanbackActivity getLeanbackActivity() {
        return (LeanbackActivity) getActivity();
    }

    /**
     * Simply recreates exoplayer objects (silently) if prev track (current from this perspective) isn't empty<br/>
     * Fixes video artifacts when switching to the next video.<br/>
     * Also could help with memory leaks(??)<br/>
     * Without this also you'll have problems with track quality switching(??).
     */
    public void resetPlayerState() {
        mExoPlayerController.resetPlayerState();
        // Hide last frame of the previous video
        showBackgroundColor(R.color.player_background);
        setChatReceiver(null);
        setSeekBarSegments(null);
        setSeekPreviewTitle(null);
    }

    public Boolean isEmbed() {
        return false;
    }

    /**
     * PIP mode fix
     */
    private void showHideWidgets(boolean show) {
        Activity activity = getActivity();

        if (activity != null) {
            View overlay = activity.findViewById(R.id.player_overlay_wrapper);

            if (overlay != null) {
                overlay.setVisibility(show ? View.VISIBLE : View.GONE);
            }

            View liveChat = activity.findViewById(R.id.live_chat_wrapper);

            if (liveChat != null) {
                liveChat.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }
    }

    /**
     * Interface to be implemented by UI widget to support PlaybackSeekUi.
     */
    public void setPlaybackSeekUiClient(PlaybackSeekUi.Client client) {
        mSeekUiClient2 = client;
    }

    private final PlaybackSeekUi.Client mChainedClient2 = new PlaybackSeekUi.Client() {
        public boolean isSeekEnabled() {
            return mSeekUiClient2 == null ? false : mSeekUiClient2.isSeekEnabled();
        }

        public void onSeekStarted() {
            if (mSeekUiClient2 != null) {
                mSeekUiClient2.onSeekStarted();
            }
            setSeekMode(true);
        }

        public PlaybackSeekDataProvider getPlaybackSeekDataProvider() {
            return mSeekUiClient2 == null ? null : mSeekUiClient2.getPlaybackSeekDataProvider();
        }

        public void onSeekPositionChanged(long positionMs) {
            if (mSeekUiClient2 != null) {
                mSeekUiClient2.onSeekPositionChanged(positionMs);
            }
            PlaybackFragment.this.onSeekPositionChanged(positionMs);
        }

        public void onSeekFinished(boolean cancelled) {
            if (mSeekUiClient2 != null) {
                mSeekUiClient2.onSeekFinished(cancelled);
            }
            setSeekMode(false);
        }
    };

    /**
     * NOTE: MOD version. Removed part: hiding rows.<br/>
     * Show or hide other rows other than PlaybackRow.
     * @param inSeek True to make other rows visible, false to make other rows invisible.
     */
    private void setSeekMode(boolean inSeek) {
        if (mInSeek == inSeek) {
            return;
        }
        mInSeek = inSeek;
        if (mInSeek) {
            stopFadeTimer();
            // Show UI while seeking with FastForward/Rewind keys
            showControlsOverlay(false);
        }
    }

    private void stopFadeTimer() {
        Object handler = Helpers.getField(this, "mHandler");
        if (handler != null) {
            ((Handler)handler).removeMessages(START_FADE_OUT);
        }
    }

    boolean onInterceptInputEvent(InputEvent event) {
        final boolean controlsHidden = !isControlsOverlayVisible();
        //if (DEBUG) Log.v(TAG, "onInterceptInputEvent hidden " + controlsHidden + " " + event);
        boolean consumeEvent = false;
        int keyCode = KeyEvent.KEYCODE_UNKNOWN;
        int keyAction = 0;

        if (event instanceof KeyEvent) {
            keyCode = ((KeyEvent) event).getKeyCode();
            keyAction = ((KeyEvent) event).getAction();
            if (getInputEventHandler() != null) {
                // VideoPlayerGlue handler
                consumeEvent = getInputEventHandler().onKey(getView(), keyCode, (KeyEvent) event);
            }
        }

        if (consumeEvent) {
            return true;
        }

        switch (keyCode) {
            // Confirm key
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
            // Navigation key
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                // Event may be consumed; regardless, if controls are hidden then these keys will
                // bring up the controls.

                if (keyAction == KeyEvent.ACTION_DOWN) {
                    tickle();
                }
                break;
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
                if (isInSeek()) {
                    // when in seek, the SeekUi will handle the BACK.
                    return false;
                }
                // If controls are not hidden, back will be consumed to fade
                // them out (even if the key was consumed by the handler).
                if (!controlsHidden) {
                    consumeEvent = true;

                    if (((KeyEvent) event).getAction() == KeyEvent.ACTION_UP) {
                        hideControlsOverlay(true);
                    }
                }
                break;
            default:
                if (consumeEvent) {
                    if (keyAction == KeyEvent.ACTION_DOWN) {
                        tickle();
                    }
                }
        }
        return consumeEvent;
    }

    private View.OnKeyListener getInputEventHandler() {
        return (View.OnKeyListener) Helpers.getField(this, "mInputEventHandler");
    }

    private boolean isInSeek() {
        Object mInSeek = Helpers.getField(this, "mInSeek");
        return mInSeek != null && (boolean) mInSeek;
    }

    public void setPlaybackMode(int mode) {
        mPlaybackMode = mode;
        persistState();
    }

    public int getPlaybackMode() {
        return mPlaybackMode;
    }

    public boolean isAfrEnabled() {
        return mIsAfrEnabled;
    }

    public void setAfrEnabled(boolean enabled) {
        mIsAfrEnabled = enabled;
        persistState();
    }

    public boolean isAfrFpsCorrectionEnabled() {
        return mIsAfrFpsCorrectionEnabled;
    }

    public void setAfrFpsCorrectionEnabled(boolean enabled) {
        mIsAfrFpsCorrectionEnabled = enabled;
        persistState();
    }

    public boolean isAfrResSwitchEnabled() {
        return mIsAfrResSwitchEnabled;
    }

    public void setAfrResSwitchEnabled(boolean enabled) {
        mIsAfrResSwitchEnabled = enabled;
        persistState();
    }

    public boolean isDoubleRefreshRateEnabled() {
        return mIsDoubleRefreshRateEnabled;
    }

    public void setDoubleRefreshRateEnabled(boolean enabled) {
        mIsDoubleRefreshRateEnabled = enabled;
        persistState();
    }

    public FormatItem getFormat(int type) {
        FormatItem format = null;

        switch (type) {
            case FormatItem.TYPE_VIDEO:
                format = mVideoFormat;
                break;
            case FormatItem.TYPE_AUDIO:
                format = mAudioFormat;
                break;
            case FormatItem.TYPE_SUBTITLE:
                format = mSubtitleFormat;
                break;
        }

        MediaTrack track = FormatItem.toMediaTrack(format);
        if (track != null) {
            track.isSaved = true;
        }

        return FormatItem.checkFormat(format, type);
    }

    public void setFormat(FormatItem format) {

        if (format == null) return;

        // Android 4.4 fix for format selection dialog (player destroyed when dialog is focused)
        mExoPlayerController.selectFormat(format);

        switch (format.getType()) {
            
            case FormatItem.TYPE_VIDEO:
                mVideoFormat = format;
                break;
            
            case FormatItem.TYPE_AUDIO:
                mAudioFormat = format;
                break;

            case FormatItem.TYPE_SUBTITLE:
                setLastSubtitleFormat(format);
                mSubtitleFormat = format;
                break;
            
        }
        
        persistState();
    }

    public FormatItem getLastSubtitleFormat() {
        return !mLastSubtitleFormats.isEmpty() ? mLastSubtitleFormats.get(0) : FormatItem.SUBTITLE_NONE;
    }

    public List<FormatItem> getLastSubtitleFormats() {
        return mLastSubtitleFormats;
    }

    private void setLastSubtitleFormat(FormatItem format) {
        if (format != null && !format.isDefault()) {
            mLastSubtitleFormats.remove(format);
            mLastSubtitleFormats.add(0, format);
        } else if (mSubtitleFormat != null && !mSubtitleFormat.isDefault()) {
            mLastSubtitleFormats.remove(mSubtitleFormat);
            mLastSubtitleFormats.add(0, mSubtitleFormat);
        }
    }

    public float getSubtitleScale() {
        return mSubtitleScale;
    }

    public void setSubtitleScale(float scale) {
        mSubtitleScale = scale;
        persistState();
    }

    public float getPlayerVolume() {
        return mPlayerVolume;
    }

    public void setPlayerVolume(float scale) {
        mPlayerVolume = scale;
        persistState();
    }

    public int getAudioDelayMs() {
        return mAudioDelayMs;
    }

    public void setAudioDelayMs(int delayMs) {
        mAudioDelayMs = delayMs;
        persistState();
    }

    public String getAudioLanguage() {
        return mAudioLanguage;
    }

    public void setAudioLanguage(String language) {
        mAudioLanguage = language;
        setLastAudioLanguage(language);
        persistState();
    }

    public List<String> getLastAudioLanguages() {
        return mLastAudioLanguages;
    }

    private void setLastAudioLanguage(String language) {
        mLastAudioLanguages.remove(language);
        mLastAudioLanguages.add(0, language);
    }

    public String getSubtitleLanguage() {
        return mSubtitleLanguage;
    }

    public void setSubtitleLanguage(String language) {
        mSubtitleLanguage = language;
        persistState();
    }

    public boolean isTimeCorrectionEnabled() {
        return mIsTimeCorrectionEnabled;
    }

    public void setTimeCorrectionEnabled(boolean enable) {
        mIsTimeCorrectionEnabled = enable;
        persistState();
    }

    public boolean isSkip24RateEnabled() {
        return mIsSkip24RateEnabled;
    }

    public void setSkip24RateEnabled(boolean enable) {
        mIsSkip24RateEnabled = enable;
        persistState();
    }

    public boolean isSkipShortsEnabled() {
        return mIsSkipShortsEnabled;
    }

    public void setSkipShortsEnabled(boolean enable) {
        mIsSkipShortsEnabled = enable;
        persistState();
    }

    public boolean isLiveChatEnabled() {
        return mIsLiveChatEnabled;
    }

    public void setLiveChatEnabled(boolean enable) {
        mIsLiveChatEnabled = enable;
        persistState();
    }

    public FormatItem getDefaultAudioFormat() {
        // Android 4 (probably some others) doesn't support opus (ac3 will be reverted to opus)
        // Note, 5.1 mp4a doesn't work in 5.1 mode
        // Use opus (ac3 fallback) on modern devices. vp9 and opus should be supported at the same time?
        return DeviceHelpers.isVP9ResolutionSupported(2160) ? FormatItem.AUDIO_51_AC3 : FormatItem.AUDIO_HQ_MP4A;
    }

    public FormatItem getDefaultVideoFormat() {

        if (VERSION.SDK_INT <= 19) { // Android 4 playback crash fix (memory leak?)
            return FormatItem.VIDEO_SD_AVC_30;
        } else if (VERSION.SDK_INT <= 23 && DeviceHelpers.isVP9ResolutionSupported(1080)) {
            return FormatItem.VIDEO_FHD_VP9_60;
        } else if (DeviceHelpers.isVP9ResolutionSupported(2160)) {
            return FormatItem.VIDEO_4K_VP9_60;
        } else if (DeviceHelpers.isVP9ResolutionSupported(1080)) {
            return FormatItem.VIDEO_FHD_VP9_60;
        } else {
            return FormatItem.VIDEO_HD_AVC_30;
        }

    }

    public FormatItem getDefaultSubtitleFormat() {
        return FormatItem.SUBTITLE_NONE;
    }

    private void restoreState() {
        if (mPrefs == null) return;

        String data = mPrefs.getProfileData(VIDEO_PLAYER_DATA);
        String[] split = Helpers.splitData(data);

        /* 01 */ mVideoFormat = Helpers.firstNonNull(ExoFormatItem.from(Helpers.parseStr(split, 1)), getDefaultVideoFormat());
        /* 02 */ mAudioFormat = Helpers.firstNonNull(ExoFormatItem.from(Helpers.parseStr(split, 2)), getDefaultAudioFormat());
        /* 03 */ mSubtitleFormat = Helpers.firstNonNull(ExoFormatItem.from(Helpers.parseStr(split, 3)), getDefaultSubtitleFormat());

        /* 07 */ mIsAfrEnabled = Helpers.parseBoolean(split, 7, false);
        /* 08 */ mIsAfrFpsCorrectionEnabled = Helpers.parseBoolean(split, 8, true);
        /* 09 */ mIsAfrResSwitchEnabled = Helpers.parseBoolean(split, 9, false);
        /* 10 */ mAudioDelayMs = Helpers.parseInt(split, 10, 0);

        /* 13 */ mIsTimeCorrectionEnabled = Helpers.parseBoolean(split, 13, true);
        /* 14 */ mIsDoubleRefreshRateEnabled = Helpers.parseBoolean(split, 14, true);
        /* 15 */ mSubtitleScale = Helpers.parseFloat(split, 15, .7f);
        /* 16 */ mPlayerVolume = Helpers.parseFloat(split, 16, 1.0f);

        /* 18 */ mSubtitlePosition = Helpers.parseFloat(split, 18, 0.1f);
        /* 19 */ mIsSkip24RateEnabled = Helpers.parseBoolean(split, 19, false);
        /* 20 */ mIsLiveChatEnabled = Helpers.parseBoolean(split, 20, false);
        /* 21 */ mLastSubtitleFormats = Helpers.parseList(split, 21, ExoFormatItem::from);

        /* 25 */ mPlaybackMode = Helpers.parseInt(split, 25, PlaybackFragment.PLAYBACK_MODE_ALL);
        /* 26 */ mAudioLanguage = Helpers.parseStr(split, 26, LocaleUtility.getCurrentLanguage(mPrefs.getContext()));
        /* 27 */ mSubtitleLanguage = Helpers.parseStr(split, 27, LocaleUtility.getCurrentLanguage(mPrefs.getContext()));

        /* 30 */ mPitch = Helpers.parseFloat(split, 30, 1.0f);
        /* 31 */ mIsSkipShortsEnabled = Helpers.parseBoolean(split, 31, false);
        /* 32 */ mLastAudioLanguages = Helpers.parseStrList(split, 32);
        
    }

    public void persistState() {
        if (mPrefs == null) return;

        mPrefs.setProfileData(
            VIDEO_PLAYER_DATA, 
            Helpers.mergeData(
            /* 00 */ null,
            /* 01 */ mVideoFormat, 
            /* 02 */ mAudioFormat, 
            /* 03 */ mSubtitleFormat,
            /* 04 */ null, 
            /* 05 */ null, 
            /* 06 */ null,
            /* 07 */ mIsAfrEnabled, 
            /* 08 */ mIsAfrFpsCorrectionEnabled, 
            /* 09 */ mIsAfrResSwitchEnabled, 
            /* 10 */ mAudioDelayMs, 
            /* 11 */ null, 
            /* 12 */ null,
            /* 13 */ mIsTimeCorrectionEnabled,
            /* 14 */ mIsDoubleRefreshRateEnabled, 
            /* 15 */ mSubtitleScale, 
            /* 16 */ mPlayerVolume, 
            /* 17 */ null,
            /* 18 */ mSubtitlePosition, 
            /* 19 */ mIsSkip24RateEnabled, 
            /* 20 */ mIsLiveChatEnabled, 
            /* 21 */ mLastSubtitleFormats, 
            /* 22 */ null, 
            /* 23 */ null, 
            /* 24 */ null, 
            /* 25 */ mPlaybackMode, 
            /* 26 */ mAudioLanguage, 
            /* 27 */ mSubtitleLanguage,
            /* 28 */ null,
            /* 29 */ null, 
            /* 30 */ mPitch, 
            /* 31 */ mIsSkipShortsEnabled, 
            /* 32 */ mLastAudioLanguages
        ));
    }

    @Override
    public void onProfileChanged() {
        persistState();
        restoreState();
    }

}
