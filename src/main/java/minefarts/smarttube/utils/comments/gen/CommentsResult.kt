package minefarts.smarttube.utils.comments.gen

import com.liskovsoft.youtubeapi.next.v2.gen.ContinuationItem

public data class CommentsResult(
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