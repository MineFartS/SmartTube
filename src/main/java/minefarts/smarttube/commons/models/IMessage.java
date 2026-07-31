package minefarts.smarttube.commons.models;

import java.util.Date;

/**
 * Stub for chatkit IMessage.
 */
public interface IMessage {
    String getId();
    String getText();
    Date getCreatedAt();
    IUser getUser();

    interface IUser {
        String getId();
        String getName();
        String getAvatar();
    }

    static boolean checkMessage(IMessage message) {
        return message != null && message.getId() != null && message.getText() != null;
    }
}
