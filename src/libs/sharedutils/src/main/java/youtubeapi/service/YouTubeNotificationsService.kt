package minefarts.sharedutils.service

import minefarts.sharedutils.NotificationsService
import minefarts.sharedutils.service.data.MediaGroup
import minefarts.sharedutils.data.MediaItem
import minefarts.sharedutils.data.NotificationState
import minefarts.sharedutils.rx.RxHelper
import minefarts.sharedutils.notifications.NotificationsServiceInt
import minefarts.sharedutils.notifications.NotificationsServiceIntWrapper
import minefarts.sharedutils.SignInService

import io.reactivex.Observable

internal object YouTubeNotificationsService: NotificationsService {
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