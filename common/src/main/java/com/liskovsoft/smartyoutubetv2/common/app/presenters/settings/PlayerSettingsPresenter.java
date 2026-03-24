package com.liskovsoft.smartyoutubetv2.common.app.presenters.settings;

import android.content.Context;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionCategory;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.UiOptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.AppDialogPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.base.BasePresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.service.SidebarService;
import com.liskovsoft.smartyoutubetv2.common.prefs.GeneralData;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerData;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerTweaksData;
import com.liskovsoft.smartyoutubetv2.common.prefs.SearchData;
import com.liskovsoft.smartyoutubetv2.common.utils.AppDialogUtil;
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

  private boolean mRestartApp;
  ;

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
    appendDeveloperCategory(settingsPresenter);

    settingsPresenter.showDialog(
        getContext().getString(R.string.settings_player),
        () -> {
          if (mRestartApp) {
            mRestartApp = false;
            MessageHelpers.showLongMessage(getContext(), R.string.msg_restart_app);
          }
        });
  }

  private void appendVideoSpeedCategory(AppDialogPresenter settingsPresenter) {
    settingsPresenter.appendSingleButton(
        UiOptionItem.from(
            getContext().getString(R.string.video_speed),
            optionItem -> {
              AppDialogPresenter settingsPresenter2 = AppDialogPresenter.instance(getContext());
              settingsPresenter2.appendCategory(
                  AppDialogUtil.createSpeedListCategory(getContext(), null));
              settingsPresenter2.appendCategory(
                  AppDialogUtil.createRememberSpeedCategory(getContext()));
              settingsPresenter2.appendCategory(
                  AppDialogUtil.createSpeedMiscCategory(getContext()));
              settingsPresenter2.showDialog(getContext().getString(R.string.video_speed));
            }));
  }

  private void appendPlayerButtonsCategory(AppDialogPresenter settingsPresenter) {
    List<OptionItem> options = new ArrayList<>();

    for (int[] pair :
        new int[][] {
          {R.string.auto_frame_rate, PlayerTweaksData.PLAYER_BUTTON_AFR},
          {R.string.video_rotate, PlayerTweaksData.PLAYER_BUTTON_VIDEO_ROTATE},
          {R.string.video_flip, PlayerTweaksData.PLAYER_BUTTON_VIDEO_FLIP},
          {R.string.open_chat, PlayerTweaksData.PLAYER_BUTTON_CHAT},
          {R.string.content_block_provider, PlayerTweaksData.PLAYER_BUTTON_CONTENT_BLOCK},
          {R.string.share_link, PlayerTweaksData.PLAYER_BUTTON_SHARE},
          {R.string.action_video_info, PlayerTweaksData.PLAYER_BUTTON_VIDEO_INFO},
          {R.string.action_video_stats, PlayerTweaksData.PLAYER_BUTTON_VIDEO_STATS},
          {R.string.action_playback_queue, PlayerTweaksData.PLAYER_BUTTON_PLAYBACK_QUEUE},
          {R.string.action_channel, PlayerTweaksData.PLAYER_BUTTON_OPEN_CHANNEL},
          {R.string.action_search, PlayerTweaksData.PLAYER_BUTTON_SEARCH},
          {R.string.run_in_background, PlayerTweaksData.PLAYER_BUTTON_PIP},
          {R.string.action_video_speed, PlayerTweaksData.PLAYER_BUTTON_VIDEO_SPEED},
          {R.string.action_subtitles, PlayerTweaksData.PLAYER_BUTTON_SUBTITLES},
          {R.string.action_subscribe, PlayerTweaksData.PLAYER_BUTTON_SUBSCRIBE},
          {R.string.action_like, PlayerTweaksData.PLAYER_BUTTON_LIKE},
          {R.string.action_dislike, PlayerTweaksData.PLAYER_BUTTON_DISLIKE},
          {R.string.action_playlist_add, PlayerTweaksData.PLAYER_BUTTON_ADD_TO_PLAYLIST},
          {R.string.action_play_pause, PlayerTweaksData.PLAYER_BUTTON_PLAY_PAUSE},
          {R.string.action_repeat_mode, PlayerTweaksData.PLAYER_BUTTON_REPEAT_MODE},
          {R.string.action_next, PlayerTweaksData.PLAYER_BUTTON_NEXT},
          {R.string.action_previous, PlayerTweaksData.PLAYER_BUTTON_PREVIOUS},
          {R.string.playback_settings, PlayerTweaksData.PLAYER_BUTTON_HIGH_QUALITY}
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
              mPlayerTweaksData.isPlayerButtonEnabled(pair[1])));
    }

    settingsPresenter.appendCheckedCategory(
        getContext().getString(R.string.player_buttons), options);
  }

  private void appendDeveloperCategory(AppDialogPresenter settingsPresenter) {
    List<OptionItem> options = new ArrayList<>();

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.audio_sync_fix),
            getContext().getString(R.string.audio_sync_fix_desc),
            option -> mPlayerTweaksData.setAudioSyncFixEnabled(option.isSelected()),
            mPlayerTweaksData.isAudioSyncFixEnabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.ambilight_ratio_fix),
            getContext().getString(R.string.ambilight_ratio_fix_desc),
            option -> {
              mPlayerTweaksData.setTextureViewEnabled(option.isSelected());
              if (option.isSelected()) {
                // Tunneled playback works only with SurfaceView
                mPlayerTweaksData.setTunneledPlaybackEnabled(false);
              }
            },
            mPlayerTweaksData.isTextureViewEnabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.disable_stream_buffer),
            getContext().getString(R.string.disable_stream_buffer_desc),
            option -> mPlayerTweaksData.setBufferOnStreamsDisabled(option.isSelected()),
            mPlayerTweaksData.isBufferOnStreamsDisabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.tunneled_video_playback),
            getContext().getString(R.string.tunneled_video_playback_desc),
            option -> {
              mPlayerTweaksData.setTunneledPlaybackEnabled(option.isSelected());
              if (option.isSelected()) {
                // Tunneled playback works only with SurfaceView
                mPlayerTweaksData.setTextureViewEnabled(false);
              }
            },
            mPlayerTweaksData.isTunneledPlaybackEnabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.skip_codec_profile_check),
            getContext().getString(R.string.skip_codec_profile_check_desc),
            option -> mPlayerTweaksData.setProfileLevelCheckSkipped(option.isSelected()),
            mPlayerTweaksData.isProfileLevelCheckSkipped()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.force_sw_codec),
            getContext().getString(R.string.force_sw_codec_desc),
            option -> mPlayerTweaksData.setSWDecoderForced(option.isSelected()),
            mPlayerTweaksData.isSWDecoderForced()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.keep_finished_activities),
            option -> mPlayerTweaksData.setKeepFinishedActivityEnabled(option.isSelected()),
            mPlayerTweaksData.isKeepFinishedActivityEnabled()));

    settingsPresenter.appendCheckedCategory(
        getContext().getString(R.string.player_tweaks), options);
  }

  private void appendMiscCategory(AppDialogPresenter settingsPresenter) {
    List<OptionItem> options = new ArrayList<>();

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.suggestions_horizontally_scrolled),
            option -> mPlayerTweaksData.setSuggestionsHorizontallyScrolled(option.isSelected()),
            mPlayerTweaksData.isSuggestionsHorizontallyScrolled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.player_auto_volume),
            option -> mPlayerTweaksData.setPlayerAutoVolumeEnabled(option.isSelected()),
            mPlayerTweaksData.isPlayerAutoVolumeEnabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.player_section_playlist),
            option -> mPlayerTweaksData.setSectionPlaylistEnabled(option.isSelected()),
            mPlayerTweaksData.isSectionPlaylistEnabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.player_chapter_notification2),
            option -> mPlayerTweaksData.setChapterNotificationEnabled(option.isSelected()),
            mPlayerTweaksData.isChapterNotificationEnabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.search_background_playback),
            option -> mSearchData.setTempBackgroundModeEnabled(option.isSelected()),
            mSearchData.isTempBackgroundModeEnabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.player_loop_shorts),
            option -> mPlayerTweaksData.setLoopShortsEnabled(option.isSelected()),
            mPlayerTweaksData.isLoopShortsEnabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.player_disable_suggestions),
            option -> mPlayerTweaksData.setSuggestionsDisabled(option.isSelected()),
            mPlayerTweaksData.isSuggestionsDisabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.remember_position_of_live_videos),
            option -> mPlayerTweaksData.setRememberPositionOfLiveVideosEnabled(option.isSelected()),
            mPlayerTweaksData.isRememberPositionOfLiveVideosEnabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.player_show_clock),
            option -> mPlayerData.setClockEnabled(option.isSelected()),
            mPlayerData.isClockEnabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.player_show_quality_info),
            option -> mPlayerData.setQualityInfoEnabled(option.isSelected()),
            mPlayerData.isQualityInfoEnabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.player_show_quality_info_bitrate),
            option -> mPlayerTweaksData.setQualityInfoBitrateEnabled(option.isSelected()),
            mPlayerTweaksData.isQualityInfoBitrateEnabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.player_global_focus),
            getContext().getString(R.string.player_global_focus_desc),
            option -> mPlayerTweaksData.setSyncRowButtonIndexEnabled(option.isSelected()),
            mPlayerTweaksData.isSyncRowButtonIndexEnabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.player_likes_count),
            option -> mPlayerTweaksData.setLikesCounterEnabled(option.isSelected()),
            mPlayerTweaksData.isLikesCounterEnabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.player_show_tooltips),
            option -> mPlayerData.setTooltipsEnabled(option.isSelected()),
            mPlayerData.isTooltipsEnabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.player_button_long_click),
            option -> mPlayerTweaksData.setButtonLongClickEnabled(option.isSelected()),
            mPlayerTweaksData.isButtonLongClickEnabled()));

    options.add(
        UiOptionItem.from(
            getContext().getString(R.string.real_channel_icon),
            option -> mPlayerTweaksData.setRealChannelIconEnabled(option.isSelected()),
            mPlayerTweaksData.isRealChannelIconEnabled()));

    settingsPresenter.appendCheckedCategory(getContext().getString(R.string.player_other), options);
  }
}
