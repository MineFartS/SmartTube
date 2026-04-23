package com.liskovsoft.sharedutils.comments.impl

import com.liskovsoft.sharedutils.data.CommentGroup
import com.liskovsoft.sharedutils.data.CommentItem
import com.liskovsoft.sharedutils.comments.gen.CommentsResult
import com.liskovsoft.sharedutils.next.v2.gen.getContinuationToken

internal data class CommentGroupImpl(val commentsResult: CommentsResult): CommentGroup {
    private val itemSectionContinuation by lazy {
        commentsResult.continuationContents?.itemSectionContinuation
    }

    private val commentItemWrappers by lazy {
        itemSectionContinuation?.contents
    }

    private val nextCommentsKeyItem by lazy {
        itemSectionContinuation?.continuations?.getContinuationToken()
    }

    private val commentItems by lazy {
        commentItemWrappers?.mapNotNull { it?.let { CommentItemImpl(it) } }
    }

    override fun getComments(): List<CommentItem?>? = commentItems

    override fun getNextCommentsKey(): String? = nextCommentsKeyItem
}
