package minefarts.sharedutils.service;

import minefarts.sharedutils.LiveChatService;
import minefarts.sharedutils.data.ChatItem;
import minefarts.sharedutils.rx.RxHelper;
import minefarts.sharedutils.chat.LiveChatServiceInt;
import io.reactivex.Observable;

public class YouTubeLiveChatService implements LiveChatService {
    private static YouTubeLiveChatService sInstance;
    private final LiveChatServiceInt mLiveChatServiceInt;

    private YouTubeLiveChatService() {
        mLiveChatServiceInt = LiveChatServiceInt.INSTANCE;
    }

    public static YouTubeLiveChatService instance() {
        if (sInstance == null) {
            sInstance = new YouTubeLiveChatService();
        }

        return sInstance;
    }

    @Override
    public Observable<ChatItem> openLiveChatObserve(String chatKey) {
        return RxHelper.createLong(emitter -> {
            mLiveChatServiceInt.openLiveChat(
                    chatKey, emitter::onNext
            );

            emitter.onComplete();
        });
    }
}
