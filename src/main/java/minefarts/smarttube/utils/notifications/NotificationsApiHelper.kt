package minefarts.smarttube.utils.notifications

import com.liskovsoft.youtubeapi.common.helpers.PostDataHelper

public object NotificationsApiHelper {
    fun getNotificationsQuery(): String {
        return PostDataHelper.createQueryTV("\"notificationsMenuRequestType\":\"NOTIFICATIONS_MENU_REQUEST_TYPE_INBOX\"")
    }

    fun getHideNotificationQuery(hideNotificationToken: String): String {
        return PostDataHelper.createQueryTV("\"serializedRecordNotificationInteractionsRequest\":\"$hideNotificationToken\"")
    }

    fun getModifyNotificationQuery(modifyNotificationParams: String): String {
        return PostDataHelper.createQueryTV("\"params\":\"$modifyNotificationParams\"")
    }
}