package minefarts.smarttube.app.presenters.settings;

import android.content.Context;

import minefarts.smarttube.update.AppUpdateChecker;
import minefarts.smarttube.utils.helpers.AppInfoHelpers;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.helpers.MessageHelpers;
import minefarts.smarttube.utils.locale.LocaleUtility;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.app.presenters.dialogs.AppUpdatePresenter;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.prefs.ContentBlockData;
import minefarts.smarttube.prefs.GeneralData;
import minefarts.smarttube.utils.Utils;
import minefarts.smarttube.prefs.GeneralData;
import com.liskovsoft.youtubeapi.service.internal.MediaServiceData;
import minefarts.smarttube.app.presenters.BrowsePresenter;
import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import minefarts.smarttube.prefs.MainUIData;
import minefarts.smarttube.prefs.PlayerTweaksData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AboutSettingsPresenter extends BasePresenter<Void> {

    AppUpdateChecker mUpdateChecker;

    public static AboutSettingsPresenter instance(Context context) {
        AboutSettingsPresenter pres = new AboutSettingsPresenter();

        pres.setContext(context);
        pres.mUpdateChecker = new AppUpdateChecker(context, null);

        return pres;
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
        UiOptionItem updateCheckOption = UiOptionItem.from(
            getContext().getString(R.string.check_for_updates),
            option -> AppUpdatePresenter.instance(getContext()).start(true)
        );

        settingsPresenter.appendSingleButton(updateCheckOption);
    }

    private void appendUpdateChangelogButton(AppDialogPresenter settingsPresenter) {
        
        List<String> changes = GeneralData.instance(getContext()).getChangelog();

        if (changes == null || changes.isEmpty()) return;

        List<UiOptionItem> changelog = new ArrayList<>();

        for (String change : changes) {
            changelog.add(UiOptionItem.from(change));
        }

        String title = String.format(
            "%s %s",
            "Changelog",
            AppInfoHelpers.getAppVersionName(getContext())
        );

        settingsPresenter.appendStringsCategory(title, changelog);

    }

}
