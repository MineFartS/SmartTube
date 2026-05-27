package minefarts.smarttube.utils.actions;

import minefarts.smarttube.utils.channelgroups.ChannelGroupServiceImpl;
import minefarts.smarttube.utils.notifications.NotificationStorage;

public class ActionsServiceWrapper extends ActionsService {
    private static ActionsServiceWrapper sInstance;

    public static ActionsServiceWrapper instance() {
        if (sInstance == null) {
            sInstance = new ActionsServiceWrapper();
        }

        return sInstance;
    }

    @Override
    public void setLike(String videoId) {
        super.setLike(videoId);

        NotificationStorage.setLike(true);
    }

    @Override
    public void removeLike(String videoId) {
        super.removeLike(videoId);

        NotificationStorage.setLike(false);
    }

    @Override
    public void setDislike(String videoId) {
        super.setDislike(videoId);

        NotificationStorage.setLike(false);
    }

    @Override
    public void removeDislike(String videoId) {
        super.removeDislike(videoId);

        NotificationStorage.setLike(true);
    }
}
