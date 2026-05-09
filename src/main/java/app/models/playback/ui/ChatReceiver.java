package minefarts.smarttube.app.models.playback.ui;

import com.liskovsoft.sharedutils.data.ChatItem;

public interface ChatReceiver {
    interface Callback {
        void onChatItem(ChatItem chatItem);
    }
    void addChatItem(ChatItem chatItem);
    void setCallback(Callback callback);
}
