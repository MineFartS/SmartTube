package com.liskovsoft.sharedutils.comments.gen

import com.liskovsoft.sharedutils.next.v2.gen.ContinuationItem

internal data class CommentsResult(
    val continuationContents: ContinuationContents?
) {
    data class ContinuationContents(
        val itemSectionContinuation: ItemSectionContinuation?
    ) {
        data class ItemSectionContinuation(
            val contents: List<CommentItemWrapper?>?,
            val continuations: List<ContinuationItem?>?
        )
    }
}