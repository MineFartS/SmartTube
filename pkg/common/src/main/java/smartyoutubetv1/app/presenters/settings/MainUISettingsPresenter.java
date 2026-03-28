package smartyoutubetv1.app.presenters.settings;

import android.content.Context;
import android.os.Build;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import smartyoutubetv1.R;
import smartyoutubetv1.app.models.playback.ui.OptionItem;
import smartyoutubetv1.app.models.playback.ui.UiOptionItem;
import smartyoutubetv1.app.presenters.AppDialogPresenter;
import smartyoutubetv1.app.presenters.BrowsePresenter;
import smartyoutubetv1.app.presenters.base.BasePresenter;

import smartyoutubetv1.prefs.GeneralData;
import smartyoutubetv1.prefs.MainUIData;
import smartyoutubetv1.prefs.PlayerData;

import java.util.ArrayList;
import java.util.List;

public class MainUISettingsPresenter extends BasePresenter<Void> {
    
    private final MainUIData mMainUIData;

    private final GeneralData mGeneralData;

    private final PlayerData mPlayerData;

    private boolean mRestartApp;

    private MainUISettingsPresenter(Context context) {
        super(context);
        mMainUIData = MainUIData.instance(context);
        mGeneralData = GeneralData.instance(context);
        mPlayerData = PlayerData.instance(context);
    }

    public static MainUISettingsPresenter instance(Context context) {
        return new MainUISettingsPresenter(context);
    }

    public void show() {

        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

        appendCardPreviews(settingsPresenter);

        appendChannelSortingCategory(settingsPresenter);

        appendMiscCategory(settingsPresenter);

        settingsPresenter.showDialog(
            getContext().getString(R.string.dialog_main_ui), 
            () -> {
                if (mRestartApp) {
                    mRestartApp = false;
                    MessageHelpers.showLongMessage(getContext(), R.string.msg_restart_app);
                }
            }
        );

    }

    private void appendCardPreviews(AppDialogPresenter settingsPresenter) {
        List<OptionItem> options = new ArrayList<>();

        for (int[] pair : new int[][] {
                {R.string.option_disabled, MainUIData.CARD_PREVIEW_DISABLED},
                {R.string.card_preview_full, MainUIData.CARD_PREVIEW_FULL},
                {R.string.card_preview_muted, MainUIData.CARD_PREVIEW_MUTED}}) {
            options.add(UiOptionItem.from(getContext().getString(pair[0]), optionItem -> {
                mMainUIData.setCardPreviewType(pair[1]);
            }, mMainUIData.getCardPreviewType() == pair[1]));
        }

        settingsPresenter.appendRadioCategory(getContext().getString(R.string.card_preview), options);
    }

    private void appendChannelSortingCategory(AppDialogPresenter settingsPresenter) {
        List<OptionItem> options = new ArrayList<>();

        for (int[] pair : new int[][] {
                {R.string.sorting_last_viewed, MainUIData.CHANNEL_SORTING_LAST_VIEWED},
                {R.string.sorting_alphabetically, MainUIData.CHANNEL_SORTING_NAME},
                {R.string.sorting_by_new_content, MainUIData.CHANNEL_SORTING_NEW_CONTENT}}) {
            options.add(UiOptionItem.from(getContext().getString(pair[0]), optionItem -> {
                mMainUIData.setChannelCategorySorting(pair[1]);
                BrowsePresenter.instance(getContext()).updateChannelSorting();
            }, mMainUIData.getChannelCategorySorting() == pair[1]));
        }

        settingsPresenter.appendRadioCategory(getContext().getString(R.string.channels_section_sorting), options);
    }

    private void appendMiscCategory(AppDialogPresenter settingsPresenter) {
       
        List<OptionItem> options = new ArrayList<>();

        options.add(UiOptionItem.from(
            getContext().getString(R.string.time_format_24) + " " + getContext().getString(R.string.time_format),
            option -> {
                mGeneralData.set24HourLocaleEnabled(option.isSelected());
                mRestartApp = true;
            },
            mGeneralData.is24HourLocaleEnabled()
        ));

        settingsPresenter.appendCheckedCategory(
            getContext().getString(R.string.player_other), 
            options
        );
        
    }

}
