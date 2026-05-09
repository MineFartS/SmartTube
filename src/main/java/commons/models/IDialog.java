

package minefarts.smarttube.commons.models;

import java.util.List;

/**
 * For implementing by real dialog model
 */

public interface IDialog<MESSAGE extends IMessage> {

    String getId();

    String getDialogPhoto();

    String getDialogName();

    List<? extends IUser> getUsers();

    MESSAGE getLastMessage();

    void setLastMessage(MESSAGE message);

    int getUnreadCount();
}
