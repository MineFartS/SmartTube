package minefarts.smarttube.utils;

import minefarts.smarttube.utils.data.ChatItem;
import io.reactivex.Observable;

public interface LiveChatService {
    Observable<ChatItem> openLiveChatObserve(String chatKey);
}
