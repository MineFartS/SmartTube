package minefarts.smarttube.utils.chat.gen

import com.liskovsoft.youtubeapi.next.v2.gen.ContinuationItem

public data class LiveChatResult(
    val continuationContents: ContinuationContents?
) {
    data class ContinuationContents(
        val liveChatContinuation: LiveChatContinuation?
    ) {
        data class LiveChatContinuation(
            val continuations: List<ContinuationItem?>?,
            val actions: List<LiveChatAction?>?
        )
    }
}
