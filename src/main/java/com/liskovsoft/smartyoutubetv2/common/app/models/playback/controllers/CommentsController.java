package com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers;

import android.content.Context;
import android.util.Pair;

import com.liskovsoft.mediaserviceinterfaces.data.CommentGroup;
import com.liskovsoft.mediaserviceinterfaces.data.CommentItem;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.BasePlayerController;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.CommentsReceiver;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.CommentsReceiver.Backup;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.AbstractCommentsReceiver;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.UiOptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.AppDialogPresenter;
import io.reactivex.disposables.Disposable;

/**
 * Controller responsible for loading and showing comments UI for the currently playing item.
 *
 * Responsibilities:
 * - Keep references to comments/live-chat keys from MediaItemMetadata.
 * - Open comment dialogs and handle nested comment dialogs.
 * - Load comments from the comments service and stream them to CommentsReceiver.
 * - Handle user interactions with comments (like toggling likes).
 * - Preserve a small backup of the last opened comments page to speed re-opening.
 *
 * This controller is lightweight and delegates presentation to AppDialogPresenter and
 * comment rendering to CommentsReceiver implementations.
 */
public class CommentsController extends BasePlayerController {
    private static final String TAG = CommentsController.class.getSimpleName();
    // Disposable for ongoing comments loading action
    private Disposable mCommentsAction;
    // Keys coming from metadata to load comments or live chat
    private String mLiveChatKey;
    private String mCommentsKey;
    // Fallback title used when player video title is not available
    private String mTitle;
    // Simple cache of last opened comments key + backup payload to restore UI quickly
    private Pair<String, Backup> mBackup;

    public CommentsController() {
    }

    /**
     * Convenience ctor used when metadata is available at creation time.
     * Sets an alternate context and initializes metadata.
     */
    public CommentsController(Context context, MediaItemMetadata metadata) {
        setAltContext(context);
        onInit();
        onMetadata(metadata);
    }

    /**
     * Update controller with new metadata (e.g. when a new video starts).
     * Extracts comment/live-chat keys and title. Clears stale backup if the key changed.
     */
    @Override
    public void onMetadata(MediaItemMetadata metadata) {
        mLiveChatKey = metadata != null && metadata.getLiveChatKey() != null ? metadata.getLiveChatKey() : null;
        mCommentsKey = metadata != null && metadata.getCommentsKey() != null ? metadata.getCommentsKey() : null;
        mTitle = metadata != null ? metadata.getTitle() : null;
        if (mBackup != null && !Helpers.equals(mBackup.first, mCommentsKey)) {
            mBackup = null;
        }
    }

    /**
     * Prepares and shows the comments dialog for the current item.
     *
     * Behavior:
     * - Stops any ongoing comment load actions.
     * - If commentsKey is missing, does nothing.
     * - Hides player controls while dialog is shown.
     * - Uses AbstractCommentsReceiver to stream and paginate comments.
     * - Saves a Backup snapshot on finish to speed re-open.
     */
    private void openCommentsDialog() {
        fitVideoIntoDialog();

        disposeActions();

        if (mCommentsKey == null) {
            return;
        }

        final String backupKey = mCommentsKey;

        if (getPlayer() != null) {
            getPlayer().showControls(false);
        }

        String title = getPlayer() != null && getPlayer().getVideo() != null ? getPlayer().getVideo().getTitleFull() : mTitle;

        CommentsReceiver commentsReceiver = new AbstractCommentsReceiver(getContext()) {
            @Override
            public void onLoadMore(CommentGroup commentGroup) {
                loadComments(this, commentGroup.getNextCommentsKey());
            }

            @Override
            public void onStart() {
                if (mBackup != null && Helpers.equals(mBackup.first, mCommentsKey)) {
                    loadBackup(mBackup.second);
                    return;
                }

                loadComments(this, mCommentsKey);
            }

            @Override
            public void onCommentClicked(CommentItem commentItem) {
                if (commentItem.isEmpty()) {
                    return;
                }

                // Open nested comment dialog for replies
                CommentsReceiver nestedReceiver = new AbstractCommentsReceiver(getContext()) {
                    @Override
                    public void onLoadMore(CommentGroup commentGroup) {
                        loadComments(this, commentGroup.getNextCommentsKey());
                    }

                    @Override
                    public void onStart() {
                        loadComments(this, commentItem.getNestedCommentsKey());
                    }

                    @Override
                    public void onCommentLongClicked(CommentItem commentItem) {
                        toggleLike(this, commentItem);
                    }
                };

                showDialog(nestedReceiver, title);
            }

            @Override
            public void onCommentLongClicked(CommentItem commentItem) {
                toggleLike(this, commentItem);
            }

            @Override
            public void onFinish(Backup backup) {
                // Save backup only if the dialog corresponds to current commentsKey
                if (Helpers.equals(backupKey, mCommentsKey)) {
                    mBackup = new Pair<>(mCommentsKey, backup);
                }
            }
        };

        showDialog(commentsReceiver, title);
    }

    /**
     * Handle toolbar / UI button clicks.
     * For comments/chat action it opens comments or shows 'empty' message.
     */
    @Override
    public void onButtonClicked(int buttonId, int buttonState) {
        if (buttonId == R.id.action_chat) {
            if (mCommentsKey != null && mLiveChatKey == null) {
                openCommentsDialog();
            }

            if (mCommentsKey == null && mLiveChatKey == null) {
                MessageHelpers.showMessage(getContext(), R.string.section_is_empty);
            }
        }
    }

    @Override
    public void onEngineReleased() {
        disposeActions();
        mBackup = null;
    }

    @Override
    public void onFinish() {
        disposeActions();
    }

    /**
     * Dispose any Rx disposables used for loading comments.
     */
    private void disposeActions() {
        RxHelper.disposeActions(mCommentsAction);
    }

    /**
     * Start loading comments for the provided key and stream results into receiver.
     * Errors will remove the loading indicator by pushing a null group.
     */
    private void loadComments(CommentsReceiver receiver, String commentsKey) {
        disposeActions();

        mCommentsAction = getCommentsService().getCommentsObserve(commentsKey)
                .subscribe(
                        receiver::addCommentGroup,
                        error -> {
                            Log.e(TAG, error.getMessage());
                            receiver.addCommentGroup(null); // remove loading message
                        }
                );
    }

    /**
     * Helper to append the comments receiver as a dialog option and show the dialog.
     */
    private void showDialog(CommentsReceiver receiver, String title) {
        AppDialogPresenter appDialogPresenter = getAppDialogPresenter();

        appDialogPresenter.appendCommentsCategory(title, UiOptionItem.from(title, receiver));
        //appDialogPresenter.enableTransparent(true);
        appDialogPresenter.showDialog();
    }

    /**
     * Toggle like status for a comment. Updates the local receiver immediately and
     * triggers an async request to the service. Displays error message on failure.
     */
    private void toggleLike(CommentsReceiver receiver, CommentItem commentItem) {
        MyCommentItem myCommentItem = MyCommentItem.from(commentItem);
        myCommentItem.setLiked(!myCommentItem.isLiked());

        receiver.sync(myCommentItem);

        RxHelper.execute(
                getCommentsService().toggleLikeObserve(commentItem.getNestedCommentsKey()), e -> MessageHelpers.showMessage(getContext(), e.getMessage()));
    }

    /**
     * Local mutable CommentItem implementation used to reflect immediate UI changes
     * (like toggling a like count) without mutating original CommentItem instances.
     */
    private static final class MyCommentItem implements CommentItem {
        private final String mId;
        private final String mMessage;
        private final String mAuthorName;
        private final String mAuthorPhoto;
        private final String mPublishedDate;
        private final String mNestedCommentsKey;
        private boolean mIsLiked;
        private String mLikeCount;
        private final String mReplyCount;
        private final boolean mIsEmpty;

        private MyCommentItem(
                String id, String message, String authorName, String authorPhoto, String publishedDate,
                String nestedCommentsKey, boolean isLiked, String likeCount, String replyCount, boolean isEmpty) {
            mId = id;
            mMessage = message;
            mAuthorName = authorName;
            mAuthorPhoto = authorPhoto;
            mPublishedDate = publishedDate;
            mNestedCommentsKey = nestedCommentsKey;
            mIsLiked = isLiked;
            mLikeCount = likeCount;
            mReplyCount = replyCount;
            mIsEmpty = isEmpty;
        }

        @Override
        public String getId() {
            return mId;
        }

        @Override
        public String getMessage() {
            return mMessage;
        }

        @Override
        public String getAuthorName() {
            return mAuthorName;
        }

        @Override
        public String getAuthorPhoto() {
            return mAuthorPhoto;
        }

        @Override
        public String getPublishedDate() {
            return mPublishedDate;
        }

        @Override
        public String getNestedCommentsKey() {
            return mNestedCommentsKey;
        }

        @Override
        public boolean isLiked() {
            return mIsLiked;
        }

        /**
         * Update like state and adjust cached like count string accordingly.
         * Keeps likeCount null when zero to follow existing UI expectations.
         */
        public void setLiked(boolean isLiked) {
            if (mIsLiked == isLiked) {
                return;
            }

            mIsLiked = isLiked;

            if (mLikeCount == null) {
                mLikeCount = String.valueOf(0);
            }

            if (Helpers.isInteger(getLikeCount())) {
                int likeCount = Helpers.parseInt(getLikeCount());
                int count = isLiked ? ++likeCount : --likeCount;
                mLikeCount = count > 0 ? String.valueOf(count) : null;
            }
        }

        @Override
        public String getLikeCount() {
            return mLikeCount;
        }

        @Override
        public String getReplyCount() {
            return mReplyCount;
        }

        @Override
        public boolean isEmpty() {
            return mIsEmpty;
        }

        /**
         * Create a mutable copy from an existing CommentItem.
         */
        public static MyCommentItem from(CommentItem commentItem) {
            return new MyCommentItem(commentItem.getId(), commentItem.getMessage(), commentItem.getAuthorName(),
                    commentItem.getAuthorPhoto(), commentItem.getPublishedDate(), commentItem.getNestedCommentsKey(),
                    commentItem.isLiked(), commentItem.getLikeCount(), commentItem.getReplyCount(), commentItem.isEmpty());
        }
    }
}
