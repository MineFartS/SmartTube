package minefarts.sharedutils.comments.gen

import minefarts.sharedutils.common.models.gen.ButtonContentWrapper
import minefarts.sharedutils.common.models.gen.NavigationEndpointItem
import minefarts.sharedutils.common.models.gen.TextItem
import minefarts.sharedutils.common.models.gen.ThumbnailItem

internal data class CommentItemWrapper(
    val commentThreadRenderer: CommentThreadRenderer?,
    val commentRenderer: CommentRenderer? // the active comment
) {
    data class CommentThreadRenderer(
        val comment: Comment?
    ) {
        data class Comment(
            val commentRenderer: CommentRenderer?
        )
    }
}

internal data class CommentRenderer(
    val commentId: String?,
    val authorText: TextItem?,
    val authorThumbnail: ThumbnailItem?,
    val publishedTimeText: TextItem?,
    val contentText: TextItem?,
    val detailViewEndpoint: NavigationEndpointItem?,
    val isLiked: Boolean?,
    val voteCount: TextItem?,
    val repliesCount: TextItem?,
    val actionButtons: ActionButtonsWrapper?
) {
    data class ActionButtonsWrapper(
        val commentActionButtonsRenderer: CommentActionButtonsRenderer?
    ) {
        data class CommentActionButtonsRenderer(
            val likeButton: ButtonContentWrapper?,
            val dislikeButton: ButtonContentWrapper?
        )
    }
}