package minefarts.smarttube.utils.comments.gen

import minefarts.smarttube.utils.next.v2.gen.ContinuationItem

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