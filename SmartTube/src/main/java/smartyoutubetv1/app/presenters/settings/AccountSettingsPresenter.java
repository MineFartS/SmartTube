package SmartTubeApp.app.presenters.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import com.liskovsoft.sharedutils.oauth.Account;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import SmartTubeApp.R;
import SmartTubeApp.app.models.playback.ui.OptionItem;
import SmartTubeApp.app.models.playback.ui.UiOptionItem;
import SmartTubeApp.app.presenters.AppDialogPresenter;
import SmartTubeApp.app.presenters.BrowsePresenter;
import SmartTubeApp.app.presenters.YTSignInPresenter;
import SmartTubeApp.app.presenters.base.BasePresenter;
import SmartTubeApp.app.presenters.dialogs.AccountSelectionPresenter;
import SmartTubeApp.misc.MediaServiceManager;
import SmartTubeApp.prefs.AccountsData;
import SmartTubeApp.prefs.AppPrefs;
import SmartTubeApp.utils.AppDialogUtil;
import SmartTubeApp.utils.SimpleEditDialog;
import SmartTubeApp.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class AccountSettingsPresenter extends BasePresenter<Void> {

    @SuppressLint("StaticFieldLeak")
    private static AccountSettingsPresenter sInstance;

    private final MediaServiceManager mMediaServiceManager;

    public AccountSettingsPresenter(Context context) {
        super(context);
        mMediaServiceManager = MediaServiceManager.instance();
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
        mMediaServiceManager.loadAccounts(this::createAndShowDialog);
    }

    private void createAndShowDialog(List<Account> accounts) {
        
        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

        AccountsData accountsData = AccountsData.instance(getContext());

        appendSelectAccountSection(accounts, settingsPresenter);
        
        settingsPresenter.appendSingleButton(
            UiOptionItem.from(
                getContext().getString(R.string.dialog_add_account), 
                option -> YTSignInPresenter.instance(getContext()).start()
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

        List<OptionItem> optionItems = new ArrayList<>();

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

        List<OptionItem> optionItems = new ArrayList<>();

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
