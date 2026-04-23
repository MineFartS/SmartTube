package SmartTubeApp.app.presenters.settings;

import android.content.Context;
import SmartTubeApp.R;
import SmartTubeApp.app.models.playback.ui.OptionCategory;
import SmartTubeApp.app.models.playback.ui.UiOptionItem;
import SmartTubeApp.app.presenters.AppDialogPresenter;
import SmartTubeApp.app.presenters.base.BasePresenter;
import SmartTubeApp.prefs.PlayerData;
import SmartTubeApp.utils.AppDialogUtil;
import com.liskovsoft.youtubeapi.service.internal.MediaServiceData;

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
