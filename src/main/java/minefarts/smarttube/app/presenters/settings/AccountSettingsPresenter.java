package minefarts.smarttube.app.presenters.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import minefarts.smarttube.utils.oauth.Account;
import minefarts.smarttube.utils.helpers.MessageHelpers;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.app.presenters.BrowsePresenter;
import minefarts.smarttube.app.presenters.SignInPresenter;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.app.presenters.dialogs.AccountSelectionPresenter;
import minefarts.smarttube.utils.ServiceManager;
import minefarts.smarttube.prefs.AccountsData;
import minefarts.smarttube.prefs.AppPrefs;
import minefarts.smarttube.utils.AppDialogUtil;
import minefarts.smarttube.utils.SimpleEditDialog;
import minefarts.smarttube.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class AccountSettingsPresenter extends BasePresenter<Void> {

    @SuppressLint("StaticFieldLeak")
    private static AccountSettingsPresenter sInstance;

    public AccountSettingsPresenter(Context context) {
        super(context);
    }

    public static AccountSettingsPresenter instance(Context context) {
        if (sInstance == null) {
            sInstance = new AccountSettingsPresenter(context);
        }

        sInstance.setContext(context);

        return sInstance;
    }

    public void unhold() {
        sInstance = null;
    }

    public void show() {
        ServiceManager.loadAccounts(this::createAndShowDialog);
    }

    private void createAndShowDialog(List<Account> accounts) {
        
        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

        AccountsData accountsData = AccountsData.instance(getContext());

        appendSelectAccountSection(accounts, settingsPresenter);
        
        settingsPresenter.appendSingleButton(
            UiOptionItem.from(
                getContext().getString(R.string.dialog_add_account), 
                option -> SignInPresenter.instance(getContext()).start()
            )
        );
        
        appendSignOutSection(accounts, settingsPresenter);
        
        appendSeparateSettings(settingsPresenter);
        
        settingsPresenter.appendSingleSwitch(
            UiOptionItem.from(
                getContext().getString(R.string.select_account_on_boot), 
                optionItem -> {
                    accountsData.selectAccountOnBoot(optionItem.isSelected());
                }, 
                accountsData.isSelectAccountOnBootEnabled()
            )
        );

        Account account = getSignInService().getSelectedAccount();

        settingsPresenter.showDialog(
            account != null ? account.getName() : getContext().getString(R.string.settings_accounts), 
            this::unhold
        );
    
    }

    private void appendSelectAccountSection(List<Account> accounts, AppDialogPresenter settingsPresenter) {
        if (accounts == null || accounts.isEmpty()) {
            return;
        }

        List<UiOptionItem> optionItems = new ArrayList<>();

        optionItems.add(UiOptionItem.from(
                getContext().getString(R.string.dialog_account_none), optionItem -> {
                    AccountSelectionPresenter.instance(getContext()).selectAccount(null);
                    settingsPresenter.closeDialog();
                }, true
        ));

        String accountName = " (" + getContext().getString(R.string.dialog_account_none) + ")";

        for (Account account : accounts) {
            optionItems.add(UiOptionItem.from(
                    getFullName(account), option -> {
                        AccountSelectionPresenter.instance(getContext()).selectAccount(account);
                        settingsPresenter.closeDialog();
                    }, account.isSelected()
            ));

            if (account.isSelected()) {
                accountName = " (" + getSimpleName(account) + ")";
            }
        }

        settingsPresenter.appendRadioCategory(getContext().getString(R.string.dialog_account_list) + accountName, optionItems);
    }

    private void appendSignOutSection(List<Account> accounts, AppDialogPresenter settingsPresenter) {
        if (accounts == null || accounts.isEmpty()) {
            return;
        }

        List<UiOptionItem> optionItems = new ArrayList<>();

        for (Account account : accounts) {
            optionItems.add(UiOptionItem.from(
                    getFullName(account), option ->
                        AppDialogUtil.showConfirmationDialog(
                                getContext(), getContext().getString(R.string.dialog_remove_account), () -> {
                                    removeAccount(account);
                                    settingsPresenter.closeDialog();
                                    MessageHelpers.showMessage(getContext(), R.string.msg_done);
                                })
            ));
        }

        settingsPresenter.appendStringsCategory(getContext().getString(R.string.dialog_remove_account), optionItems);
    }

    private void appendSeparateSettings(AppDialogPresenter settingsPresenter) {
        settingsPresenter.appendSingleSwitch(UiOptionItem.from(getContext().getString(R.string.multi_profiles),
                option -> {
                    AppPrefs.instance(getContext()).enableMultiProfiles(option.isSelected());
                    BrowsePresenter.instance(getContext()).updateSections();
                },
                AppPrefs.instance(getContext()).isMultiProfilesEnabled()));
    }

    private String getFullName(Account account) {
        String format;

        if (account.getEmail() != null) {
            format = String.format("%s (%s)", account.getName(), account.getEmail());
        } else {
            format = account.getName();
        }

        return format;
    }

    private String getSimpleName(Account account) {
        return account.getName() != null ? account.getName() : account.getEmail();
    }

    private void removeAccount(Account account) {
        getSignInService().removeAccount(account);
        BrowsePresenter.instance(getContext()).refresh(false);
    }

}
