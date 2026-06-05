package minefarts.smarttube.ui.widgets.chat;

import minefarts.smarttube.utils.data.ChatItem;
import minefarts.smarttube.utils.data.CommentItem;
import minefarts.smarttube.commons.models.IUser;

public class ChatItemAuthor implements IUser {
    private String mId;
    private String mName;
    private String mAvatar;

    public static ChatItemAuthor from(ChatItem chatItem) {
        ChatItemAuthor author = new ChatItemAuthor();
        author.mAvatar = chatItem.getAuthorPhoto();
        author.mName = chatItem.getAuthorName();
        author.mId = chatItem.getAuthorName();

        return author;
    }

    public static ChatItemAuthor from(CommentItem commentItem) {
        ChatItemAuthor author = new ChatItemAuthor();
        author.mAvatar = commentItem.getAuthorPhoto();
        author.mName = commentItem.getAuthorName();
        author.mId = commentItem.getAuthorName();

        return author;
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getAvatar() {
        return mAvatar;
    }
}
