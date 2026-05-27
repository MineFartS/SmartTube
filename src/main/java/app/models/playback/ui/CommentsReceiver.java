package minefarts.smarttube.app.models.playback.ui;

import minefarts.sharedutils.data.CommentGroup;
import minefarts.sharedutils.data.CommentItem;

public interface CommentsReceiver {
    interface Callback {
        void onCommentGroup(CommentGroup commentGroup);
        void onBackup(Backup backup);
        void onSync(CommentItem commentItem);
    }
    interface Backup {}
    void addCommentGroup(CommentGroup commentGroup);
    void loadBackup(Backup backup);
    void sync(CommentItem commentItem);
    void setCallback(Callback callback);
    void onLoadMore(CommentGroup commentGroup);
    void onStart();
    void onCommentClicked(CommentItem commentItem);
    void onCommentLongClicked(CommentItem commentItem);
    void onFinish(Backup backup);
    String getLoadingMessage();
    String getErrorMessage();
}
