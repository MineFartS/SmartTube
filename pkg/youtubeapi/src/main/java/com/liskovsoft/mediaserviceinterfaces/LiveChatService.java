package com.liskovsoft.youtubeapi;

import com.liskovsoft.youtubeapi.data.ChatItem;
import io.reactivex.Observable;

public interface LiveChatService {
    Observable<ChatItem> openLiveChatObserve(String chatKey);
}
