package minefarts.sharedutils;

import minefarts.sharedutils.service.data.MediaGroup;
import minefarts.sharedutils.data.MediaItem;
import minefarts.sharedutils.data.NotificationState;
import io.reactivex.Observable;

public interface NotificationsService {
    MediaGroup getNotificationItems();
    void hideNotification(MediaItem item);
    void setNotificationState(NotificationState state);

    Observable<MediaGroup> getNotificationItemsObserve();
    Observable<Void> hideNotificationObserve(MediaItem item);
    Observable<Void> setNotificationStateObserve(NotificationState state);
}
