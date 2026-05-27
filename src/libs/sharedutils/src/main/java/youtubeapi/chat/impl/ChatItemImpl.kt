package minefarts.sharedutils.chat.impl

import minefarts.sharedutils.data.ChatItem
import minefarts.sharedutils.chat.gen.LiveChatAction
import minefarts.sharedutils.common.models.gen.getOptimalResThumbnailUrl
import minefarts.sharedutils.common.models.gen.getText

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
