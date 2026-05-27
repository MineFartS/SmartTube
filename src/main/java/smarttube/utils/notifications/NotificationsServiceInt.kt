package minefarts.smarttube.utils.notifications

import minefarts.smarttube.utils.service.data.MediaGroup
import minefarts.smarttube.utils.data.MediaItem
import minefarts.smarttube.utils.data.NotificationState
import minefarts.smarttube.utils.common.models.impl.mediagroup.NotificationsMediaGroup
import minefarts.smarttube.google.common.helpers.RetrofitHelper
import minefarts.smarttube.utils.common.models.impl.NotificationStateImpl
import minefarts.smarttube.utils.common.models.impl.mediaitem.NotificationMediaItem

internal open class NotificationsServiceInt {
    private val mService: NotificationsApi = RetrofitHelper.create(NotificationsApi::class.java)

    open fun getItems(): MediaGroup? {
        val result = mService.getNotifications(NotificationsApiHelper.getNotificationsQuery())

        return RetrofitHelper.getWithErrors(result)?.let { NotificationsMediaGroup(it) }
    }

    open fun hideNotification(item: MediaItem?) {
        if (item is NotificationMediaItem) {
            hideNotification(item.hideNotificationToken)
        }
    }

    open fun modifyNotification(notificationState: NotificationState?) {
        if (notificationState is NotificationStateImpl) {

            notificationState.setSelected()

            modifyNotification(notificationState.stateParams)
        }
    }

    private fun hideNotification(hideNotificationToken: String?) {
        if (hideNotificationToken == null) {
            return
        }

        val result = mService.getHideNotification(NotificationsApiHelper.getHideNotificationQuery(hideNotificationToken))

        RetrofitHelper.get(result)
    }

    private fun modifyNotification(modifyNotificationParams: String?) {
        if (modifyNotificationParams == null) {
            return
        }

        val result = mService.getModifyNotification(NotificationsApiHelper.getModifyNotificationQuery(modifyNotificationParams))

        RetrofitHelper.getWithErrors(result)
    }
}