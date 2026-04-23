package SmartTubeApp.app.presenters.settings;

import android.annotation.SuppressLint;
import android.content.Context;

import com.liskovsoft.sharedutils.data.MediaGroup;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.okhttp.OkHttpManager;
import SmartTubeApp.R;
import SmartTubeApp.app.models.data.Video;
import SmartTubeApp.app.models.playback.manager.PlayerConstants;
import SmartTubeApp.app.models.playback.ui.OptionCategory;
import SmartTubeApp.app.models.playback.ui.OptionItem;
import SmartTubeApp.app.models.playback.ui.UiOptionItem;
import SmartTubeApp.app.presenters.AppDialogPresenter;
import SmartTubeApp.app.presenters.BrowsePresenter;
import SmartTubeApp.app.presenters.base.BasePresenter;
import SmartTubeApp.app.presenters.dialogs.menu.providers.ContextMenuManager;
import SmartTubeApp.app.presenters.dialogs.menu.providers.ContextMenuProvider;
import SmartTubeApp.app.presenters.service.SidebarService;
import SmartTubeApp.misc.MediaServiceManager;
import SmartTubeApp.prefs.AppPrefs;
import SmartTubeApp.prefs.GeneralData;
import SmartTubeApp.prefs.MainUIData;
import SmartTubeApp.prefs.PlayerData;
import SmartTubeApp.prefs.PlayerTweaksData;
import SmartTubeApp.prefs.SearchData;
import SmartTubeApp.utils.AppDialogUtil;
import SmartTubeApp.utils.SimpleEditDialog;
import com.liskovsoft.sharedutils.service.internal.MediaServiceData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GeneralSettingsPresenter extends BasePresenter<Void> {

    private final GeneralData mGeneralData;

    private final PlayerData mPlayerData;

    private final PlayerTweaksData mPlayerTweaksData;

    private final MainUIData mMainUIData;

    private final MediaServiceData mMediaServiceData;

    private final SidebarService mSidebarService;

    private boolean mRestartApp;

    private GeneralSettingsPresenter(Context context) {

        super(context);
        
        mGeneralData = GeneralData.instance(context);
        
        mPlayerData = PlayerData.instance(context);
        
        mPlayerTweaksData = PlayerTweaksData.instance(context);
        
        mMainUIData = MainUIData.instance(context);
        
        mMediaServiceData = MediaServiceData.instance();
        
        mSidebarService = SidebarService.instance(context);

    }

    public static GeneralSettingsPresenter instance(Context context) {
        return new GeneralSettingsPresenter(context);
    }

    public void show() {

        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

        appendEnabledSections(settingsPresenter);
        appendContextMenuItemsCategory(settingsPresenter);
        appendHideVideos(settingsPresenter);
        appendBackgroundPlaybackCategory(settingsPresenter);
        appendMiscCategory(settingsPresenter);

        settingsPresenter.showDialog(
            getContext().getString(R.string.settings_general), 
            () -> {
                if (mRestartApp) {
                    
                    mRestartApp = false;
                    
                    MessageHelpers.showLongMessage(
                        getContext(), 
                        R.string.msg_restart_app
                    );
                
                }
            }
        );

    }

    private void appendEnabledSections(AppDialogPresenter settingsPresenter) {
        
        List<OptionItem> options = new ArrayList<>();

        Map<Integer, Integer> sections = mSidebarService.getDefaultSections();

        for (Entry<Integer, Integer> section : sections.entrySet()) {
        
            int sectionResId = section.getKey();
            int sectionId = section.getValue();

            if (sectionId == MediaGroup.TYPE_SETTINGS) {
                continue;
            }

            options.add(
                UiOptionItem.from(
                    getContext().getString(sectionResId), 
                    optionItem -> {
                        BrowsePresenter.instance(getContext()).enableSection(
                            sectionId,
                            optionItem.isSelected()
                        );
                    }, 
                    mSidebarService.isSectionPinned(sectionId)
                )
            );
        
        }

        settingsPresenter.appendCheckedCategory(
            getContext().getString(R.string.side_panel_sections), 
            options
        );

    }

    private void appendHideVideos(AppDialogPresenter settingsPresenter) {
        
        List<OptionItem> options = new ArrayList<>();

        options.add(
            UiOptionItem.from(
                "Content Mixes",
                option -> mMediaServiceData.setContentHidden(MediaServiceData.CONTENT_MIXES, option.isSelected()),
                mMediaServiceData.isContentHidden(MediaServiceData.CONTENT_MIXES)
            )
        );

        options.add(
            UiOptionItem.from(
                "Hide watched videos",
                option -> mMediaServiceData.setContentHidden(MediaServiceData.CONTENT_WATCHED, option.isSelected()),
                mMediaServiceData.isContentHidden(MediaServiceData.CONTENT_WATCHED)
            )
        );

        options.add(
            UiOptionItem.from(
                "Shorts",
                option -> mMediaServiceData.setContentHidden(MediaServiceData.CONTENT_SHORTS, option.isSelected()),
                mMediaServiceData.isContentHidden(MediaServiceData.CONTENT_SHORTS)
            )
        );

        options.add(
            UiOptionItem.from(
                "Upcoming Streams",
                option -> mMediaServiceData.setContentHidden(MediaServiceData.CONTENT_UPCOMING, option.isSelected()),
                mMediaServiceData.isContentHidden(MediaServiceData.CONTENT_UPCOMING)
            )
        );

        options.add(
            UiOptionItem.from(
                getContext().getString(R.string.hide_streams),
                option -> mMediaServiceData.setContentHidden(MediaServiceData.CONTENT_STREAMS_SUBSCRIPTIONS, option.isSelected()),
                mMediaServiceData.isContentHidden(MediaServiceData.CONTENT_STREAMS_SUBSCRIPTIONS)
            )
        );

        settingsPresenter.appendCheckedCategory(
            getContext().getString(R.string.hide_unwanted_content),
            options
        );
    
    }

    private void appendContextMenuItemsCategory(AppDialogPresenter settingsPresenter) {

        List<OptionItem> options = new ArrayList<>();

        Map<Long, Integer> menuNames = getMenuNames();

        for (Long menuItem : mMainUIData.getMenuItemsOrdered()) {

            Integer nameResId = menuNames.get(menuItem);

            if (nameResId == null) {
                continue;
            }

            options.add(
                UiOptionItem.from(
                    getContext().getString(nameResId),
                    optionItem -> {
                        if (optionItem.isSelected()) {
                        
                            mMainUIData.setMenuItemEnabled(menuItem);
                            showMenuItemOrderDialog(menuItem);
                        
                        } else {
                            mMainUIData.setMenuItemDisabled(menuItem);
                        }
                    },
                    mMainUIData.isMenuItemEnabled(menuItem)
                )
            );

        }

        settingsPresenter.appendCheckedCategory(
            getContext().getString(R.string.context_menu), 
            options
        );

    }

    private void showMenuItemOrderDialog(Long menuItem) {
        
        AppDialogPresenter dialog = AppDialogPresenter.instance(getContext());

        List<OptionItem> options = new ArrayList<>();

        Map<Long, Integer> menuNames = getMenuNames();

        Integer currentNameResId = menuNames.get(menuItem);

        if (currentNameResId == null) {
            return;
        }

        List<Long> menuItemsOrdered = mMainUIData.getMenuItemsOrdered();

        int size = menuItemsOrdered.size();
        int currentIndex = mMainUIData.getMenuItemIndex(menuItem);
        int counter = 0;

        for (int i = 0; i < size; i++) {

            Long item = menuItemsOrdered.get(i);
            Integer nameResId = menuNames.get(item);

            if (nameResId == null || !mMainUIData.isMenuItemEnabled(item)) {
                continue;
            }

            final int index = i;
            options.add(
                UiOptionItem.from(
                    (counter + 1) + " " + getContext().getString(nameResId), 
                    optionItem -> {
                        if (optionItem.isSelected()) {
                            mMainUIData.setMenuItemIndex(index, menuItem);
                            dialog.goBack();
                        }
                    },
                    currentIndex == i
                )
            );
            
            counter++;
        
        }

        String itemName = getContext().getString(currentNameResId);
        
        dialog.appendRadioCategory(
            getContext().getString(R.string.item_postion) + " " + itemName,
            options
        );

        dialog.showDialog();

    }

    private void appendBackgroundPlaybackCategory(AppDialogPresenter settingsPresenter) {
        
        OptionCategory category = AppDialogUtil.createBackgroundPlaybackCategory(
            getContext(), 
            mPlayerData, 
            mGeneralData
        );
        
        settingsPresenter.appendRadioCategory(
            category.title, 
            category.options
        );
    
    }

    private void appendMiscCategory(AppDialogPresenter settingsPresenter) {
        
        List<OptionItem> options = new ArrayList<>();

        options.add(
            UiOptionItem.from(
                getContext().getString(R.string.multi_profiles),
                option -> {
                    AppPrefs.instance(getContext()).enableMultiProfiles(option.isSelected());
                    BrowsePresenter.instance(getContext()).updateSections();
                },
                AppPrefs.instance(getContext()).isMultiProfilesEnabled()
            )
        );

        options.add(
            UiOptionItem.from(
                getContext().getString(R.string.remember_position_subscriptions),
                option -> mGeneralData.setRememberSubscriptionsPositionEnabled(option.isSelected()),
                mGeneralData.isRememberSubscriptionsPositionEnabled()
            )
        );

        settingsPresenter.appendCheckedCategory(
            getContext().getString(R.string.player_other), 
            options
        );
    
    }

    private Map<Long, Integer> getMenuNames() {

        Map<Long, Integer> menuNames = new HashMap<>();
        
        menuNames.put(
            MainUIData.MENU_ITEM_EXIT_FROM_PIP, 
            R.string.return_to_background_video
        );

        menuNames.put(
            MainUIData.MENU_ITEM_EXCLUDE_FROM_CONTENT_BLOCK, 
            R.string.content_block_exclude_channel
        );
        
        menuNames.put(MainUIData.MENU_ITEM_MARK_AS_WATCHED, R.string.mark_as_watched);
        menuNames.put(MainUIData.MENU_ITEM_OPEN_CHANNEL, R.string.open_channel);
        menuNames.put(MainUIData.MENU_ITEM_UPDATE_CHECK, R.string.check_for_updates);
        menuNames.put(MainUIData.MENU_ITEM_CLEAR_HISTORY, R.string.clear_history);
        menuNames.put(MainUIData.MENU_ITEM_TOGGLE_HISTORY, R.string.pause_history);
        menuNames.put(MainUIData.MENU_ITEM_PLAYLIST_ORDER, R.string.playlist_order);
        menuNames.put(MainUIData.MENU_ITEM_PLAY_NEXT, R.string.play_next);
        menuNames.put(MainUIData.MENU_ITEM_ADD_TO_QUEUE, R.string.add_remove_from_playback_queue);
        menuNames.put(MainUIData.MENU_ITEM_SUBSCRIBE, R.string.subscribe_unsubscribe_from_channel);
        menuNames.put(MainUIData.MENU_ITEM_SAVE_REMOVE_PLAYLIST, R.string.save_remove_playlist);
        menuNames.put(MainUIData.MENU_ITEM_CREATE_PLAYLIST, R.string.create_playlist);
        menuNames.put(MainUIData.MENU_ITEM_RENAME_PLAYLIST, R.string.rename_playlist);
        menuNames.put(MainUIData.MENU_ITEM_ADD_TO_NEW_PLAYLIST, R.string.add_video_to_new_playlist);
        menuNames.put(MainUIData.MENU_ITEM_ADD_TO_PLAYLIST, R.string.dialog_add_to_playlist);
        menuNames.put(MainUIData.MENU_ITEM_RECENT_PLAYLIST, R.string.add_remove_from_recent_playlist);
        menuNames.put(MainUIData.MENU_ITEM_PLAY_VIDEO, R.string.play_video);
        menuNames.put(MainUIData.MENU_ITEM_NOT_INTERESTED, R.string.not_interested);
        menuNames.put(MainUIData.MENU_ITEM_NOT_RECOMMEND_CHANNEL, R.string.not_recommend_channel);
        menuNames.put(MainUIData.MENU_ITEM_REMOVE_FROM_HISTORY, R.string.remove_from_history);
        menuNames.put(MainUIData.MENU_ITEM_REMOVE_FROM_SUBSCRIPTIONS, R.string.remove_from_subscriptions);
        menuNames.put(MainUIData.MENU_ITEM_PIN_TO_SIDEBAR, R.string.pin_unpin_from_sidebar);
        menuNames.put(MainUIData.MENU_ITEM_SHARE_LINK, R.string.share_link);
        menuNames.put(MainUIData.MENU_ITEM_SHARE_EMBED_LINK, R.string.share_embed_link);
        menuNames.put(MainUIData.MENU_ITEM_SHARE_QR_LINK, R.string.share_qr_link);
        menuNames.put(MainUIData.MENU_ITEM_SELECT_ACCOUNT, R.string.dialog_account_list);
        menuNames.put(MainUIData.MENU_ITEM_MOVE_SECTION_UP, R.string.move_section_up);
        menuNames.put(MainUIData.MENU_ITEM_MOVE_SECTION_DOWN, R.string.move_section_down);
        menuNames.put(MainUIData.MENU_ITEM_RENAME_SECTION, R.string.rename_section);
        menuNames.put(MainUIData.MENU_ITEM_OPEN_DESCRIPTION, R.string.action_video_info);
        menuNames.put(MainUIData.MENU_ITEM_OPEN_COMMENTS, R.string.open_comments);
        menuNames.put(MainUIData.MENU_ITEM_OPEN_PLAYLIST, R.string.open_playlist);

        for (ContextMenuProvider provider : new ContextMenuManager(getContext()).getProviders()) {
            menuNames.put(
                provider.getId(), 
                provider.getTitleResId()
            );
        }

        return menuNames;
    }
}
