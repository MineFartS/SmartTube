package minefarts.smarttube.utils.chat

import com.liskovsoft.youtubeapi.common.helpers.PostDataHelper

public object LiveChatApiParams {
    fun getLiveChatQuery(chatKey: String): String {
        val chatData = String.format("\"continuation\":\"%s\"", chatKey)
        return PostDataHelper.createQueryTV(chatData)
    }
}