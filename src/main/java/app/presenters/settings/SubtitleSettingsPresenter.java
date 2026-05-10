package minefarts.smarttube.app.presenters.settings;

import android.content.Context;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.playback.ui.OptionCategory;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.prefs.PlayerData;
import minefarts.smarttube.utils.AppDialogUtil;
import com.liskovsoft.sharedutils.service.internal.MediaServiceData;

public class SubtitleSettingsPresenter extends BasePresenter<Void> {
    private final PlayerData mPlayerData;

    public SubtitleSettingsPresenter(Context context) {
        super(context);
        mPlayerData = PlayerData.instance(context);
    }

    public static SubtitleSettingsPresenter instance(Context context) {
        return new SubtitleSettingsPresenter(context);
    }

    public void show() {
        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

        appendSubtitleStyleCategory(settingsPresenter);

        settingsPresenter.showDialog("Subtitles");
    }

    private void appendSubtitleStyleCategory(AppDialogPresenter settingsPresenter) {
        OptionCategory category = AppDialogUtil.createSubtitleStylesCategory(getContext());
        settingsPresenter.appendRadioCategory(category.title, category.options);
    }

}
