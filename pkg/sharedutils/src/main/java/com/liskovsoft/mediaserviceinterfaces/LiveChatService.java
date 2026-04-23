package com.liskovsoft.sharedutils;

import com.liskovsoft.sharedutils.data.ChatItem;
import io.reactivex.Observable;

public interface LiveChatService {
    Observable<ChatItem> openLiveChatObserve(String chatKey);
}
