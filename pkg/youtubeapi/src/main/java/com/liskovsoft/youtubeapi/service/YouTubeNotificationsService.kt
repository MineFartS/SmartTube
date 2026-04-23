package com.liskovsoft.youtubeapi.service

import com.liskovsoft.youtubeapi.NotificationsService
import com.liskovsoft.youtubeapi.data.MediaGroup
import com.liskovsoft.youtubeapi.data.MediaItem
import com.liskovsoft.youtubeapi.data.NotificationState
import com.liskovsoft.sharedutils.rx.RxHelper
import com.liskovsoft.youtubeapi.notifications.NotificationsServiceInt
import com.liskovsoft.youtubeapi.notifications.NotificationsServiceIntWrapper
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
        getYouTubeSignInService().checkAuth()
    }

    private fun getYouTubeSignInService(): YouTubeSignInService = YouTubeSignInService.instance()
    private fun getNotificationServiceInt(): NotificationsServiceInt = NotificationsServiceIntWrapper
}