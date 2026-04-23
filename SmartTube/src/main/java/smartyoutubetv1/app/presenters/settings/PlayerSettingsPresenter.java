package SmartTubeApp.app.presenters.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Pair;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.prefs.GlobalPreferences;
import SmartTubeApp.R;
import SmartTubeApp.app.models.playback.ui.OptionCategory;
import SmartTubeApp.app.models.playback.ui.OptionItem;
import SmartTubeApp.app.models.playback.ui.UiOptionItem;
import SmartTubeApp.app.presenters.AppDialogPresenter;
import SmartTubeApp.app.presenters.base.BasePresenter;
import SmartTubeApp.app.presenters.service.SidebarService;
import SmartTubeApp.exoplayer.selector.TrackSelectorUtil;
import SmartTubeApp.prefs.GeneralData;
import SmartTubeApp.prefs.PlayerData;
import SmartTubeApp.prefs.PlayerTweaksData;
import SmartTubeApp.prefs.SearchData;
import SmartTubeApp.utils.AppDialogUtil;
import com.liskovsoft.youtubeapi.service.internal.MediaServiceData;

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
        
        appendPlayerButtonsCategory(settingsPresenter);
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

    private void appendPlayerButtonsCategory(AppDialogPresenter settingsPresenter) {
        List<OptionItem> options = new ArrayList<>();

        for (int[] pair : new int[][] {
            {
                R.string.open_chat,
                PlayerTweaksData.PLAYER_BUTTON_CHAT
            },
            {
                R.string.action_video_info,
                PlayerTweaksData.PLAYER_BUTTON_VIDEO_INFO
            },
            {
                R.string.action_channel, 
                PlayerTweaksData.PLAYER_BUTTON_OPEN_CHANNEL
            },
            {
                R.string.action_video_speed, 
                PlayerTweaksData.PLAYER_BUTTON_VIDEO_SPEED
            },
            {
                R.string.action_subtitles, 
                PlayerTweaksData.PLAYER_BUTTON_SUBTITLES
            },
            {
                R.string.action_subscribe, 
                PlayerTweaksData.PLAYER_BUTTON_SUBSCRIBE
            },
            {
                R.string.action_like, 
                PlayerTweaksData.PLAYER_BUTTON_LIKE
            },
            {
                R.string.action_dislike, 
                PlayerTweaksData.PLAYER_BUTTON_DISLIKE
            },
            {
                R.string.action_playlist_add, 
                PlayerTweaksData.PLAYER_BUTTON_ADD_TO_PLAYLIST
            },
            {
                R.string.action_play_pause,
                PlayerTweaksData.PLAYER_BUTTON_PLAY_PAUSE
            },
            {
                R.string.action_repeat_mode, 
                PlayerTweaksData.PLAYER_BUTTON_REPEAT_MODE
            },
            {
                R.string.action_next, 
                PlayerTweaksData.PLAYER_BUTTON_NEXT
            },
            {
                R.string.action_previous, 
                PlayerTweaksData.PLAYER_BUTTON_PREVIOUS
            },
            {
                R.string.playback_settings, 
                PlayerTweaksData.PLAYER_BUTTON_HIGH_QUALITY
            }
        }) {
            options.add(
                UiOptionItem.from(
                    getContext().getString(pair[0]), 
                    optionItem -> {
                        if (optionItem.isSelected()) {
                            mPlayerTweaksData.setPlayerButtonEnabled(pair[1]);
                        } else {
                            mPlayerTweaksData.setPlayerButtonDisabled(pair[1]);
                        }
                    }, 
                    mPlayerTweaksData.isPlayerButtonEnabled(pair[1])
                )
            );
        }

        settingsPresenter.appendCheckedCategory(
            getContext().getString(R.string.player_buttons),
            options
        );
    
    }

    private void appendMiscCategory(AppDialogPresenter settingsPresenter) {

        List<OptionItem> options = new ArrayList<>();

        options.add(UiOptionItem.from(getContext().getString(R.string.player_section_playlist),
                option -> mPlayerTweaksData.setSectionPlaylistEnabled(option.isSelected()),
                mPlayerTweaksData.isSectionPlaylistEnabled()));

        options.add(UiOptionItem.from(getContext().getString(R.string.search_background_playback),
                option -> mSearchData.setTempBackgroundModeEnabled(option.isSelected()),
                mSearchData.isTempBackgroundModeEnabled()));

        options.add(UiOptionItem.from(getContext().getString(R.string.player_loop_shorts),
                option -> mPlayerTweaksData.setLoopShortsEnabled(option.isSelected()),
                mPlayerTweaksData.isLoopShortsEnabled()));

        options.add(UiOptionItem.from(getContext().getString(R.string.remember_position_of_live_videos),
                option -> mPlayerTweaksData.setRememberPositionOfLiveVideosEnabled(option.isSelected()),
                mPlayerTweaksData.isRememberPositionOfLiveVideosEnabled()));

        options.add(UiOptionItem.from(getContext().getString(R.string.player_show_tooltips),
                option -> mPlayerData.setTooltipsEnabled(option.isSelected()),
                mPlayerData.isTooltipsEnabled()));

        settingsPresenter.appendCheckedCategory(getContext().getString(R.string.player_other), options);

    }

}
