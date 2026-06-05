package minefarts.smarttube.utils.chat.impl

import minefarts.smarttube.utils.data.ChatItem
import minefarts.smarttube.utils.chat.gen.LiveChatAction
import minefarts.smarttube.utils.common.models.gen.getOptimalResThumbnailUrl
import minefarts.smarttube.utils.common.models.gen.getText

internal data class ChatItemImpl(val liveChatAction: LiveChatAction): ChatItem {
    private val messageRenderer by lazy {
        liveChatAction.addChatItemAction?.item?.liveChatTextMessageRenderer ?:
        liveChatAction.addBannerToLiveChatCommand?.bannerRenderer?.liveChatBannerRenderer?.contents?.liveChatTextMessageRenderer
    }

    override fun getId(): String? {
        return messageRenderer?.id
    }

    override fun getMessage(): String? {
        return messageRenderer?.message?.getText()
    }

    override fun getAuthorName(): String? {
        return messageRenderer?.authorName?.getText()
    }

    override fun getAuthorPhoto(): String? {
        return messageRenderer?.authorPhoto?.getOptimalResThumbnailUrl()
    }
}
