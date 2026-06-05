package minefarts.smarttube.app.presenters.settings;

import android.content.Context;
import android.os.Build;

import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.helpers.MessageHelpers;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.app.presenters.BrowsePresenter;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.prefs.GeneralData;
import minefarts.smarttube.prefs.MainUIData;
import minefarts.smarttube.ui.playback.PlaybackFragment2;

import java.util.ArrayList;
import java.util.List;

public class MainUISettingsPresenter extends BasePresenter<Void> {
    
    private final MainUIData mMainUIData;

    private final GeneralData mGeneralData;

    private final PlaybackFragment2 mPlayerData;

    private boolean mRestartApp;

    private MainUISettingsPresenter(Context context) {
        super(context);
        mMainUIData = MainUIData.instance(context);
        mGeneralData = GeneralData.instance(context);
        mPlayerData = PlaybackFragment2.instance(context);
    }

    public static MainUISettingsPresenter instance(Context context) {
        return new MainUISettingsPresenter(context);
    }

    public void show() {

        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

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

    private void appendMiscCategory(AppDialogPresenter settingsPresenter) {
       
        List<UiOptionItem> options = new ArrayList<>();

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
