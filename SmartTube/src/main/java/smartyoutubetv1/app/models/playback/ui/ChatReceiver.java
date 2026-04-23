package SmartTubeApp.app.models.playback.ui;

import com.liskovsoft.youtubeapi.data.ChatItem;

public interface ChatReceiver {
    interface Callback {
        void onChatItem(ChatItem chatItem);
    }
    void addChatItem(ChatItem chatItem);
    void setCallback(Callback callback);
}
