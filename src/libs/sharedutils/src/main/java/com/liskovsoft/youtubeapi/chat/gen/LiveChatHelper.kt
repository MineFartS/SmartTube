package com.liskovsoft.sharedutils.chat.gen

internal fun LiveChatResult.getActions() = continuationContents?.liveChatContinuation?.actions
internal fun LiveChatResult.getContinuation() = continuationContents?.liveChatContinuation?.continuations?.getOrNull(0)
    ?.let { it.timedContinuationData ?: it.invalidationContinuationData }