package SmartTubeApp.app.models.playback.controllers;

import com.liskovsoft.sharedutils.LiveChatService;
import com.liskovsoft.sharedutils.data.ChatItem;
import com.liskovsoft.sharedutils.data.MediaItemMetadata;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import SmartTubeApp.R;
import SmartTubeApp.app.models.playback.BasePlayerController;
import SmartTubeApp.app.models.playback.ui.ChatReceiver;
import SmartTubeApp.app.models.playback.ui.ChatReceiverImpl;
import SmartTubeApp.app.models.playback.ui.OptionItem;
import SmartTubeApp.app.models.playback.ui.UiOptionItem;
import SmartTubeApp.app.presenters.AppDialogPresenter;
import com.liskovsoft.sharedutils.service.YouTubeServiceManager;
import io.reactivex.disposables.Disposable;

import java.util.ArrayList;
import java.util.List;

public class ChatController extends BasePlayerController {
    private static final String TAG = ChatController.class.getSimpleName();
    /**
     * NOTE: Don't remove duplicates! They contain different chars.
     */
    private static final String[] BLACK_LIST = {". XYZ", ". ХYZ", "⠄XYZ", "⠄ХYZ", "Ricardo Merlino", "⠄СОM", ".COM", ".СОM", ". COM"};
    private LiveChatService mChatService;
    private Disposable mChatAction;
    private String mLiveChatKey;

    @Override
    public void onInit() {
        mChatService = YouTubeServiceManager.instance().getLiveChatService();
    }

    @Override
    public void onMetadata(MediaItemMetadata metadata) {
        mLiveChatKey = metadata != null ? metadata.getLiveChatKey() : null;

        if (mLiveChatKey != null) {
            getPlayer().setButtonState(R.id.action_chat, getPlayerData().isLiveChatEnabled() ? 1 : 0);
        }

        if (getPlayerData().isLiveChatEnabled()) {
            openLiveChat();
        }
    }

    private void openLiveChat() {
        disposeActions();

        if (mLiveChatKey == null) {
            return;
        }

        ChatReceiver chatReceiver = new ChatReceiverImpl();
        getPlayer().setChatReceiver(chatReceiver);

        mChatAction = mChatService.openLiveChatObserve(mLiveChatKey)
                .subscribe(
                        chatItem -> {
                            Log.d(TAG, chatItem.getMessage());
                            if (checkItem(chatItem)) {
                                chatReceiver.addChatItem(chatItem);
                            }
                        },
                        error -> {
                            Log.e(TAG, error.getMessage());
                            error.printStackTrace();
                        },
                        () -> Log.e(TAG, "Live chat session has been closed")
                );
    }

    @Override
    public void onButtonClicked(int buttonId, int buttonState) {
        if (buttonId == R.id.action_chat) {
            if (mLiveChatKey != null) {
                enableLiveChat(buttonState != 1);
            }
        }
    }

    @Override
    public void onButtonLongClicked(int buttonId, int buttonState) {
        if (buttonId == R.id.action_chat) {
            String chatCategoryTitle = getContext().getString(R.string.open_chat);

            AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

            List<OptionItem> options = new ArrayList<>();

            options.add(UiOptionItem.from(getContext().getString(R.string.option_disabled),
                    optionItem -> {
                        enableLiveChat(false);
                        settingsPresenter.closeDialog();
                    },
                    !getPlayerData().isLiveChatEnabled()));

            options.add(UiOptionItem.from(getContext().getString(R.string.chat_right),
                    optionItem -> {
                        enableLiveChat(true);
                        settingsPresenter.closeDialog();
                    },
                    getPlayerData().isLiveChatEnabled()));

            settingsPresenter.appendRadioCategory(chatCategoryTitle, options);

            settingsPresenter.showDialog(chatCategoryTitle);
        }
    }

    @Override
    public void onEngineReleased() {
        disposeActions();
    }

    @Override
    public void onFinish() {
        disposeActions();
    }

    private void disposeActions() {
        if (RxHelper.isAnyActionRunning(mChatAction)) {
            RxHelper.disposeActions(mChatAction);
            getPlayer().setChatReceiver(null);
        }
    }

    private boolean checkItem(ChatItem chatItem) {
        if (chatItem == null || chatItem.getAuthorName() == null) {
            return false;
        }

        String authorName = chatItem.getAuthorName();

        for (String spammer : BLACK_LIST) {
            if (authorName.toLowerCase().contains(spammer.toLowerCase())) {
                return false;
            }
        }

        return true;
    }

    private void enableLiveChat(boolean enabled) {
        
        if (enabled) {
            openLiveChat();
        } else {
            disposeActions();
        }
        
        getPlayerData().setLiveChatEnabled(enabled);

        if (mLiveChatKey != null) {
            getPlayer().setButtonState(
                R.id.action_chat, 
                enabled ? 1 : 0
            );
        }

    }

}
