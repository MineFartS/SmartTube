package smartyoutubetv1.app.presenters.settings;

import android.content.Context;
import smartyoutubetv1.R;
import smartyoutubetv1.app.models.playback.ui.OptionCategory;
import smartyoutubetv1.app.models.playback.ui.UiOptionItem;
import smartyoutubetv1.app.presenters.AppDialogPresenter;
import smartyoutubetv1.app.presenters.base.BasePresenter;
import smartyoutubetv1.prefs.PlayerData;
import smartyoutubetv1.utils.AppDialogUtil;
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
