package minefarts.smarttube.app.presenters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import minefarts.sharedutils.oauth.Account;
import minefarts.sharedutils.service.data.MediaGroup;
import minefarts.sharedutils.helpers.Helpers;
import minefarts.sharedutils.helpers.MessageHelpers;
import minefarts.sharedutils.mylogger.Log;
import minefarts.sharedutils.prefs.GlobalPreferences;
import minefarts.sharedutils.rx.RxHelper;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.playback.service.VideoStateService;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.app.presenters.dialogs.AccountSelectionPresenter;
import minefarts.smarttube.app.presenters.dialogs.BootDialogPresenter;
import minefarts.smarttube.app.views.SplashView;
import minefarts.smarttube.app.views.ViewManager;
import minefarts.smarttube.misc.ServiceManager;
import minefarts.smarttube.prefs.AccountsData;
import minefarts.smarttube.prefs.GeneralData;
import minefarts.smarttube.utils.IntentExtractor;
import minefarts.smarttube.utils.SimpleEditDialog;
import minefarts.smarttube.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class SplashPresenter extends BasePresenter<SplashView> {
    
    private static final String TAG = SplashPresenter.class.getSimpleName();
    
    private static final long APP_INIT_DELAY_MS = 10_000;
    
    @SuppressLint("StaticFieldLeak")
    private static SplashPresenter sInstance;
    
    private static boolean sRunOnce;
    private boolean mRunPerInstance;
    private final List<IntentProcessor> mIntentChain = new ArrayList<>();
    private String mBridgePackageName;

    private interface IntentProcessor {
        boolean process(Intent intent);
    }

    private SplashPresenter(Context context) {
        super(context);
    }

    public static SplashPresenter instance(Context context) {
        if (sInstance == null) {
            sInstance = new SplashPresenter(context);
        }

        sInstance.setContext(context);

        return sInstance;
    }

    public static void unhold() {
        if (sInstance != null) {
            Utils.removeCallbacks(sInstance::runBackgroundTasks);
        }
        sInstance = null;
    }

    @Override
    public void onViewInitialized() {
        if (getView() == null) return;

        Utils.cancelFinishTheApp(getContext());

        if (!sRunOnce) {
            sRunOnce = true;
            RxHelper.setupGlobalErrorHandler();
            initGlobalPrefs();
            initVideoStateService();
        }

        if (!mRunPerInstance) {
            mRunPerInstance = true;
            Utils.postDelayed(this::runBackgroundTasks, APP_INIT_DELAY_MS);
            initIntentChain();
        }

        Utils.postDelayed(this::checkForUpdates, APP_INIT_DELAY_MS);
        Utils.updateRemoteControlService(getContext());

        applyNewIntent(getView().getNewIntent());

        showAccountSelectionIfNeeded(); // should be placed after Intent chain
        checkAccountPassword();

    }

    private void runBackgroundTasks() {
        ServiceManager.refreshCacheIfNeeded(); // warm up player engine
        Utils.updateChannels(getContext());
    }

    private void showAccountSelectionIfNeeded() {
        AccountSelectionPresenter.instance(getContext()).show();
    }

    private void checkAccountPassword() {
        AccountsData data = AccountsData.instance(getContext());
        // Block even if the password was accepted before
        if (data.getAccountPassword() != null) {
            data.setPasswordAccepted(false);
            PlaybackPresenter.instance(getContext()).forceFinish();
            BrowsePresenter.instance(getContext()).updateSections();
        }
    }

    private void checkForUpdates() {
        BootDialogPresenter updatePresenter = BootDialogPresenter.instance(getContext());
        updatePresenter.start();
    }

    private void initVideoStateService() {
        if (getContext() != null) {
            VideoStateService.instance(getContext());
        }
    }

    /**
     * Need to be the first line and executed on earliest stage once.<br/>
     * Do init media service language and context.<br/>
     * NOTE: this command should run before using any of the media service api.
     */
    private void initGlobalPrefs() {
        Log.d(TAG, "initGlobalData called...");

        if (getContext() != null) {
            // 1) Auth token storage init
            // 2) Media service language setup (I assume that context has proper language)
            GlobalPreferences.instance(getContext());
        }
    }

    public String getBridgePackageName() {
        return mBridgePackageName;
    }

    private void initIntentChain() {
        mIntentChain.add(intent -> {
            String accountName = IntentExtractor.extractAccountName(intent);

            if (accountName != null) {
                List<Account> accounts = getSignInService().getAccounts();
                for (Account account : accounts) {
                    if (Helpers.equals(account.getName(), accountName)) {
                        AccountSelectionPresenter.instance(getContext()).selectAccount(account);
                        break;
                    }
                }
            }

            return false;
        });

        mIntentChain.add(intent -> {
            String searchText = IntentExtractor.extractSearchText(intent);

            if (searchText != null || IntentExtractor.isStartVoiceCommand(intent)) {
                SearchPresenter searchPresenter = SearchPresenter.instance(getContext());
                if (IntentExtractor.isInstantPlayCommand(intent)) {
                    searchPresenter.startPlay(searchText);
                } else {
                    searchPresenter.startSearch(searchText);
                }
                return true;
            }

            return false;
        });

        mIntentChain.add(intent -> {
            String channelId = null;

            try {
                channelId = IntentExtractor.extractChannelId(intent);
            } catch (IllegalArgumentException e) {
                MessageHelpers.showLongMessage(getContext(), e.getMessage());
            }

            if (channelId != null) {
                ChannelPresenter channelPresenter = ChannelPresenter.instance(getContext());
                channelPresenter.openChannel(channelId);
                return true;
            }

            return false;
        });

        mIntentChain.add(intent -> {
            String playlistId = IntentExtractor.extractPlaylistId(intent);

            if (playlistId != null) {
                Video video = new Video();
                video.playlistId = playlistId;
                ChannelUploadsPresenter.instance(getContext()).openChannel(video);
                return true;
            }

            return false;
        });

        // Should come after playlist
        mIntentChain.add(intent -> {
            String videoId = IntentExtractor.extractVideoId(intent);

            if (videoId != null) {
                long timeMs = IntentExtractor.extractVideoTimeMs(intent);
                PlaybackPresenter playbackPresenter = PlaybackPresenter.instance(getContext());
                boolean finishOnEnded = IntentExtractor.hasFinishOnEndedFlag(intent);

                playbackPresenter.openVideo(videoId, finishOnEnded, timeMs);

                enablePlayerOnlyModeIfNeeded(intent);

                return true;
            }

            return false;
        });

        // NOTE: doesn't work very well. E.g. there's problems with focus or conflicts with 'boot to' section option.
        mIntentChain.add(intent -> {

            int sectionId = -1;

            // ATV channel icon clicked
            if (IntentExtractor.isSubscriptionsUrl(intent)) {
                sectionId = MediaGroup.TYPE_SUBSCRIPTIONS;
            } else if (IntentExtractor.isHistoryUrl(intent)) {
                sectionId = MediaGroup.TYPE_HISTORY;
            } else if (IntentExtractor.isRecommendedUrl(intent)) {
                sectionId = MediaGroup.TYPE_HOME;
            }

            if (sectionId != -1) {

                BrowsePresenter.instance(getContext()).selectSection(sectionId);

                return true;
            }

            return false;
        });

        // Should come last
        mIntentChain.add(intent -> {
            ViewManager viewManager = getViewManager();
            viewManager.startDefaultView();

            // For debug purpose when using ATV bridge.
            if (IntentExtractor.hasData(intent) && !IntentExtractor.isATVChannelUrl(intent) && !IntentExtractor.isRootUrl(intent)) {
                MessageHelpers.showLongMessage(getContext(), String.format("Can't process intent: %s", Helpers.toString(intent)));
            }

            return true;
        });
    }

    public void applyNewIntent(Intent intent) {
        if (intent != null) {
            mBridgePackageName = intent.getStringExtra("bridge_package_name");
        }

        for (IntentProcessor processor : mIntentChain) {
            if (processor.process(intent)) {
                break;
            }
        }
    }

    private void enablePlayerOnlyModeIfNeeded(Intent intent) {
        ViewManager viewManager = getViewManager();

        boolean isRestartIntent = IntentExtractor.isRestartIntent(intent);
        boolean isATVIntent = IntentExtractor.isATVIntent(intent);
        boolean isExternalIntent = !isRestartIntent && !isATVIntent && !viewManager.isTopViewVisible();

        viewManager.enablePlayerOnlyMode(isExternalIntent);
    }
}
