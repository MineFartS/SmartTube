package minefarts.smarttube.app.presenters.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;

import com.liskovsoft.youtubeapi.service.YouTubeSignInService;
import com.liskovsoft.mediaserviceinterfaces.oauth.Account;

import minefarts.smarttube.R;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.app.presenters.settings.AccountSettingsPresenter;
import minefarts.smarttube.prefs.AccountsData;
import minefarts.smarttube.prefs.GeneralData;
import minefarts.smarttube.utils.Utils;
import minefarts.smarttube.app.models.playback.BasePlayerController;

import java.util.ArrayList;
import java.util.List;

public class AccountSelectionPresenter extends BasePresenter<Void> {
    
    @SuppressLint("StaticFieldLeak")
    private static AccountSelectionPresenter sInstance;

    YouTubeSignInService mSignInService;

    public static AccountSelectionPresenter instance(Context context) {
        if (sInstance == null) {
            sInstance = new AccountSelectionPresenter();
            sInstance.mSignInService = BasePlayerController.getSignInService();
        }

        sInstance.setContext(context);

        return sInstance;
    }

    public void show() {
        show(false);
    }

    public void show(boolean force) {
        if (!AccountsData.instance(getContext()).isSelectAccountOnBootEnabled() && !force) {
            // user don't want to see selection dialog
            return;
        }

        createAndShowDialog(mSignInService.getAccounts(), force);
    }

    public void nextAccountOrDialog() {
        BasePlayerController.loadAccounts(this::nextAccountOrDialog);
    }

    private void createAndShowDialog(List<Account> accounts, boolean force) {
        if (accounts.size() <= 1 && !force) return;

        AppDialogPresenter dialogPresenter = AppDialogPresenter.instance(getContext());

        appendAccountSelection(accounts, dialogPresenter);

        dialogPresenter.showDialog(getContext().getString(R.string.settings_accounts));
    }

    private void appendAccountSelection(List<Account> accounts, AppDialogPresenter settingsPresenter) {
        List<UiOptionItem> optionItems = new ArrayList<>();

        optionItems.add(UiOptionItem.from(
                getContext().getString(R.string.dialog_account_none), optionItem -> {
                    selectAccount(null);
                    settingsPresenter.closeDialog();
                }, true
        ));

        for (Account account : accounts) {
            optionItems.add(UiOptionItem.from(
                    formatAccount(account), option -> {
                        selectAccount(account);
                        settingsPresenter.closeDialog();
                    }, account.isSelected()
            ));
        }

        settingsPresenter.appendRadioCategory(getContext().getString(R.string.dialog_account_list), optionItems);
    }

    private void nextAccountOrDialog(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            AccountSettingsPresenter.instance(getContext()).show();
            return;
        }

        Account current = null;

        for (Account account : accounts) {
            if (account.isSelected()) {
                current = account;
                break;
            }
        }

        int index = accounts.indexOf(current);

        int nextIndex = index + 1;
        // null == 'without account'
        selectAccount(nextIndex == accounts.size() ? null : accounts.get(nextIndex));
        //selectAccount(accounts.get(nextIndex == accounts.size() ? 0 : nextIndex));
    }

    public void selectAccount(Account account) {
        mSignInService.selectAccount(account);
        Utils.updateChannels(getContext());
    }

    private String formatAccount(Account account) {
        String format;

        if (account.getEmail() != null) {
            format = String.format("%s (%s)", account.getName(), account.getEmail());
        } else {
            format = account.getName();
        }

        return format;
    }
}
