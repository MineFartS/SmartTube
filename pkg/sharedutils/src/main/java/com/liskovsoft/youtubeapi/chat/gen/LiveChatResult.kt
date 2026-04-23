package com.liskovsoft.sharedutils.chat.gen

import com.liskovsoft.sharedutils.next.v2.gen.ContinuationItem

internal data class LiveChatResult(
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
