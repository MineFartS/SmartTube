package minefarts.smarttube.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.liskovsoft.sharedutils.MediaItemService;
import com.liskovsoft.sharedutils.data.ItemGroup;
import com.liskovsoft.sharedutils.data.MediaItem;
import com.liskovsoft.sharedutils.data.PlaylistInfo;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.helpers.PermissionHelpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.Queue;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.playback.PlayerEngine;
import minefarts.smarttube.app.models.playback.PlayerEngine;
import minefarts.smarttube.app.models.playback.ui.OptionCategory;
import minefarts.smarttube.app.models.playback.ui.OptionItem;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.app.presenters.BrowsePresenter;
import minefarts.smarttube.app.presenters.PlaybackPresenter;
import minefarts.smarttube.app.presenters.dialogs.menu.VideoMenuPresenter.VideoMenuCallback;
import minefarts.smarttube.app.presenters.dialogs.menu.providers.channelgroup.ChannelGroupServiceWrapper;
import minefarts.smarttube.app.views.ViewManager;
import minefarts.smarttube.exoplayer.selector.ExoFormatItem;
import minefarts.smarttube.exoplayer.selector.FormatItem;
import minefarts.smarttube.exoplayer.selector.FormatItem.VideoPreset;
import minefarts.smarttube.exoplayer.selector.TrackSelectorManager;
import minefarts.smarttube.exoplayer.selector.track.MediaTrack;
import minefarts.smarttube.misc.AppDataSourceManager;
import minefarts.smarttube.misc.ServiceManager;
import minefarts.smarttube.misc.MotherActivity;
import minefarts.smarttube.prefs.ContentBlockData;
import minefarts.smarttube.prefs.GeneralData;
import minefarts.smarttube.prefs.PlayerData;
import minefarts.smarttube.prefs.PlayerTweaksData;
import com.liskovsoft.sharedutils.playlist.impl.YouTubePlaylistInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class AppDialogUtil {
    
    private static final String TAG = AppDialogUtil.class.getSimpleName();
    
    private static final int VIDEO_PRESETS_ID = 136;
    private static final int AUDIO_LANGUAGE_ID = 138;
    private static final int PLAYER_SPEED_LIST_ID = 141;
    private static final int PLAYER_REMEMBER_SPEED_ID = 142;
    private static final int PLAYER_SPEED_MISC_ID = 143;
    private static final int PLAYER_REPEAT_ID = 146;

    /**
     * Adds share link items to existing dialog.
     */
    public static void appendShareLinkDialogItem(Context context, AppDialogPresenter dialogPresenter, Video video) {
        appendShareLinkDialogItem(context, dialogPresenter, video, -1);
    }

    /**
     * Adds share link items to existing dialog.
     */
    public static void appendShareLinkDialogItem(Context context, AppDialogPresenter dialogPresenter, Video video, int positionSec) {
        if (video == null) {
            return;
        }

        if (video.videoId == null && video.channelId == null) {
            return;
        }

        dialogPresenter.appendSingleButton(
                UiOptionItem.from(context.getString(R.string.share_link), optionItem -> {
                    if (video.videoId != null) {
                        Utils.displayShareVideoDialog(context, video.videoId, positionSec == -1 ? Utils.toSec(video.getPositionMs()) : positionSec);
                    } else if (video.channelId != null) {
                        Utils.displayShareChannelDialog(context, video.channelId);
                    }
                }));
    }

    /**
     * Adds share link items to existing dialog.
     */
    public static void appendShareEmbedLinkDialogItem(Context context, AppDialogPresenter dialogPresenter, Video video) {
        appendShareEmbedLinkDialogItem(context, dialogPresenter, video, -1);
    }

    /**
     * Adds share link items to existing dialog.
     */
    public static void appendShareEmbedLinkDialogItem(Context context, AppDialogPresenter dialogPresenter, Video video, int positionSec) {
        if (video == null) {
            return;
        }

        if (video.videoId == null) {
            return;
        }

        dialogPresenter.appendSingleButton(
                UiOptionItem.from(context.getString(R.string.share_embed_link), optionItem -> {
                    if (video.videoId != null) {
                        Utils.displayShareEmbedVideoDialog(context, video.videoId, positionSec == -1 ? Utils.toSec(video.getPositionMs()) : positionSec);
                    }
                }));
    }

    /**
     * Adds QR code item to existing dialog.
     */
    public static void appendShareQRLinkDialogItem(Context context, AppDialogPresenter dialogPresenter, Video video) {
        appendShareQRLinkDialogItem(context, dialogPresenter, video, -1);
    }

    /**
     * Adds QR code item to existing dialog.
     */
    public static void appendShareQRLinkDialogItem(Context context, AppDialogPresenter dialogPresenter, Video video, int positionSec) {
        if (video == null) {
            return;
        }

        if (video.videoId == null) {
            return;
        }

        dialogPresenter.appendSingleButton(
                UiOptionItem.from(context.getString(R.string.share_qr_link), optionItem -> {
                    dialogPresenter.closeDialog(); // pause bg video
                    if (video.videoId != null) {
                        Utils.openLink(context, Utils.toQrCodeLink(
                                Utils.convertToFullVideoUrl(video.videoId, positionSec == -1 ? Utils.toSec(video.getPositionMs()) : positionSec).toString()));
                    }
                }));
    }

    public static OptionCategory createVideoPresetsCategory(Context context) {
        return createVideoPresetsCategory(context, () -> {});
    }

    public static OptionCategory createVideoPresetsCategory(Context context, Runnable onFormatSelected) {
        return OptionCategory.from(
                VIDEO_PRESETS_ID,
                OptionCategory.TYPE_RADIO_LIST,
                context.getString(R.string.title_video_presets),
                fromPresets(
                        context,
                        AppDataSourceManager.instance().getVideoPresets(),
                        onFormatSelected
                )
        );
    }

    private static List<OptionItem> fromPresets(Context context, VideoPreset[] presets, Runnable onFormatSelected) {
        List<OptionItem> result = new ArrayList<>();

        PlayerData playerData = PlayerData.instance(context);
        PlayerTweaksData playerTweaksData = PlayerTweaksData.instance(context);
        FormatItem selectedFormat = playerData.getFormat(FormatItem.TYPE_VIDEO);
        boolean isPresetSelection = selectedFormat != null && selectedFormat.isPreset();

        for (VideoPreset preset : presets) {
            if (!Utils.isPresetSupported(preset)) {
                continue;
            }

            result.add(0, UiOptionItem.from(preset.name,
                    option -> setFormat(preset.format, playerData, onFormatSelected),
                    isPresetSelection && preset.format.equals(selectedFormat)));
        }

        FormatItem noVideo = ExoFormatItem.from(MediaTrack.forRendererIndex(TrackSelectorManager.RENDERER_INDEX_VIDEO), true);
        result.add(0, UiOptionItem.from(
                context.getString(R.string.video_disabled),
                optionItem ->
                        setFormat(noVideo, playerData, onFormatSelected),
                isPresetSelection && Helpers.equals(noVideo, selectedFormat)));

        result.add(0, UiOptionItem.from(
                context.getString(R.string.video_preset_disabled),
                optionItem -> setFormat(playerData.getDefaultVideoFormat(), playerData, onFormatSelected),
                !isPresetSelection));

        return result;
    }

    private static void setFormat(FormatItem formatItem, PlayerData playerData, Runnable onFormatSelected) {
        
        playerData.setFormat(formatItem);
    
        onFormatSelected.run();
    
    }

    public static OptionCategory createAudioLanguageCategory(Context context) {
        return createAudioLanguageCategory(context, () -> {});
    }

    public static OptionCategory createAudioLanguageCategory(Context context, Runnable onSetCallback) {
        PlayerData playerData = PlayerData.instance(context);
        String title = context.getString(R.string.audio_language);

        List<OptionItem> options = new ArrayList<>();

        List<String> addedCodes = new ArrayList<>();
        List<String> lastLanguages = playerData.getLastAudioLanguages();

        for (Locale locale : Locale.getAvailableLocales()) {
            String languageCode = locale.getLanguage().toLowerCase();

            if (addedCodes.contains(languageCode) || lastLanguages.contains(languageCode)) {
                continue;
            }

            options.add(UiOptionItem.from(locale.getDisplayLanguage(),
                    optionItem -> {
                        playerData.setAudioLanguage(languageCode);
                        onSetCallback.run();
                    },
                    languageCode.equals(playerData.getAudioLanguage())));
            addedCodes.add(languageCode);
        }

        // NOTE: Comparator.comparing API >= 24
        // Alphabetical order
        Collections.sort(options, (o1, o2) -> ((String) o1.getTitle()).compareTo((String) o2.getTitle()));

        int idx = 0;

        for (String languageCode : lastLanguages) {
            if (TextUtils.isEmpty(languageCode)) { // original
                continue;
            }

            Locale locale = new Locale(languageCode);

            options.add(idx++, UiOptionItem.from(locale.getDisplayLanguage(),
                    optionItem -> {
                        playerData.setAudioLanguage(languageCode);
                        onSetCallback.run();
                    },
                    languageCode.equals(playerData.getAudioLanguage())));
        }

        options.add(0, UiOptionItem.from(
            "Original",
            optionItem -> {
                playerData.setAudioLanguage("");
                onSetCallback.run();
            },
            "".equals(playerData.getAudioLanguage())
        ));

        return OptionCategory.from(
            AUDIO_LANGUAGE_ID, 
            OptionCategory.TYPE_RADIO_LIST, 
            title, 
            options
        );
        
    }

    public static OptionItem createExcludeFromContentBlockButton(
        Context context,
        Video video, 
        Runnable onClose
    ) {
        return UiOptionItem.from(
                context.getString(
                        ContentBlockData.instance(context).isChannelExcluded(video.channelId) ?
                                R.string.content_block_stop_excluding_channel :
                                R.string.content_block_exclude_channel),
                optionItem -> {
                    if (video.hasChannel()) {
                        ContentBlockData.instance(context).toggleExcludeChannel(video.channelId);
                        if (onClose != null) {
                            onClose.run();
                        }
                    } else {
                        MessageHelpers.showMessage(context, R.string.wait_data_loading);

                        ServiceManager.loadMetadata(
                                video,
                                metadata -> {
                                    video.sync(metadata);
                                    ContentBlockData.instance(context).excludeChannel(video.channelId);
                                    if (onClose != null) {
                                        onClose.run();
                                    }
                                }
                        );
                    }
                });
    }

    public static OptionCategory createSpeedListCategory(Context context, PlayerEngine playbackController) {
        PlayerData playerData = PlayerData.instance(context);
        List<OptionItem> items = new ArrayList<>();

        PlayerTweaksData data = PlayerTweaksData.instance(context);
        for (float speed : data.isLongSpeedListEnabled() ? Utils.SPEED_LIST_LONG :
                data.isExtraLongSpeedListEnabled() ? Utils.SPEED_LIST_EXTRA_LONG : Utils.SPEED_LIST_SHORT) {
            items.add(UiOptionItem.from(
                    String.valueOf(speed),
                    optionItem -> {
                        if (playbackController != null) {
                            //playerData.setSpeed(playbackController.getVideo().channelId, speed);
                            playbackController.setSpeed(speed);
                        } else {
                            playerData.setSpeed(speed);
                        }
                    },
                    (playbackController != null ? playbackController.getSpeed() : playerData.getSpeed()) == speed));
        }

        return OptionCategory.from(PLAYER_SPEED_LIST_ID, OptionCategory.TYPE_RADIO_LIST, context.getString(R.string.video_speed), items);
    }

    public static OptionCategory createRememberSpeedCategory(Context context) {
        PlayerData playerData = PlayerData.instance(context);
        List<OptionItem> options = new ArrayList<>();

        options.add(UiOptionItem.from(context.getString(R.string.player_remember_speed_none),
                optionItem -> {
                    playerData.setAllSpeedEnabled(false);
                    playerData.setSpeedPerVideoEnabled(false);
                    playerData.setSpeedPerChannelEnabled(false);
                },
                !playerData.isAllSpeedEnabled() && !playerData.isSpeedPerVideoEnabled()));

        options.add(UiOptionItem.from(context.getString(R.string.player_remember_speed_all),
                optionItem -> playerData.setAllSpeedEnabled(true),
                playerData.isAllSpeedEnabled()));

        options.add(UiOptionItem.from(context.getString(R.string.player_remember_speed_each),
                optionItem -> playerData.setSpeedPerVideoEnabled(true),
                playerData.isSpeedPerVideoEnabled()));

        options.add(UiOptionItem.from(context.getString(R.string.player_speed_per_channel),
                option -> playerData.setSpeedPerChannelEnabled(option.isSelected()),
                playerData.isSpeedPerChannelEnabled()));

        String title = context.getString(R.string.player_remember_speed);

        return OptionCategory.from(PLAYER_REMEMBER_SPEED_ID, OptionCategory.TYPE_RADIO_LIST, title, options);
    }

    public static OptionCategory createSpeedMiscCategory(Context context) {
        PlayerTweaksData playerTweaksData = PlayerTweaksData.instance(context);
        List<OptionItem> options = new ArrayList<>();

        options.add(UiOptionItem.from(context.getString(R.string.player_long_speed_list),
                option -> playerTweaksData.setLongSpeedListEnabled(option.isSelected()),
                playerTweaksData.isLongSpeedListEnabled()));

        options.add(UiOptionItem.from(context.getString(R.string.player_extra_long_speed_list),
                option -> playerTweaksData.setExtraLongSpeedListEnabled(option.isSelected()),
                playerTweaksData.isExtraLongSpeedListEnabled()));

        String title = context.getString(R.string.player_other);

        return OptionCategory.from(PLAYER_SPEED_MISC_ID, OptionCategory.TYPE_CHECKBOX_LIST, title, options);
    }

    public static OptionCategory createPlaybackModeCategory(Context context) {
        return createPlaybackModeCategory(context, () -> {});
    }

    public static OptionCategory createPlaybackModeCategory(Context context, Runnable onModeSelected) {
        PlayerData playerData = PlayerData.instance(context);
        List<OptionItem> options = new ArrayList<>();

        for (int[] pair : new int[][] {
                {R.string.repeat_mode_all, PlayerEngine.PLAYBACK_MODE_ALL},
                {R.string.repeat_mode_one, PlayerEngine.PLAYBACK_MODE_ONE},
                {R.string.repeat_mode_shuffle, PlayerEngine.PLAYBACK_MODE_SHUFFLE},
                {R.string.repeat_mode_pause_alt, PlayerEngine.PLAYBACK_MODE_LIST},
                {R.string.repeat_mode_reverse_list, PlayerEngine.PLAYBACK_MODE_REVERSE_LIST},
                {R.string.repeat_mode_pause, PlayerEngine.PLAYBACK_MODE_PAUSE},
                {R.string.repeat_mode_none, PlayerEngine.PLAYBACK_MODE_CLOSE}
        }) {
            options.add(UiOptionItem.from(context.getString(pair[0]),
                    optionItem -> {
                        playerData.setPlaybackMode(pair[1]);
                        onModeSelected.run();
                    },
                    playerData.getPlaybackMode() == pair[1]
            ));
        }

        return OptionCategory.from(
                PLAYER_REPEAT_ID,
                OptionCategory.TYPE_RADIO_LIST,
                context.getString(R.string.action_repeat_mode),
                options
        );
    }

    public static void showConfirmationDialog(Context context, String title, Runnable onConfirm) {
        showConfirmationDialog(context, title, onConfirm, () -> {});
    }

    public static void showConfirmationDialog(Context context, String title, Runnable onConfirm, Runnable onCancel) {
        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(context);

        List<OptionItem> options = new ArrayList<>();

        options.add(UiOptionItem.from(
            "Cancel",
            option -> {
                settingsPresenter.goBack();
                onCancel.run();
            }
        ));

        options.add(UiOptionItem.from(context.getString(R.string.btn_confirm),
                option -> {
                    settingsPresenter.goBack();
                    onConfirm.run();
                }));

        settingsPresenter.appendStringsCategory(title, options);

        settingsPresenter.showDialog(title);
    }

    public static void showAddToPlaylistDialog(Context context, Video video, VideoMenuCallback callback) {

        if (video == null) {
            return;
        }

        MediaItemService itemManager = MediaItemService.instance();

        Disposable playlistsInfoAction = itemManager.getPlaylistsInfoObserve(video.videoId)
                .subscribe(
                        videoPlaylistInfos -> showAddToPlaylistDialog(context, video, callback, videoPlaylistInfos, null),
                        error -> {
                            // Fallback to something on error
                            Log.e(TAG, "Get playlists error: %s", error.getMessage());
                        }
                );
    }

    public static void showAddToPlaylistDialog(Context context, Video video, VideoMenuCallback callback, List<PlaylistInfo> playlistInfos, Runnable onFinish) {
        if (playlistInfos == null) {
            MessageHelpers.showMessage(context, R.string.msg_signed_users_only);
            return;
        }

        AppDialogPresenter dialogPresenter = AppDialogPresenter.instance(context);

        appendPlaylistDialogContent(context, video, callback, dialogPresenter, playlistInfos);
        dialogPresenter.showDialog(context.getString(R.string.dialog_add_to_playlist), onFinish);
    }

    private static void appendPlaylistDialogContent(
            Context context, Video video, VideoMenuCallback callback, AppDialogPresenter dialogPresenter, List<PlaylistInfo> playlistInfos) {
        List<OptionItem> options = new ArrayList<>();

        for (PlaylistInfo playlistInfo : playlistInfos) {
            options.add(UiOptionItem.from(
                    playlistInfo.getTitle(),
                    (item) -> {
                        if (playlistInfo instanceof YouTubePlaylistInfo) {
                            ((YouTubePlaylistInfo) playlistInfo).setSelected(item.isSelected());
                        }
                        addRemoveFromPlaylist(context, video, callback, playlistInfo.getPlaylistId(), item.isSelected());
                        GeneralData.instance(context).setLastPlaylistId(playlistInfo.getPlaylistId());
                        GeneralData.instance(context).setLastPlaylistTitle(playlistInfo.getTitle());
                    },
                    playlistInfo.isSelected()));
        }

        dialogPresenter.appendCheckedCategory(context.getString(R.string.dialog_add_to_playlist), options);
    }

    private static void addRemoveFromPlaylist(Context context, Video video, VideoMenuCallback callback, String playlistId, boolean add) {
        if (video == null) {
            return;
        }

        Observable<Void> editObserve;
        MediaItemService itemManager = MediaItemService.instance();

        if (add) {
            editObserve = video.mediaItem != null ?
                    itemManager.addToPlaylistObserve(playlistId, video.mediaItem) : itemManager.addToPlaylistObserve(playlistId, video.videoId);
        } else {
            // Check that the current video belongs to the right section
            if (callback != null && Helpers.equals(video.playlistId, playlistId)) {
                callback.onItemAction(video, VideoMenuCallback.ACTION_REMOVE_FROM_PLAYLIST);
            }
            editObserve = itemManager.removeFromPlaylistObserve(playlistId, video.videoId);
        }

        // Handle error: Maximum playlist size exceeded (> 5000 items)
        RxHelper.execute(editObserve, error -> MessageHelpers.showLongMessage(context, error.getMessage()));
    }

    public static void showPlaylistOrderDialog(Context context, Video video, Runnable onClose) {
        if (video == null) {
            return;
        }

        if (video.hasPlaylist()) {
            showPlaylistOrderDialog(context, video.playlistId, onClose);
        } else if (video.belongsToUserPlaylists()) {
            ServiceManager.loadChannelUploads(video, group -> {
                if (group.getMediaItems() == null || group.getMediaItems().isEmpty()) {
                    return;
                }

                MediaItem first = group.getMediaItems().get(0);
                String playlistId = first.getPlaylistId();

                showPlaylistOrderDialog(context, playlistId, onClose);
            });
        }
    }

    public static void showPlaylistOrderDialog(Context context, String playlistId, Runnable onClose) {
        AppDialogPresenter dialogPresenter = AppDialogPresenter.instance(context);

        GeneralData generalData = GeneralData.instance(context);

        List<OptionItem> options = new ArrayList<>();

        for (int[] pair : new int[][] {
                {R.string.playlist_order_added_date_newer_first, MediaItemService.PLAYLIST_ORDER_ADDED_DATE_NEWER_FIRST},
                {R.string.playlist_order_added_date_older_first, MediaItemService.PLAYLIST_ORDER_ADDED_DATE_OLDER_FIRST},
                {R.string.playlist_order_popularity, MediaItemService.PLAYLIST_ORDER_POPULARITY},
                {R.string.playlist_order_published_date_newer_first, MediaItemService.PLAYLIST_ORDER_PUBLISHED_DATE_NEWER_FIRST},
                {R.string.playlist_order_published_date_older_first, MediaItemService.PLAYLIST_ORDER_PUBLISHED_DATE_OLDER_FIRST}
        }) {
            options.add(UiOptionItem.from(context.getString(pair[0]), optionItem -> {
                if (optionItem.isSelected()) {
                    RxHelper.execute(
                            MediaItemService.instance().setPlaylistOrderObserve(playlistId, pair[1]),
                            (error) -> MessageHelpers.showMessage(context, R.string.owned_playlist_warning),
                            () -> {
                                generalData.setPlaylistOrder(playlistId, pair[1]);
                                ViewManager.instance(context).refreshCurrentView();
                                if (onClose != null) {
                                    dialogPresenter.closeDialog();
                                    onClose.run();
                                }
                                MessageHelpers.showMessage(context, R.string.msg_done);
                            }
                    );
                }
            }, generalData.getPlaylistOrder(playlistId) == pair[1]));
        }

        dialogPresenter.appendRadioCategory(context.getString(R.string.playlist_order), options);

        dialogPresenter.showDialog(context.getString(R.string.playlist_order));
    }

    public interface OnVideoClick {
        void onClick(Video item);
    }

}
