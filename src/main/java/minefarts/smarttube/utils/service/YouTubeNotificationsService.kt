package minefarts.smarttube.utils.service

import minefarts.smarttube.utils.NotificationsService
import minefarts.smarttube.utils.service.data.MediaGroup
import minefarts.smarttube.utils.data.MediaItem
import minefarts.smarttube.utils.data.NotificationState
import com.liskovsoft.sharedutils.rx.RxHelper
import minefarts.smarttube.utils.notifications.NotificationsServiceInt
import minefarts.smarttube.utils.notifications.NotificationsServiceIntWrapper
import minefarts.smarttube.utils.SignInService

import io.reactivex.Observable

public object YouTubeNotificationsService: NotificationsService {
    override fun getNotificationItems(): MediaGroup? {
        checkSigned()

        return getNotificationServiceInt().getItems()
    }

    override fun hideNotification(item: MediaItem?) {
        checkSigned()

        getNotificationServiceInt().hideNotification(item)
    }

    override fun setNotificationState(state: NotificationState?) {
        checkSigned()

        getNotificationServiceInt().modifyNotification(state)
    }

    override fun getNotificationItemsObserve(): Observable<MediaGroup> {
        return RxHelper.fromCallable { notificationItems }
    }

    override fun hideNotificationObserve(item: MediaItem?): Observable<Void> {
        return RxHelper.fromRunnable { hideNotification(item) }
    }

    override fun setNotificationStateObserve(state: NotificationState?): Observable<Void> {
        return RxHelper.fromRunnable { setNotificationState(state) }
    }

    private fun checkSigned() {
        getSignInService().checkAuth()
    }

    private fun getSignInService(): SignInService = SignInService.instance()
    private fun getNotificationServiceInt(): NotificationsServiceInt = NotificationsServiceIntWrapper
}