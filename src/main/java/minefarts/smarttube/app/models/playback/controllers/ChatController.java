package minefarts.smarttube.app.models.playback.controllers;

import com.liskovsoft.mediaserviceinterfaces.LiveChatService;
import com.liskovsoft.mediaserviceinterfaces.data.ChatItem;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.rx.RxHelper;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.playback.BasePlayerController;
import minefarts.smarttube.app.models.playback.ui.ChatReceiver;
import minefarts.smarttube.app.models.playback.ui.ChatReceiverImpl;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
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
        mChatService = getLiveChatService();
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

        onEngineReleased();

        if (mLiveChatKey == null) return;

        ChatReceiver chatReceiver = new ChatReceiverImpl();
        getPlayer().setChatReceiver(chatReceiver);

        mChatAction = mChatService.openLiveChatObserve(mLiveChatKey).subscribe(
            
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

        super.onButtonClicked(buttonId, buttonState);

        if (buttonId == R.id.action_chat) {
            if (mLiveChatKey != null) {
                enableLiveChat(buttonState != 1);
            }
        }
        
    }

    @Override
    public void onButtonLongClicked(int buttonId, int buttonState) {
        
        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

        if (buttonId == R.id.action_chat) {

            List<UiOptionItem> options = new ArrayList<>();

            options.add(UiOptionItem.from(
                getContext().getString(R.string.option_disabled),
                optionItem -> {
                    enableLiveChat(false);
                    settingsPresenter.closeDialog();
                },
                !getPlayerData().isLiveChatEnabled()
            ));

            options.add(UiOptionItem.from(
                "Right",
                optionItem -> {
                    enableLiveChat(true);
                    settingsPresenter.closeDialog();
                },
                getPlayerData().isLiveChatEnabled()
            ));

            String title = "Chat/Comments";

            settingsPresenter.appendRadioCategory(title, options);

            settingsPresenter.showDialog(title);

        }
    }

    @Override
    public void onEngineReleased() {
        if (RxHelper.isAnyActionRunning(mChatAction)) {
            RxHelper.disposeActions(mChatAction);
            getPlayer().setChatReceiver(null);
        }
    }

    @Override
    public void onFinish() {
        onEngineReleased();
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
            onEngineReleased();
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
