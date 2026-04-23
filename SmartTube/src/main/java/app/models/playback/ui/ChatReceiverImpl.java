package SmartTubeApp.app.models.playback.ui;

import com.liskovsoft.sharedutils.data.ChatItem;

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
