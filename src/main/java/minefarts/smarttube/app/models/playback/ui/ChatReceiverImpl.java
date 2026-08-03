package minefarts.smarttube.app.models.playback.ui;

import minefarts.smarttube.utils.data.ChatItem;

public class ChatReceiverImpl implements ChatReceiver {
    private Callback mCallback;

    @Override
    public void addChatItem(ChatItem chatItem) {
        if (mCallback != null) {
            mCallback.onChatItem(chatItem);
        }
    }

    @Override
    public void setCallback(Callback callback) {
        mCallback = callback;
    }
}
