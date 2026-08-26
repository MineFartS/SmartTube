package com.liskovsoft.smartyoutubetv2.tv.ui.main;

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.mediaserviceinterfaces.data.SponsorSegment;
import com.liskovsoft.smartyoutubetv2.common.exoplayer.other.SubtitleManager.SubtitleStyle;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.BrowsePresenter;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerData;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerTweaksData;
import com.liskovsoft.smartyoutubetv2.common.prefs.MainUIData;
import com.liskovsoft.smartyoutubetv2.common.prefs.SponsorBlockData;
import com.liskovsoft.youtubeapi.service.internal.MediaServiceData;

import android.content.Context;

public class TweakSettings {

    private static Context mContext;
    
    public static void tweak(Context context) {

        mContext = context;

        disableSections();
        disablePlayerButtons();
        disableContextMenuOptions();
        disableContentBlock();
        hideContent();
        configSubtitles();
        
    }

    private static void disableSections() {

        BrowsePresenter bp = BrowsePresenter.instance(mContext);
        
        bp.enableSection(MediaGroup.TYPE_MUSIC, false);
        bp.enableSection(MediaGroup.TYPE_NEWS, false);
        bp.enableSection(MediaGroup.TYPE_GAMING, false);
        bp.enableSection(MediaGroup.TYPE_CHANNEL, false);
        bp.enableSection(MediaGroup.TYPE_KIDS_HOME, false);
        bp.enableSection(MediaGroup.TYPE_TRENDING, false);
        bp.enableSection(MediaGroup.TYPE_SHORTS, false);
        bp.enableSection(MediaGroup.TYPE_NOTIFICATIONS, false);
        bp.enableSection(MediaGroup.TYPE_SPORTS, false);
        bp.enableSection(MediaGroup.TYPE_MOVIES, false);
        bp.enableSection(MediaGroup.TYPE_LIVE, false);
        bp.enableSection(MediaGroup.TYPE_MY_VIDEOS, false);
        bp.enableSection(MediaGroup.TYPE_PLAYBACK_QUEUE, false);
        bp.enableSection(MediaGroup.TYPE_BLOCKED_CHANNELS, false);

    }

    private static void disablePlayerButtons() {
        
        PlayerTweaksData PTD = PlayerTweaksData.instance(mContext);

        PTD.setPlayerButtonDisabled(PlayerTweaksData.PLAYER_BUTTON_VIDEO_STATS);
        PTD.setPlayerButtonDisabled(PlayerTweaksData.PLAYER_BUTTON_SCREEN_DIMMING);
        PTD.setPlayerButtonDisabled(PlayerTweaksData.PLAYER_BUTTON_SEARCH);
        PTD.setPlayerButtonDisabled(PlayerTweaksData.PLAYER_BUTTON_PIP);
        PTD.setPlayerButtonDisabled(PlayerTweaksData.PLAYER_BUTTON_HIGH_QUALITY);
        
    }

    private static void disableContextMenuOptions() {

        MainUIData MUID = MainUIData.instance(mContext);
        
        MUID.setMenuItemEnabled(MainUIData.MENU_ITEM_MARK_AS_WATCHED);
        
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_STREAM_REMINDER);
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_CREATE_PLAYLIST);
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_RENAME_PLAYLIST);
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_ADD_TO_NEW_PLAYLIST);
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_BLOCK_CHANNEL);
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_REMOVE_FROM_SUBSCRIPTIONS);
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_PLAYLIST_ORDER);
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_PLAY_NEXT);
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_PIN_TO_SIDEBAR);
        MUID.setMenuItemDisabled(MainUIData.MENU_ITEM_SAVE_REMOVE_PLAYLIST);

        MUID.setMenuItemIndex(0, MainUIData.MENU_ITEM_MARK_AS_WATCHED);
        MUID.setMenuItemIndex(1, MainUIData.MENU_ITEM_NOT_INTERESTED);
        MUID.setMenuItemIndex(2, MainUIData.MENU_ITEM_NOT_RECOMMEND_CHANNEL);
        MUID.setMenuItemIndex(3, MainUIData.MENU_ITEM_OPEN_CHANNEL);

    }

    private static void disableContentBlock() {

        String[] segments = {
            SponsorSegment.CATEGORY_INTRO,
            SponsorSegment.CATEGORY_OUTRO,
            SponsorSegment.CATEGORY_SELF_PROMO,
            SponsorSegment.CATEGORY_INTERACTION,
            SponsorSegment.CATEGORY_MUSIC_OFF_TOPIC,
            SponsorSegment.CATEGORY_PREVIEW_RECAP,
            SponsorSegment.CATEGORY_POI_HIGHLIGHT,
            SponsorSegment.CATEGORY_FILLER,
        };

        SponsorBlockData SBD = SponsorBlockData.instance(mContext);

        for (String segment : segments) {
            SBD.disableColorMarker(segment);
            SBD.setAction(segment, SponsorBlockData.ACTION_DO_NOTHING);
        }

        SBD.setDontSkipSegmentAgainEnabled(true);

    }

    private static void hideContent() {

        MediaServiceData MSD = MediaServiceData.instance();

        int[] content_types = {

            MediaServiceData.CONTENT_WATCHED_HOME,

            MediaServiceData.CONTENT_SHORTS_SUBSCRIPTIONS,
            MediaServiceData.CONTENT_SHORTS_SEARCH,
            MediaServiceData.CONTENT_SHORTS_HOME,
            MediaServiceData.CONTENT_SHORTS_CHANNEL,
            MediaServiceData.CONTENT_SHORTS_HISTORY,
            MediaServiceData.CONTENT_SHORTS_TRENDING,

            MediaServiceData.CONTENT_UPCOMING_SUBSCRIPTIONS,
            MediaServiceData.CONTENT_UPCOMING_HOME,
            MediaServiceData.CONTENT_UPCOMING_CHANNEL,

        };

        for (int content : content_types) {
            MSD.setContentHidden(content, true);
        }

    }

    private static void configSubtitles() {

        PlayerData PD = PlayerData.instance(mContext);

        PD.setSubtitleScale(.7f);

        SubtitleStyle white_semi_trans = PD.getSubtitleStyles().get(1);
        PD.setSubtitleStyle(white_semi_trans);

    }

}
