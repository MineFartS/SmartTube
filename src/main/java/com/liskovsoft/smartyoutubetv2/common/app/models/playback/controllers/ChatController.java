package com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers;

import com.liskovsoft.mediaserviceinterfaces.LiveChatService;
import com.liskovsoft.mediaserviceinterfaces.data.ChatItem;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.BasePlayerController;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.manager.PlayerUI;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.ChatReceiver;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.ChatReceiverImpl;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.UiOptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.AppDialogPresenter;
import com.liskovsoft.youtubeapi.service.YouTubeServiceManager;
import io.reactivex.disposables.Disposable;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller that manages live chat integration for the video player.
 *
 * Responsibilities:
 * - Open and observe live chat stream for the current video (if available)
 * - Filter unwanted chat messages (simple blacklist)
 * - Expose UI controls to enable/disable chat and position it (left/right)
 * - Clean up Rx subscriptions when player is released or controller is finished
 */
public class ChatController extends BasePlayerController {
    private static final String TAG = ChatController.class.getSimpleName();
    /**
     * NOTE: Don't remove duplicates! They contain different chars.
     * Simple author-name blacklist used to filter out spammy chat messages.
     */
    private static final String[] BLACK_LIST = {". XYZ", ". ХYZ", "⠄XYZ", "⠄ХYZ", "Ricardo Merlino", "⠄СОM", ".COM", ".СОM", ". COM"};

    // Service used to open/observe live chat streams
    private LiveChatService mChatService;
    // Disposable representing active chat subscription
    private Disposable mChatAction;
    // Key identifying the live chat for the current media item (may be null)
    private String mLiveChatKey;

    @Override
    public void onInit() {
        // Obtain YouTube live chat service instance
        mChatService = YouTubeServiceManager.instance().getLiveChatService();
    }

    @Override
    public void onMetadata(MediaItemMetadata metadata) {
        // Extract live chat key from metadata (if available)
        mLiveChatKey = metadata != null ? metadata.getLiveChatKey() : null;

        if (mLiveChatKey != null) {
            // Update chat toggle button state according to user preference
            getPlayer().setButtonState(R.id.action_chat, getPlayerData().isLiveChatEnabled() ? PlayerUI.BUTTON_ON : PlayerUI.BUTTON_OFF);
        }

        // If chat was enabled in preferences, open it automatically
        if (getPlayerData().isLiveChatEnabled()) {
            openLiveChat();
        }
    }

    private void openLiveChat() {
        // Ensure any previous actions are disposed before opening new chat
        disposeActions();

        if (mLiveChatKey == null) {
            return;
        }

        // Create chat receiver UI and attach to player
        ChatReceiver chatReceiver = new ChatReceiverImpl();
        getPlayer().setChatReceiver(chatReceiver);

        // Subscribe to live chat stream and forward filtered messages to the UI
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
                // Toggle chat state based on button
                enableLiveChat(buttonState != PlayerUI.BUTTON_ON);
            }
        }
    }

    @Override
    public void onButtonLongClicked(int buttonId, int buttonState) {
        if (buttonId == R.id.action_chat) {
            String chatCategoryTitle = getContext().getString(R.string.open_chat);

            AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

            List<OptionItem> options = new ArrayList<>();

            // Option: disable chat
            options.add(UiOptionItem.from(getContext().getString(R.string.option_disabled),
                    optionItem -> {
                        enableLiveChat(false);
                        settingsPresenter.closeDialog();
                    },
                    !getPlayerData().isLiveChatEnabled()));

            // Option: place chat on left
            options.add(UiOptionItem.from(getContext().getString(R.string.chat_left),
                    optionItem -> {
                        placeChatLeft(true);
                        enableLiveChat(true);
                        settingsPresenter.closeDialog();
                    },
                    getPlayerData().isLiveChatEnabled() && isChatPlacedLeft()));

            // Option: place chat on right
            options.add(UiOptionItem.from(getContext().getString(R.string.chat_right),
                    optionItem -> {
                        placeChatLeft(false);
                        enableLiveChat(true);
                        settingsPresenter.closeDialog();
                    },
                    getPlayerData().isLiveChatEnabled() && !isChatPlacedLeft()));

            settingsPresenter.appendRadioCategory(chatCategoryTitle, options);

            settingsPresenter.showDialog(chatCategoryTitle);
        }
    }

    @Override
    public void onEngineReleased() {
        // Clean up any active subscriptions when player engine is released
        disposeActions();
    }

    @Override
    public void onFinish() {
        // Ensure cleanup on controller finish
        disposeActions();
    }

    private void disposeActions() {
        if (RxHelper.isAnyActionRunning(mChatAction)) {
            RxHelper.disposeActions(mChatAction);
            // Remove chat receiver from player UI
            getPlayer().setChatReceiver(null);
        }
    }

    private boolean checkItem(ChatItem chatItem) {
        if (chatItem == null || chatItem.getAuthorName() == null) {
            return false;
        }

        String authorName = chatItem.getAuthorName();

        // Simple blacklist check (case-insensitive)
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
            getPlayer().setButtonState(R.id.action_chat, enabled ? PlayerUI.BUTTON_ON : PlayerUI.BUTTON_OFF);
        }
    }

    private void placeChatLeft(boolean left) {
        // Persist UI placement preference depending on whether this is live chat or comments fallback
        if (mLiveChatKey != null) {
            getPlayerTweaksData().setChatPlacedLeft(left);
        } else {
            getPlayerTweaksData().setCommentsPlacedLeft(left);
        }
    }

    private boolean isChatPlacedLeft() {
        // Return placement preference based on whether live chat is available
        return mLiveChatKey != null ? getPlayerTweaksData().isChatPlacedLeft() : getPlayerTweaksData().isCommentsPlacedLeft();
    }
}
