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
import com.liskovsoft.smartyoutubetv2.common.prefs.MainUIData;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerTweaksData;

class SettingsOverride {

    public void Run(
        Context context, 
        AppDialogPresenter settingsPresenter
    ) {

        ContentBlock(context);

        Misc(context);

        HideContent();

        Sidebar(context);

        ContextMenu(context);

        PlayerButtons(context);

        settingsPresenter.closeDialog();

    }

    private void ContentBlock(Context context) {

        ContentBlockData CBD = ContentBlockData.instance(context);


        // Disable all ContentBlock Color Markers
        for (String category : CBD.getAllCategories()) {
            CBD.disableColorMarker(category);
        }

        // ReEnable ContentBlock Color Marker for Sponsors
        CBD.enableColorMarker(SponsorSegment.CATEGORY_SPONSOR);

    }

    private void Misc(Context context) {

        GeneralData GD = GeneralData.instance(context);

        // Enable Update Notification
        GD.setOldUpdateNotificationsEnabled(true);

        // Use 24-hour time
        GD.set24HourLocaleEnabled(true);

    }

    private void HideContent() {

        MediaServiceData MSD = MediaServiceData.instance();

        // Hide Content Mixes
        MSD.setContentHidden(MediaServiceData.CONTENT_MIXES, true);

        // Hide Shorts from Channels
        MSD.setContentHidden(MediaServiceData.CONTENT_SHORTS_CHANNEL, true);

        // Hide Shorts from Home
        MSD.setContentHidden(MediaServiceData.CONTENT_SHORTS_HOME, true);

        // Hide Shorts from Search Results
        MSD.setContentHidden(MediaServiceData.CONTENT_SHORTS_SEARCH, true);

        // Hide Shorts from Subscriptions
        MSD.setContentHidden(MediaServiceData.CONTENT_SHORTS_SUBSCRIPTIONS, true);

    }

    private void Sidebar(Context context) {

        BrowsePresenter BP = BrowsePresenter.instance(context);

        // Remove Notifications Section
        BP.enableSection(MediaGroup.TYPE_NOTIFICATIONS, false);

        // Remove Playback Queue Section
        BP.enableSection(MediaGroup.TYPE_PLAYBACK_QUEUE, false);

    }

    private void ContextMenu(Context context) {

        MainUIData MUID = MainUIData.instance(context);

        // Hide 'Stream Reminder'
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_STREAM_REMINDER);

        // Hide 'Create Playlist'
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_CREATE_PLAYLIST);

        // Hide 'Rename Playlist'
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_RENAME_PLAYLIST);

        // Hide 'Add to new Playlist'
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_ADD_TO_NEW_PLAYLIST);

        // Hide 'Hide'
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_REMOVE_FROM_SUBSCRIPTIONS);

        // Hide 'Sort Playlist'
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_PLAYLIST_ORDER);

        // Hide 'Add Item to Sidebar'
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_PIN_TO_SIDEBAR);

        // Hide 'Removed Saved Playlist'
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_SAVE_REMOVE_PLAYLIST);

        // Hide 'Move Section Up'
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_MOVE_SECTION_UP);

        // Hide 'Move section down'
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_MOVE_SECTION_DOWN);

        // Hide 'Play Next'
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_PLAY_NEXT);

        // Hide 'Clear History'
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_CLEAR_HISTORY);

        // Show 'Mark as Watched'
        MUID.setMenuItemEnabled(MainUIData.MENU_ITEM_MARK_AS_WATCHED);

        // ============================================================

        // Move 'Mark as Watched' to Index 0
        MUID.setMenuItemIndex(0, MainUIData.MENU_ITEM_MARK_AS_WATCHED);

        // Move 'Not Interested' to Index 1
        MUID.setMenuItemIndex(1, MainUIData.MENU_ITEM_NOT_INTERESTED);

        // Move 'Don't Recommend Channel' to Index 2
        MUID.setMenuItemIndex(2, MainUIData.MENU_ITEM_NOT_RECOMMEND_CHANNEL);

        // Move 'Open Playlist' to Index 3
        MUID.setMenuItemIndex(3, MainUIData.MENU_ITEM_OPEN_PLAYLIST);

        // Move 'Open Channel' to Index 4
        MUID.setMenuItemIndex(4, MainUIData.MENU_ITEM_OPEN_CHANNEL);

        // Move 'Add/Remove to Playlist' to Index 5
        MUID.setMenuItemIndex(5, MainUIData.MENU_ITEM_ADD_TO_PLAYLIST);

        // Move 'Subscribe' to Index 6
        MUID.setMenuItemIndex(6, MainUIData.MENU_ITEM_SUBSCRIBE);

        // Move 'Remove from History' to Index 7
        MUID.setMenuItemIndex(7, MainUIData.MENU_ITEM_REMOVE_FROM_HISTORY);

    }

    private void PlayerButtons(Context context) {

        PlayerTweaksData PTD = PlayerTweaksData.instance(context);

        // Show Player UI when switching to the next video
        PTD.setPlayerUiOnNextEnabled(true);

        // ============================================================

        // Hide 'Video Stats'
        PTD.setPlayerButtonDisabled(PlayerTweaksData.PLAYER_BUTTON_VIDEO_STATS);

        // Hide 'Screen Dimming'
        PTD.setPlayerButtonDisabled(PlayerTweaksData.PLAYER_BUTTON_SCREEN_DIMMING);

        // Hide 'Search'
        PTD.setPlayerButtonDisabled(PlayerTweaksData.PLAYER_BUTTON_SEARCH);

        // Hide 'Picture in picture'
        PTD.setPlayerButtonDisabled(PlayerTweaksData.PLAYER_BUTTON_PIP);

        // Hide 'Add to playlist'
        PTD.setPlayerButtonDisabled(PlayerTweaksData.PLAYER_BUTTON_ADD_TO_PLAYLIST);

        // Hide 'Playback quality'
        PTD.setPlayerButtonDisabled(PlayerTweaksData.PLAYER_BUTTON_HIGH_QUALITY);

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
            "Use Phil's Presets",
            option -> SO.Run(
                getContext(), 
                settingsPresenter
            )
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
