package minefarts.smarttube.utils.comments

import minefarts.smarttube.utils.data.CommentGroup
import minefarts.smarttube.utils.comments.gen.getDislikeParams
import minefarts.smarttube.utils.comments.gen.getLikeParams
import minefarts.smarttube.utils.comments.gen.getActiveCommentItem
import minefarts.smarttube.utils.comments.gen.getUnLikeParams
import minefarts.smarttube.utils.comments.impl.CommentGroupImpl
import minefarts.smarttube.google.common.helpers.RetrofitHelper

internal object CommentsServiceInt {
    private val mApi = RetrofitHelper.create(CommentsApi::class.java)

    fun getComments(key: String): CommentGroup? {
        val commentsResult = getCommentsResult(key)
        return commentsResult?.let { CommentGroupImpl(it) }
    }

    fun toggleLike(key: String) {
        val commentsResult = getCommentsResult(key)
        val activeCommentItem = commentsResult?.getActiveCommentItem()
        val likeParam = activeCommentItem?.let { if (it.isLiked == true) it.getUnLikeParams() else it.getLikeParams() }
        likeParam?.let { getActionResult(it) }
    }

    fun toggleDislike(key: String) {
        val commentsResult = getCommentsResult(key)
        val likeParam = commentsResult?.getActiveCommentItem()?.getDislikeParams()
        likeParam?.let { getActionResult(it) }
    }

    private fun getCommentsResult(commentsKey: String) = RetrofitHelper.get(mApi.getComments(CommentsApiParams.getCommentsQuery(commentsKey)))

    private fun getActionResult(actionKey: String) = RetrofitHelper.get(mApi.commentAction(CommentsApiParams.getActionQuery(actionKey)))
}