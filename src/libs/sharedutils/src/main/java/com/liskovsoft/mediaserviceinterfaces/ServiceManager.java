package com.liskovsoft.sharedutils;

import com.liskovsoft.sharedutils.service.ContentService;

public interface ServiceManager {
    SignInService getSignInService();
    RemoteControlService getRemoteControlService();
    ContentService getContentService();
    MediaItemService getMediaItemService();
    LiveChatService getLiveChatService();
    CommentsService getCommentsService();
    NotificationsService getNotificationsService();
    ChannelGroupService getChannelGroupService();
    void invalidateCache();
    void refreshCacheIfNeeded();
    void applyNoPlaybackFix();
    void applySubtitleFix();
}
