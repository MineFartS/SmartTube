package com.liskovsoft.sharedutils.service

import com.liskovsoft.sharedutils.CommentsService
import com.liskovsoft.sharedutils.data.CommentGroup
import com.liskovsoft.sharedutils.rx.RxHelper
import com.liskovsoft.sharedutils.comments.CommentsServiceInt
import io.reactivex.Observable

internal object YouTubeCommentsService: CommentsService {
    private fun getComments(key: String?): CommentGroup? {
        return key?.let { CommentsServiceInt.getComments(key) }
    }

    private fun toggleLike(key: String?) {
        key?.let { CommentsServiceInt.toggleLike(key) }
    }

    private fun toggleDislike(key: String?) {
        key?.let { CommentsServiceInt.toggleDislike(key) }
    }

    override fun getCommentsObserve(key: String?): Observable<CommentGroup> {
        return RxHelper.fromCallable { getComments(key) }
    }

    override fun toggleLikeObserve(key: String?): Observable<Void> {
        return RxHelper.fromRunnable { toggleLike(key) }
    }

    override fun toggleDislikeObserve(key: String?): Observable<Void> {
        return RxHelper.fromRunnable { toggleDislike(key) }
    }
}