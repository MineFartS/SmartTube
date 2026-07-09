package minefarts.smarttube.utils.chat.gen

public fun LiveChatResult.getActions() = continuationContents?.liveChatContinuation?.actions
public fun LiveChatResult.getContinuation() = continuationContents?.liveChatContinuation?.continuations?.getOrNull(0)
    ?.let { it.timedContinuationData ?: it.invalidationContinuationData }