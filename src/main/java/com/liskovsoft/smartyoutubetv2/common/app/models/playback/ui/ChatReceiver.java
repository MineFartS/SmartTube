package com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui;

/** Consumer interface for live chat items used by the player UI. */
import com.liskovsoft.mediaserviceinterfaces.data.ChatItem;

public interface ChatReceiver {
    interface Callback {
        void onChatItem(ChatItem chatItem);
    }
    void addChatItem(ChatItem chatItem);
    void setCallback(Callback callback);
}
