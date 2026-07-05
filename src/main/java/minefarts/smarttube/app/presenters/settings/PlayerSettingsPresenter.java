package minefarts.smarttube.app.presenters.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Pair;

import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.helpers.MessageHelpers;
import minefarts.smarttube.utils.prefs.GlobalPreferences;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.playback.ui.OptionCategory;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.app.presenters.service.SidebarService;
import minefarts.smarttube.exoplayer.selector.TrackSelectorUtil;
import minefarts.smarttube.prefs.GeneralData;
import minefarts.smarttube.ui.playback.PlaybackFragment2;
import minefarts.smarttube.prefs.PlayerTweaksData;
import minefarts.smarttube.prefs.SearchData;
import minefarts.smarttube.utils.AppDialogUtil;
import com.liskovsoft.youtubeapi.service.internal.MediaServiceData;

import java.util.ArrayList;
import java.util.List;

public class PlayerSettingsPresenter extends BasePresenter<Void> {
    
    PlaybackFragment2 mPlayerData;
    PlayerTweaksData mPlayerTweaksData;
    SearchData mSearchData;
    GeneralData mGeneralData;
    SidebarService mSidebarService;
    MediaServiceData mMediaServiceData;
    
    private boolean mRestartApp;;

    public static PlayerSettingsPresenter instance(Context context) {
        PlayerSettingsPresenter pres = new PlayerSettingsPresenter();

        pres.mPlayerData = PlaybackFragment2.instance(context);
        pres.mPlayerTweaksData = PlayerTweaksData.instance(context);
        pres.mSearchData = SearchData.instance(context);
        pres.mGeneralData = GeneralData.instance(context);
        pres.mSidebarService = SidebarService.instance(context);
        pres.mMediaServiceData = MediaServiceData.instance();

        pres.setContext(context);

        return pres;
    }

    public void show() {

        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

        OptionCategory category;

        category = AppDialogUtil.createPlaybackModeCategory(getContext());
        settingsPresenter.appendCategory(category);
        
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

    private void appendMiscCategory(AppDialogPresenter settingsPresenter) {

        List<UiOptionItem> options = new ArrayList<>();

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
