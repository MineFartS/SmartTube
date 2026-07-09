package minefarts.smarttube.utils.notifications

import minefarts.smarttube.utils.service.data.MediaGroup
import minefarts.smarttube.utils.data.NotificationState
import minefarts.smarttube.utils.common.models.gen.NotificationStateItem
import minefarts.smarttube.utils.common.models.impl.NotificationStateImpl
import minefarts.smarttube.utils.rss.RssService

private const val ALL = 0 // Enable notifications
private const val PERSONALIZED = 1 // Disable notifications
private const val NONE = 2 // Disable notifications

public object NotificationsServiceIntWrapper: NotificationsServiceInt() {
    override fun getItems(): MediaGroup? {
        return try {
            super.getItems()
        } catch (e: IllegalStateException) {
            NotificationStorage.getChannels()?.let { RssService.getFeed(*it.toTypedArray(), type = MediaGroup.TYPE_NOTIFICATIONS) }
        }
    }

    override fun modifyNotification(notificationState: NotificationState?) {
        if (notificationState is NotificationStateImpl) {
            if (notificationState.index == ALL)
                NotificationStorage.addChannel(notificationState.channelId)
            else
                NotificationStorage.removeChannel(notificationState.channelId)
        }

        try {
            super.modifyNotification(notificationState)
        } catch (e: IllegalStateException) {
            // Notification cannot be modified
        }
    }
}

public class NotificationStateImplWrapper(
    notificationStateItem: NotificationStateItem,
    selectedSateId: Int?,
    channelId: String?,
    params: String?,
    isSubscribed: Boolean
): NotificationStateImpl(notificationStateItem, selectedSateId, channelId, params, isSubscribed) {
    override fun isSelected(): Boolean {
        return if (NotificationStorage.contains(channelId))
             if (index == ALL) true else false
        else if (super.isSelected() && index == ALL) {
            // Set to none if selected globally (global notifications doesn't work)
            allStates.getOrNull(NONE)?.setSelected()
            false
        }
        else super.isSelected()
    }
}