package minefarts.smarttube.utils.comments

import minefarts.smarttube.utils.rx.RxHelper
import minefarts.smarttube.utils.comments.gen.getDislikeParams
import minefarts.smarttube.utils.comments.gen.getLikeParams
import minefarts.smarttube.utils.comments.gen.getActiveCommentItem
import minefarts.smarttube.utils.comments.gen.getUnLikeParams
import minefarts.smarttube.utils.common.helpers.PostDataHelper
import minefarts.smarttube.google.common.helpers.RetrofitHelper

import io.reactivex.Observable

public object CommentsService {

    private val mApi = RetrofitHelper.create(CommentsApi::class.java)

    private fun toggleLike(key: String?) {
        key?.let { toggleLike2(key) }
    }

    private fun toggleDislike(key: String?) {
        key?.let { toggleDislike2(key) }
    }

    fun toggleLikeObserve(key: String?): Observable<Void> {
        return RxHelper.fromRunnable { toggleLike(key) }
    }

    fun toggleDislikeObserve(key: String?): Observable<Void> {
        return RxHelper.fromRunnable { toggleDislike(key) }
    }

    private fun toggleLike2(key: String) {
        val commentsResult = getCommentsResult(key)
        val activeCommentItem = commentsResult?.getActiveCommentItem()
        val likeParam = activeCommentItem?.let { if (it.isLiked == true) it.getUnLikeParams() else it.getLikeParams() }
        likeParam?.let { getActionResult(it) }
    }

    private fun toggleDislike2(key: String) {
        val commentsResult = getCommentsResult(key)
        val likeParam = commentsResult?.getActiveCommentItem()?.getDislikeParams()
        likeParam?.let { getActionResult(it) }
    }

    public fun getCommentsResult(commentsKey: String) = RetrofitHelper.get(
        mApi.getComments(getCommentsQuery(commentsKey))
    )

    private fun getActionResult(actionKey: String) = RetrofitHelper.get(
        mApi.commentAction(getActionQuery(actionKey))
    )

    private fun getCommentsQuery(commentsKey: String): String {
        val chatData = String.format("\"continuation\":\"%s\"", commentsKey)
        return PostDataHelper.createQueryTV(chatData)
    }

    private fun getActionQuery(actionKey: String): String {
        return PostDataHelper.createQueryTV(String.format("\"actions\":[\"%s\"]", actionKey))
    }

}