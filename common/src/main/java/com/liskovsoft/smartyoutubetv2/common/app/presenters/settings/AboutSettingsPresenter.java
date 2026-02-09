package com.liskovsoft.smartyoutubetv2.common.app.presenters.settings;

import android.content.Context;
import com.liskovsoft.appupdatechecker2.AppUpdateChecker;
import com.liskovsoft.sharedutils.helpers.AppInfoHelpers;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.locale.LocaleUtility;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.UiOptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.AppDialogPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.dialogs.ATVBridgePresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.dialogs.AmazonBridgePresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.dialogs.AppUpdatePresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.base.BasePresenter;
import com.liskovsoft.smartyoutubetv2.common.prefs.ContentBlockData;
import com.liskovsoft.smartyoutubetv2.common.prefs.GeneralData;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.liskovsoft.mediaserviceinterfaces.data.SponsorSegment;
import com.liskovsoft.smartyoutubetv2.common.prefs.GeneralData;
import com.liskovsoft.youtubeapi.service.internal.MediaServiceData;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.BrowsePresenter;
import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerData;

class SettingsOverride {

    public void Run(Context context) {

        Config_ContentBlock(context);

        Update_Notifications(context);

        Hide_Content();

        Hide_Sidebar_Tabs(context);

        Highest_Buffer(context);

    }

    private void Config_ContentBlock(Context context) {

        ContentBlockData CBD = ContentBlockData.instance(context);

        // Don't Skip Segments Again
        CBD.enableDontSkipSegmentAgain(true);


        // Disable all ContentBlock Color Markers
        for (String category : CBD.getAllCategories()) {
            CBD.disableColorMarker(category);
        }

        // ReEnable ContentBlock Color Marker for Sponsors
        CBD.enableColorMarker(SponsorSegment.CATEGORY_SPONSOR);

    }

    private void Update_Notifications(Context context) {

        GeneralData GD = GeneralData.instance(context);

        // Enable Update Notification
        GD.setOldUpdateNotificationsEnabled(true);

    }

    private void Hide_Content() {

        MediaServiceData MSD = MediaServiceData.instance();

        // Hide Content Mixes
        MSD.setContentHidden(MediaServiceData.CONTENT_MIXES, true);

        // Hide Watched Videos from Home
        MSD.setContentHidden(MediaServiceData.CONTENT_WATCHED_HOME, true);

        // Hide Shorts from Channels
        MSD.setContentHidden(MediaServiceData.CONTENT_SHORTS_CHANNEL, true);

        // Hide Shorts from Home
        MSD.setContentHidden(MediaServiceData.CONTENT_SHORTS_HOME, true);

        // Hide Shorts from Search Results
        MSD.setContentHidden(MediaServiceData.CONTENT_SHORTS_SEARCH, true);

        // Hide Shorts from Subscriptions
        MSD.setContentHidden(MediaServiceData.CONTENT_SHORTS_SUBSCRIPTIONS, true);

    }

    private void Hide_Sidebar_Tabs(Context context) {

        BrowsePresenter BP = BrowsePresenter.instance(context);

        // Remove Shorts Section
        BP.enableSection(MediaGroup.TYPE_SHORTS, false);

        // Remove Trending Section
        BP.enableSection(MediaGroup.TYPE_TRENDING, false);

        // Remove Kids Section
        BP.enableSection(MediaGroup.TYPE_KIDS_HOME, false);

        // Remove Sports Section
        BP.enableSection(MediaGroup.TYPE_SPORTS, false);

        // Remove Live Section
        BP.enableSection(MediaGroup.TYPE_LIVE, false);

        // Remove Gaming Section
        BP.enableSection(MediaGroup.TYPE_GAMING, false);

        // Remove News Section
        BP.enableSection(MediaGroup.TYPE_NEWS, false);

        // Remove Music Section
        BP.enableSection(MediaGroup.TYPE_MUSIC, false);

        // Remove Channel Uploads Section
        BP.enableSection(MediaGroup.TYPE_CHANNEL_UPLOADS, false);

        // Remove My Videos Section
        BP.enableSection(MediaGroup.TYPE_MY_VIDEOS, false);

        // Remove Playback Queue Section
        BP.enableSection(MediaGroup.TYPE_PLAYBACK_QUEUE, false);

    }

    private void Highest_Buffer(Context context) {

        PlayerData PD = PlayerData.instance(context);

        // Raise Video Buffer to Maximum
        PD.setVideoBufferType(PlayerData.BUFFER_HIGHEST);

    }

}

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

        String mainTitle = String.format(
            "%s %s",
            getContext().getString(R.string.app_name),
            AppInfoHelpers.getAppVersionName(getContext())
        );

        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

        String country = LocaleUtility.getCurrentLocale(getContext()).getCountry();

        appendSettingsOverrideButton(settingsPresenter);

        appendUpdateCheckButton(settingsPresenter);

        appendUpdateChangelogButton(settingsPresenter);

        appendUpdateSource(settingsPresenter);

        appendInstallBridge(settingsPresenter);

        appendAutoUpdateSwitch(settingsPresenter);

        appendOldUpdateNotificationSwitch(settingsPresenter);

        if (!Helpers.equalsAny(country, "RU", "UA")) {
            appendDonation(settingsPresenter);
            appendFeedback(settingsPresenter);
            appendLinks(settingsPresenter);
        }

        settingsPresenter.showDialog(mainTitle);
    }

    private void appendAutoUpdateSwitch(AppDialogPresenter settingsPresenter) {
        settingsPresenter.appendSingleSwitch(
            UiOptionItem.from(
                getContext().getString(R.string.check_updates_auto), 
                optionItem -> {
                    mUpdateChecker.enableUpdateCheck(optionItem.isSelected());
                }, 
                mUpdateChecker.isUpdateCheckEnabled()
            )
        );
    }

    private void appendOldUpdateNotificationSwitch(AppDialogPresenter settingsPresenter) {

        GeneralData generalData = GeneralData.instance(getContext());
        
        settingsPresenter.appendSingleSwitch(
            UiOptionItem.from(
                getContext().getString(R.string.dialog_notification),
                optionItem -> generalData.setOldUpdateNotificationsEnabled(optionItem.isSelected()), 
                generalData.isOldUpdateNotificationsEnabled()
            )
        );
    
    }

    private void appendUpdateCheckButton(AppDialogPresenter settingsPresenter) {
        OptionItem updateCheckOption = UiOptionItem.from(
                getContext().getString(R.string.check_for_updates),
                option -> AppUpdatePresenter.instance(getContext()).start(true));

        settingsPresenter.appendSingleButton(updateCheckOption);
    }

    private void appendSettingsOverrideButton(AppDialogPresenter settingsPresenter) {

        SettingsOverride SO = new SettingsOverride();

        OptionItem button = UiOptionItem.from(
                "Override Settings",
                option -> SO.Run(getContext())
        );

        settingsPresenter.appendSingleButton(button);

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

    private void appendLinks(AppDialogPresenter settingsPresenter) {
        OptionItem releasesOption = UiOptionItem.from(
            getContext().getString(R.string.releases),
            option -> Utils.openLink(
                getContext(), 
                Utils.toQrCodeLink(getContext().getString(R.string.releases_url))
            )
        );

        OptionItem sourcesOption = UiOptionItem.from(
            getContext().getString(R.string.sources),
            option -> Utils.openLink(
                getContext(), 
                Utils.toQrCodeLink(getContext().getString(R.string.sources_url))
            )
        );

        settingsPresenter.appendSingleButton(releasesOption);
        settingsPresenter.appendSingleButton(sourcesOption);
        
    }

    private void appendDonation(AppDialogPresenter settingsPresenter) {

        List<OptionItem> donateOptions = new ArrayList<>();

        Map<String, String> donations = Helpers.getMap(getContext(), R.array.donations);

        for (Entry<String, String> entry : donations.entrySet()) {
            donateOptions.add(
                UiOptionItem.from(
                    entry.getKey(),
                    option -> Utils.openLink(
                        getContext(), 
                        Utils.toQrCodeLink(entry.getValue())
                    )
                )
            );
        }

        if (!donateOptions.isEmpty()) {
            settingsPresenter.appendStringsCategory(
                getContext().getString(R.string.donation), 
                donateOptions
            );
        }
    
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

    private void appendFeedback(AppDialogPresenter settingsPresenter) {
        
        List<OptionItem> feedbackOptions = new ArrayList<>();

        Map<String, String> feedback = Helpers.getMap(
            getContext(), 
            R.array.feedback
        );

        for (Entry<String, String> entry : feedback.entrySet()) {
            
            feedbackOptions.add(
                UiOptionItem.from(
                    entry.getKey(),
                    option -> Utils.openLink(
                        getContext(), 
                        Utils.toQrCodeLink(entry.getValue())
                    )
                )
            );
        }

        if (!feedbackOptions.isEmpty()) {
            settingsPresenter.appendStringsCategory(
                getContext().getString(R.string.feedback), 
                feedbackOptions
            );
        }
    }

    private void appendInstallBridge(AppDialogPresenter settingsPresenter) {
        
        OptionItem installBridgeOption = UiOptionItem.from(
            "Install ATV/Amazon bridge",
            option -> startBridgePresenter()
        );

        settingsPresenter.appendSingleButton(installBridgeOption);
    
    }

    private void startBridgePresenter() {
        
        MessageHelpers.showLongMessage(
            getContext(), 
            R.string.enable_voice_search_desc
        );

        ATVBridgePresenter atvPresenter = ATVBridgePresenter.instance(getContext());
        atvPresenter.runBridgeInstaller(true);
        atvPresenter.unhold();

        AmazonBridgePresenter amazonPresenter = AmazonBridgePresenter.instance(getContext());
        amazonPresenter.runBridgeInstaller(true);
        amazonPresenter.unhold();
    
    }

}
