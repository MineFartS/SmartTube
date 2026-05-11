package minefarts.smarttube.app.presenters.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Pair;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.prefs.GlobalPreferences;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.playback.ui.OptionCategory;
import minefarts.smarttube.app.models.playback.ui.OptionItem;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.app.presenters.service.SidebarService;
import minefarts.smarttube.exoplayer.selector.TrackSelectorUtil;
import minefarts.smarttube.prefs.GeneralData;
import minefarts.smarttube.prefs.PlayerData;
import minefarts.smarttube.prefs.PlayerTweaksData;
import minefarts.smarttube.prefs.SearchData;
import minefarts.smarttube.utils.AppDialogUtil;
import com.liskovsoft.sharedutils.service.internal.MediaServiceData;

import java.util.ArrayList;
import java.util.List;

public class PlayerSettingsPresenter extends BasePresenter<Void> {
    
    private final PlayerData mPlayerData;

    private final PlayerTweaksData mPlayerTweaksData;

    private final SearchData mSearchData;

    private final GeneralData mGeneralData;

    private final SidebarService mSidebarService;

    private final MediaServiceData mMediaServiceData;
    
    private boolean mRestartApp;;

    private PlayerSettingsPresenter(Context context) {
        super(context);
        mPlayerData = PlayerData.instance(context);
        mPlayerTweaksData = PlayerTweaksData.instance(context);
        mSearchData = SearchData.instance(context);
        mGeneralData = GeneralData.instance(context);
        mSidebarService = SidebarService.instance(context);
        mMediaServiceData = MediaServiceData.instance();
    }

    public static PlayerSettingsPresenter instance(Context context) {
        return new PlayerSettingsPresenter(context);
    }

    public void show() {

        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

        OptionCategory category;

        category = AppDialogUtil.createPlaybackModeCategory(getContext());
        settingsPresenter.appendCategory(category);
        
        category = AppDialogUtil.createVideoPresetsCategory(getContext());
        settingsPresenter.appendCategory(category);
        
        appendVideoSpeedCategory(settingsPresenter);
        
        category = AppDialogUtil.createAudioLanguageCategory(getContext());
        settingsPresenter.appendCategory(category);

        appendMiscCategory(settingsPresenter);

        settingsPresenter.showDialog(
            getContext().getString(R.string.settings_player),
            () -> {
                if (mRestartApp) {
                    mRestartApp = false;
                    MessageHelpers.showLongMessage(getContext(), R.string.msg_restart_app);
                }
            }
        );

    }

    private void appendVideoSpeedCategory(AppDialogPresenter settingsPresenter) {
        settingsPresenter.appendSingleButton(UiOptionItem.from(getContext().getString(R.string.video_speed), optionItem -> {
            AppDialogPresenter settingsPresenter2 = AppDialogPresenter.instance(getContext());
            settingsPresenter2.appendCategory(AppDialogUtil.createSpeedListCategory(getContext(), null));
            settingsPresenter2.appendCategory(AppDialogUtil.createRememberSpeedCategory(getContext()));
            settingsPresenter2.appendCategory(AppDialogUtil.createSpeedMiscCategory(getContext()));
            settingsPresenter2.showDialog(getContext().getString(R.string.video_speed));
        }));
    }

    private void appendMiscCategory(AppDialogPresenter settingsPresenter) {

        List<OptionItem> options = new ArrayList<>();

        options.add(UiOptionItem.from(getContext().getString(R.string.player_section_playlist),
                option -> mPlayerTweaksData.setSectionPlaylistEnabled(option.isSelected()),
                mPlayerTweaksData.isSectionPlaylistEnabled()));

        options.add(UiOptionItem.from(getContext().getString(R.string.player_loop_shorts),
                option -> mPlayerTweaksData.setLoopShortsEnabled(option.isSelected()),
                mPlayerTweaksData.isLoopShortsEnabled()));

        options.add(UiOptionItem.from(getContext().getString(R.string.remember_position_of_live_videos),
                option -> mPlayerTweaksData.setRememberPositionOfLiveVideosEnabled(option.isSelected()),
                mPlayerTweaksData.isRememberPositionOfLiveVideosEnabled()));

        settingsPresenter.appendCheckedCategory(getContext().getString(R.string.player_other), options);

    }

}
