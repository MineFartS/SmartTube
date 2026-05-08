package SmartTubeApp.app.presenters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import com.liskovsoft.sharedutils.oauth.Account;
import com.liskovsoft.sharedutils.data.MediaGroup;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.prefs.GlobalPreferences;
import com.liskovsoft.sharedutils.rx.RxHelper;
import SmartTubeApp.R;
import SmartTubeApp.app.models.data.Video;
import SmartTubeApp.app.models.playback.service.VideoStateService;
import SmartTubeApp.app.presenters.base.BasePresenter;
import SmartTubeApp.app.presenters.dialogs.AccountSelectionPresenter;
import SmartTubeApp.app.presenters.dialogs.BootDialogPresenter;
import SmartTubeApp.app.views.SplashView;
import SmartTubeApp.app.views.ViewManager;
import SmartTubeApp.misc.ServiceManager;
import SmartTubeApp.misc.StreamReminderService;
import SmartTubeApp.prefs.AccountsData;
import SmartTubeApp.prefs.GeneralData;
import SmartTubeApp.utils.IntentExtractor;
import SmartTubeApp.utils.SimpleEditDialog;
import SmartTubeApp.utils.Utils;

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
    private final Runnable mRunBackgroundTasks = this::runBackgroundTasks;
    private final Runnable mCheckForUpdates = this::checkForUpdates;

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
            Utils.removeCallbacks(sInstance.mRunBackgroundTasks);
        }
        sInstance = null;
    }

    @Override
    public void onViewInitialized() {
        if (getView() == null) {
            return;
        }

        Utils.cancelFinishTheApp(getContext());

        runOneTimeTasks();
        runPerInstanceTasks();
        runPerViewTasks();
    }

    private void runOneTimeTasks() {
        if (!sRunOnce) {
            sRunOnce = true;
            RxHelper.setupGlobalErrorHandler();
            initGlobalPrefs();
            initVideoStateService();
            initStreamReminderService();
        }
    }

    private void runPerInstanceTasks() {
        if (!mRunPerInstance) {
            mRunPerInstance = true;
            Utils.postDelayed(mRunBackgroundTasks, APP_INIT_DELAY_MS);
            initIntentChain();
        }
    }

    private void runPerViewTasks() {
        Utils.postDelayed(mCheckForUpdates, APP_INIT_DELAY_MS);
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

    private void initStreamReminderService() {
        if (getContext() != null) {
            StreamReminderService.instance(getContext()).start();
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
