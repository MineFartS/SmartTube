package smartyoutubetv1.app.presenters.settings;

import android.content.Context;
import smartyoutubetv1.AppUpdateChecker;
import com.liskovsoft.sharedutils.helpers.AppInfoHelpers;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.locale.LocaleUtility;
import smartyoutubetv1.R;
import smartyoutubetv1.app.models.playback.ui.OptionItem;
import smartyoutubetv1.app.models.playback.ui.UiOptionItem;
import smartyoutubetv1.app.presenters.AppDialogPresenter;
import smartyoutubetv1.app.presenters.dialogs.AppUpdatePresenter;
import smartyoutubetv1.app.presenters.base.BasePresenter;
import smartyoutubetv1.prefs.ContentBlockData;
import smartyoutubetv1.prefs.GeneralData;
import smartyoutubetv1.utils.Utils;
import com.liskovsoft.mediaserviceinterfaces.data.SponsorSegment;
import smartyoutubetv1.prefs.GeneralData;
import com.liskovsoft.youtubeapi.service.internal.MediaServiceData;
import smartyoutubetv1.app.presenters.BrowsePresenter;
import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import smartyoutubetv1.prefs.PlayerData;
import smartyoutubetv1.prefs.MainUIData;
import smartyoutubetv1.prefs.PlayerTweaksData;

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

        appendUpdateSource(settingsPresenter);

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

    private void appendUpdateSource(AppDialogPresenter settingsPresenter) {
        
        List<OptionItem> options = new ArrayList<>();

        String[] updateUrls = getContext().getResources().getStringArray(R.array.update_urls);

        if (updateUrls.length <= 1) {
            return;
        }

        if (mUpdateChecker.getPreferredHost() == null) {
            mUpdateChecker.setPreferredHost(Helpers.getHost(updateUrls[0]));
        }

        for (String url : updateUrls) {

            String hostName = Helpers.getHost(url);
            
            options.add(
                UiOptionItem.from(
                    hostName,
                    optionItem -> mUpdateChecker.setPreferredHost(hostName),
                    Helpers.equals(
                        hostName, 
                        mUpdateChecker.getPreferredHost()
                    )
                )
            );
        
        }

        settingsPresenter.appendRadioCategory(
            getContext().getString(R.string.preferred_update_source), 
            options
        );

    }

}
