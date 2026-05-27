package minefarts.smarttube.utils.chat

import minefarts.smarttube.utils.common.helpers.PostDataHelper

internal object LiveChatApiParams {
    fun getLiveChatQuery(chatKey: String): String {
        val chatData = String.format("\"continuation\":\"%s\"", chatKey)
        return PostDataHelper.createQueryTV(chatData)
    }
}