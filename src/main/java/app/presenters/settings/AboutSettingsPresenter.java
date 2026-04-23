package SmartTubeApp.app.presenters.settings;

import android.content.Context;
import SmartTubeApp.AppUpdateChecker;
import com.liskovsoft.sharedutils.helpers.AppInfoHelpers;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.locale.LocaleUtility;
import SmartTubeApp.R;
import SmartTubeApp.app.models.playback.ui.OptionItem;
import SmartTubeApp.app.models.playback.ui.UiOptionItem;
import SmartTubeApp.app.presenters.AppDialogPresenter;
import SmartTubeApp.app.presenters.dialogs.AppUpdatePresenter;
import SmartTubeApp.app.presenters.base.BasePresenter;
import SmartTubeApp.prefs.ContentBlockData;
import SmartTubeApp.prefs.GeneralData;
import SmartTubeApp.utils.Utils;
import com.liskovsoft.sharedutils.data.SponsorSegment;
import SmartTubeApp.prefs.GeneralData;
import com.liskovsoft.sharedutils.service.internal.MediaServiceData;
import SmartTubeApp.app.presenters.BrowsePresenter;
import com.liskovsoft.sharedutils.data.MediaGroup;
import SmartTubeApp.prefs.PlayerData;
import SmartTubeApp.prefs.MainUIData;
import SmartTubeApp.prefs.PlayerTweaksData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AboutSettingsPresenter extends BasePresenter<Void> {

    private final AppUpdateChecker mUpdateChecker;

    public AboutSettingsPresenter(Context context) {
        super(context);

        mUpdateChecker = new AppUpdateChecker(getContext(), null);
    }

    public static AboutSettingsPresenter instance(Context context) {
        return new AboutSettingsPresenter(context);
    }

    public void show() {

        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());
        
        GeneralData generalData = GeneralData.instance(getContext());

        String mainTitle = String.format(
            "%s %s",
            getContext().getString(R.string.app_name),
            AppInfoHelpers.getAppVersionName(getContext())
        );

        appendUpdateCheckButton(settingsPresenter);

        appendUpdateChangelogButton(settingsPresenter);

        settingsPresenter.appendSingleSwitch(
            UiOptionItem.from(
                getContext().getString(R.string.check_updates_auto), 
                optionItem -> {
                    mUpdateChecker.enableUpdateCheck(optionItem.isSelected());
                }, 
                mUpdateChecker.isUpdateCheckEnabled()
            )
        );

        settingsPresenter.appendSingleSwitch(
            UiOptionItem.from(
                getContext().getString(R.string.dialog_notification),
                optionItem -> generalData.setOldUpdateNotificationsEnabled(optionItem.isSelected()), 
                generalData.isOldUpdateNotificationsEnabled()
            )
        );

        settingsPresenter.showDialog(mainTitle);

    }

    private void appendUpdateCheckButton(AppDialogPresenter settingsPresenter) {
        OptionItem updateCheckOption = UiOptionItem.from(
            getContext().getString(R.string.check_for_updates),
            option -> AppUpdatePresenter.instance(getContext()).start(true)
        );

        settingsPresenter.appendSingleButton(updateCheckOption);
    }

    private void appendUpdateChangelogButton(AppDialogPresenter settingsPresenter) {
        
        List<String> changes = GeneralData.instance(getContext()).getChangelog();

        if (changes == null || changes.isEmpty()) {
            return;
        }

        List<OptionItem> changelog = new ArrayList<>();

        for (String change : changes) {
            changelog.add(UiOptionItem.from(change));
        }

        String title = String.format(
            "%s %s",
            getContext().getString(R.string.update_changelog),
            AppInfoHelpers.getAppVersionName(getContext())
        );

        settingsPresenter.appendStringsCategory(title, changelog);

    }

}
