package minefarts.smarttube.utils.chat.gen

import minefarts.smarttube.utils.common.models.gen.TextItem
import minefarts.smarttube.utils.common.models.gen.ThumbnailItem

public data class LiveChatAction(
    val addBannerToLiveChatCommand: AddBannerToLiveChatCommand?,
    val addChatItemAction: AddChatItemAction?
)

public data class AddBannerToLiveChatCommand(
    val bannerRenderer: BannerRenderer?
)

public data class AddChatItemAction(
    val item: LiveChatTextMessageRendererItem?
)

public data class LiveChatTextMessageRendererItem(
    val liveChatTextMessageRenderer: LiveChatTextMessageRenderer?
)

public data class LiveChatTextMessageRenderer(
    val message: TextItem?,
    val authorName: TextItem?,
    val authorPhoto: ThumbnailItem?,
    val id: String?,
    val authorExternalChannelId: String?
)

public data class LiveChatBannerHeaderRendererItem(
    val liveChatBannerHeaderRenderer: LiveChatBannerHeaderRenderer?
)

public data class LiveChatBannerHeaderRenderer(
    val text: TextItem?
)

public data class BannerRenderer(
    val liveChatBannerRenderer: LiveChatBannerRenderer?
)

public data class LiveChatBannerRenderer(
    val header: LiveChatBannerHeaderRendererItem?,
    val contents: LiveChatTextMessageRendererItem?
)