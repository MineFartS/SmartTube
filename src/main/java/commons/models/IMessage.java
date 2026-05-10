

package minefarts.smarttube.commons.models;

import java.util.Date;

/**
 * For implementing by real message model
 */
public interface IMessage {

    /**
     * Returns message identifier
     *
     * @return the message id
     */
    String getId();

    /**
     * Returns message text
     *
     * @return the message text
     */
    CharSequence getText();

    /**
     * Returns message author. See the {@link IUser} for more details
     *
     * @return the message author
     */
    IUser getUser();

    /**
     * Returns message creation date
     *
     * @return the message creation date
     */
    Date getCreatedAt();

    static boolean checkMessage(IMessage message) {
        return message != null && message.getId() != null && message.getUser() != null && message.getUser().getId() != null
                && message.getText() != null;
    }
}
