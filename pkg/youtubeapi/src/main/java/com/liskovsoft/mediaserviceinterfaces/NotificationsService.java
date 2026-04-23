package com.liskovsoft.youtubeapi;

import com.liskovsoft.youtubeapi.data.MediaGroup;
import com.liskovsoft.youtubeapi.data.MediaItem;
import com.liskovsoft.youtubeapi.data.NotificationState;
import io.reactivex.Observable;

public interface NotificationsService {
    MediaGroup getNotificationItems();
    void hideNotification(MediaItem item);
    void setNotificationState(NotificationState state);

    Observable<MediaGroup> getNotificationItemsObserve();
    Observable<Void> hideNotificationObserve(MediaItem item);
    Observable<Void> setNotificationStateObserve(NotificationState state);
}
