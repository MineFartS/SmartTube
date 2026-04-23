package SmartTubeApp.app.presenters.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import SmartTubeApp.AppUpdateChecker;
import SmartTubeApp.AppUpdateCheckerListener;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import SmartTubeApp.R;
import SmartTubeApp.app.models.errors.ErrorFragmentData;
import SmartTubeApp.app.models.playback.ui.OptionItem;
import SmartTubeApp.app.models.playback.ui.UiOptionItem;
import SmartTubeApp.app.presenters.AppDialogPresenter;
import SmartTubeApp.app.presenters.BrowsePresenter;
import SmartTubeApp.app.presenters.base.BasePresenter;
import SmartTubeApp.prefs.GeneralData;
import SmartTubeApp.utils.LoadingManager;
import SmartTubeApp.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class AppUpdatePresenter extends BasePresenter<Void> implements AppUpdateCheckerListener {
    @SuppressLint("StaticFieldLeak")
    private static AppUpdatePresenter sInstance;
    private final AppUpdateChecker mUpdateChecker;
    private final AppDialogPresenter mSettingsPresenter;
    private final String[] mUpdateManifestUrls;
    private boolean mIsForceCheck;

    public AppUpdatePresenter(Context context) {
        super(context);
        mUpdateChecker = new AppUpdateChecker(context, this);
        mSettingsPresenter = AppDialogPresenter.instance(context);
        mUpdateManifestUrls = context.getResources().getStringArray(R.array.update_urls);
    }

    public static AppUpdatePresenter instance(Context context) {
        if (sInstance == null) {
            sInstance = new AppUpdatePresenter(context);
        }

        sInstance.setContext(context);

        return sInstance;
    }

    public static void unhold() {
        sInstance = null;
    }

    public void start(boolean forceCheck) {
        mUpdateChecker.checkForUpdates(mUpdateManifestUrls);
    }

    public void onUpdateFound(
        String versionName, 
        ArrayList<String> changelog, 
        String apkPath
    ) {
        if (mIsForceCheck) {
            LoadingManager.showLoading(getContext(), false);
            showUpdateDialog(versionName, changelog, apkPath);
        } else if (GeneralData.instance(getContext()).isOldUpdateNotificationsEnabled()) {
            showUpdateDialog(versionName, changelog, apkPath);
        } else {
            pinUpdateSection(versionName, changelog, apkPath);
        }
    }

    @Override
    public void onUpdateError(Exception error) {
        if (mIsForceCheck) {
            LoadingManager.showLoading(getContext(), false);

            if (AppUpdateCheckerListener.LATEST_VERSION.equals(error.getMessage())) {
                MessageHelpers.showMessage(getContext(), R.string.update_not_found);
            } else {
                MessageHelpers.showMessage(getContext(), String.format("%s: %s", getContext().getString(R.string.update_error), error.getMessage()));
            }
        }

        onFinish();
    }

    private void showUpdateDialog(
        String versionName, 
        ArrayList<String> changelog, 
        String apkPath
    ) {
        
        // Don't show update dialog if the player opened or the app is collapsed
        if (getContext() == null || getViewManager().isPlayerInForeground() || !Utils.isAppInForegroundFixed()) {
            return;
        }

        mSettingsPresenter.appendSingleButton(
            UiOptionItem.from(
                getContext().getString(R.string.install_update), 
                optionItem -> {
                    GeneralData.instance(getContext()).setChangelog(changelog);
                    mUpdateChecker.installUpdate();
                }, 
                false
            )
        );

        mSettingsPresenter.appendStringsCategory(
            getContext().getString(R.string.update_changelog), 
            createChangelogOptions(changelog)
        );

        mSettingsPresenter.showDialog(
            String.format(
                "%s %s", 
                getContext().getString(R.string.app_name), 
                versionName
            ), 
            AppUpdatePresenter::unhold
        );
        
    }

    private void pinUpdateSection(
        String versionName, 
        ArrayList<String> changelog, 
        String apkPath
    ) {
        // Don't show update dialog if the player opened or the app is collapsed
        if (getContext() == null) {
            return;
        }

        BrowsePresenter.instance(getContext()).pinItem(getContext().getString(R.string.update_found), R.drawable.action_info, new ErrorFragmentData() {
            @Override
            public void onAction() {
                GeneralData.instance(getContext()).setChangelog(changelog);
                mUpdateChecker.installUpdate();
            }

            @Override
            public String getMessage() {
                return String.format("%s %s", getContext().getString(R.string.app_name), versionName) + " " +
                        getContext().getString(R.string.update_changelog) + ":\n" +
                        createChangelog(changelog);
            }

            @Override
            public String getActionText() {
                return getContext().getString(R.string.install_update);
            }
        });
    }

    private List<OptionItem> createChangelogOptions(ArrayList<String> changelog) {
        List<OptionItem> options = new ArrayList<>();

        for (String change : changelog) {
            options.add(UiOptionItem.from(change));
        }

        return options;
    }

    private String createChangelog(ArrayList<String> changelog) {
        StringBuilder builder = new StringBuilder();

        int maxLines = 30;
        int lineNum = 0;

        for (String change : changelog) {
            if (lineNum > maxLines) {
                break;
            }

            builder.append("- ");
            builder.append(change);
            builder.append("\n");

            lineNum++;
        }

        return builder.toString();
    }
}
