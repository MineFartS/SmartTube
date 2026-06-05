package minefarts.smarttube.utils.chat

import minefarts.smarttube.utils.data.ChatItem
import minefarts.smarttube.utils.mylogger.Log
import minefarts.smarttube.utils.chat.gen.LiveChatResult
import minefarts.smarttube.utils.chat.gen.getActions
import minefarts.smarttube.utils.chat.gen.getContinuation
import minefarts.smarttube.utils.chat.impl.ChatItemImpl
import minefarts.smarttube.google.common.helpers.RetrofitHelper
import java.io.InterruptedIOException
import java.net.SocketTimeoutException

object LiveChatServiceInt {
    
    private val TAG = LiveChatServiceInt::class.simpleName
    private val mApi = RetrofitHelper.create(LiveChatApi::class.java)

    fun openLiveChat(chatKey: String, onChatItem: OnChatItem) {
        // It's common to stream to be interrupted multiple times
        while (true) {
            try {
                openLiveChatInt(chatKey, onChatItem)
                Thread.sleep(5_000) // fix too frequent request
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "Connection hanged. Reconnecting...")
            } catch (e: InterruptedIOException) {
                Log.e(TAG, "Oops. Stopping. Listening thread interrupted.")
                break
            } catch (e: InterruptedException) {
                Log.e(TAG, "Oops. Stopping. Listening thread interrupted.")
                break
            } catch (e: NullPointerException) {
                Log.e(TAG, "Oops. Stopping. Got NPE.")
                e.printStackTrace()
                break
            } catch (e: Exception) {
                Log.e(TAG, e.message)
                // Continue to listen whichever is happening.
            }
        }
    }

    private fun openLiveChatInt(chatKey: String, onChatItem: OnChatItem) {
        var continuationKey: String? = chatKey
        var timeoutMs: Int?

        while (true) {
            if (continuationKey.isNullOrEmpty()) {
                break
            }

            val chatResult = getLiveChatResult(continuationKey)
            val continuation = chatResult?.getContinuation()
            continuationKey = continuation?.continuation
            timeoutMs = continuation?.timeoutMs

            val actions = chatResult?.getActions()
            actions?.forEach { it?.let { onChatItem.onChatItem(ChatItemImpl(it)) } }

            timeoutMs?.run { Thread.sleep(timeoutMs.toLong()) }
        }
    }

    private fun getLiveChatResult(chatKey: String): LiveChatResult? {
        val chatQuery = LiveChatApiParams.getLiveChatQuery(chatKey)
        val wrapper = mApi.getLiveChat(chatQuery)
        return RetrofitHelper.get(wrapper)
    }

    interface OnChatItem {
        fun onChatItem(chatItem: ChatItem)
    }
}