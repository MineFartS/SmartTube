package minefarts.sharedutils.comments

import minefarts.sharedutils.data.CommentGroup
import minefarts.sharedutils.comments.gen.getDislikeParams
import minefarts.sharedutils.comments.gen.getLikeParams
import minefarts.sharedutils.comments.gen.getActiveCommentItem
import minefarts.sharedutils.comments.gen.getUnLikeParams
import minefarts.sharedutils.comments.impl.CommentGroupImpl
import minefarts.googlecommon.common.helpers.RetrofitHelper

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