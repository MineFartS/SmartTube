package minefarts.sharedutils.notifications

import minefarts.sharedutils.service.data.MediaGroup
import minefarts.sharedutils.data.MediaItem
import minefarts.sharedutils.data.NotificationState
import minefarts.sharedutils.common.models.impl.mediagroup.NotificationsMediaGroup
import minefarts.googlecommon.common.helpers.RetrofitHelper
import minefarts.sharedutils.common.models.impl.NotificationStateImpl
import minefarts.sharedutils.common.models.impl.mediaitem.NotificationMediaItem

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