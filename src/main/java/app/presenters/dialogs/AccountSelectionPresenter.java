package SmartTubeApp.app.presenters.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;

import com.liskovsoft.sharedutils.ServiceManager;
import com.liskovsoft.sharedutils.SignInService;
import com.liskovsoft.sharedutils.oauth.Account;
import SmartTubeApp.R;
import SmartTubeApp.app.models.playback.ui.OptionItem;
import SmartTubeApp.app.models.playback.ui.UiOptionItem;
import SmartTubeApp.app.presenters.AppDialogPresenter;
import SmartTubeApp.app.presenters.base.BasePresenter;
import SmartTubeApp.app.presenters.settings.AccountSettingsPresenter;
import SmartTubeApp.misc.MediaServiceManager;
import SmartTubeApp.prefs.AccountsData;
import SmartTubeApp.prefs.GeneralData;
import SmartTubeApp.utils.Utils;
import com.liskovsoft.sharedutils.service.YouTubeServiceManager;

import java.util.ArrayList;
import java.util.List;

public class AccountSelectionPresenter extends BasePresenter<Void> {
    
    @SuppressLint("StaticFieldLeak")
    private static AccountSelectionPresenter sInstance;
    private final SignInService mSignInService;

    public AccountSelectionPresenter(Context context) {
        super(context);
        ServiceManager service = YouTubeServiceManager.instance();
        mSignInService = service.getSignInService();
    }

    public static AccountSelectionPresenter instance(Context context) {
        if (sInstance == null) {
            sInstance = new AccountSelectionPresenter(context);
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
        MediaServiceManager.instance().loadAccounts(this::nextAccountOrDialog);
    }

    public void unhold() {
        sInstance = null;
    }

    private void createAndShowDialog(List<Account> accounts, boolean force) {
        if (accounts.size() <= 1 && !force) {
            return;
        }

        AppDialogPresenter dialogPresenter = AppDialogPresenter.instance(getContext());

        appendAccountSelection(accounts, dialogPresenter);

        dialogPresenter.showDialog(getContext().getString(R.string.settings_accounts), this::unhold);
    }

    private void appendAccountSelection(List<Account> accounts, AppDialogPresenter settingsPresenter) {
        List<OptionItem> optionItems = new ArrayList<>();

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
