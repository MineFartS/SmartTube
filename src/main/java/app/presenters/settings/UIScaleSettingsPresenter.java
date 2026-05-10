package minefarts.smarttube.app.presenters.settings;

import android.content.Context;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.playback.ui.OptionItem;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.prefs.MainUIData;

import java.util.ArrayList;
import java.util.List;

public class UIScaleSettingsPresenter extends BasePresenter<Void> {
    private final MainUIData mMainUIData;
    private boolean mRestartApp;
    private final Runnable mOnFinish = () -> {
        if (mRestartApp) {
            mRestartApp = false;
            MessageHelpers.showLongMessage(getContext(), R.string.msg_restart_app);
        }
    };

    public UIScaleSettingsPresenter(Context context) {
        super(context);
        mMainUIData = MainUIData.instance(context);
    }

    public static UIScaleSettingsPresenter instance(Context context) {
        return new UIScaleSettingsPresenter(context);
    }

    public void show() {
        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

        settingsPresenter.showDialog(getContext().getString(R.string.settings_ui_scale), mOnFinish);
    }

}
