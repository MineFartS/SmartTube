package minefarts.smarttube.utils.notifications.gen

import com.liskovsoft.youtubeapi.common.models.gen.getNotificationToken
import com.liskovsoft.youtubeapi.common.models.gen.getText

public fun NotificationsResult.getItems(): List<NotificationItem?>? =
    actions?.firstOrNull()?.openPopupAction?.popup?.multiPageMenuRenderer?.sections?.firstOrNull()?.multiPageMenuNotificationSectionRenderer?.items

public fun NotificationItem.getVideoId() = notificationRenderer?.navigationEndpoint?.watchEndpoint?.videoId
public fun NotificationItem.getThumbnails() = notificationRenderer?.videoThumbnail
public fun NotificationItem.getMessage() = notificationRenderer?.shortMessage?.getText()
public fun NotificationItem.getPublishedTime() = notificationRenderer?.sentTimeText?.getText()
public fun NotificationItem.getNotificationToken() = notificationRenderer?.contextualMenu?.getNotificationToken()
public fun NotificationItem.getTitle() = getMessage()?.split(":", limit = 2)?.getOrNull(1)?.trim() ?: getMessage()
public fun NotificationItem.getUserName() = getMessage()?.split(":", limit = 2)?.getOrNull(0)