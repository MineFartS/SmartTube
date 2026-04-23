package com.liskovsoft.sharedutils;

import com.liskovsoft.sharedutils.data.MediaGroup;
import com.liskovsoft.sharedutils.data.MediaItem;
import com.liskovsoft.sharedutils.data.NotificationState;
import io.reactivex.Observable;

public interface NotificationsService {
    MediaGroup getNotificationItems();
    void hideNotification(MediaItem item);
    void setNotificationState(NotificationState state);

    Observable<MediaGroup> getNotificationItemsObserve();
    Observable<Void> hideNotificationObserve(MediaItem item);
    Observable<Void> setNotificationStateObserve(NotificationState state);
}
