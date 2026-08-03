package minefarts.smarttube.utils.comments.impl

import minefarts.smarttube.utils.data.CommentGroup
import minefarts.smarttube.utils.data.CommentItem
import minefarts.smarttube.utils.comments.gen.CommentsResult
import minefarts.smarttube.utils.next.v2.gen.getContinuationToken

public data class CommentGroupImpl(val commentsResult: CommentsResult): CommentGroup {
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
