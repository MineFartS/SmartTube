package com.liskovsoft.googleapi.oauth2.impl;

import androidx.annotation.Nullable;

import com.liskovsoft.googleapi.oauth2.manager.OAuth2AccountManager;
import com.liskovsoft.sharedutils.oauth.Account;
import com.liskovsoft.sharedutils.oauth.SignInCode;
import com.liskovsoft.sharedutils.rx.RxHelper;

import java.util.List;

import io.reactivex.Observable;

public class GoogleSignInService {

    private static GoogleSignInService sInstance;
    private final OAuth2AccountManager mAccountManager;

    private GoogleSignInService() {
        mAccountManager = OAuth2AccountManager.instance();
    }

    public static GoogleSignInService instance() {
        if (sInstance == null) {
            sInstance = new GoogleSignInService();
        }

        return sInstance;
    }

    public Observable<SignInCode> signInObserve() {
        return RxHelper.createLong(emitter -> {
            SignInCode signInCode = mAccountManager.getSignInCode();

            if (signInCode == null) {
                RxHelper.onError(emitter, "User code result is empty");
                return;
            }

            emitter.onNext(signInCode);

            mAccountManager.waitUserCodeConfirmation();

            emitter.onComplete();
        });
    }

    public void signOut() {
        // TODO: not implemented
    }

    public Observable<Void> signOutObserve() {
        return RxHelper.create(emitter -> {
            signOut();
            emitter.onComplete();
        });
    }

    public boolean isSigned() {
        // Condition created for the case when a device in offline mode.
        return mAccountManager.getSelectedAccount() != null;
    }

    public Observable<Boolean> isSignedObserve() {
        return RxHelper.fromCallable(this::isSigned);
    }

    public List<Account> getAccounts() {
        return mAccountManager.getAccounts();
    }

    public Observable<List<Account>> getAccountsObserve() {
        return RxHelper.fromCallable(this::getAccounts);
    }

    @Nullable
    public Account getSelectedAccount() {
        return mAccountManager.getSelectedAccount();
    }

    public void selectAccount(Account account) {
        mAccountManager.selectAccount(account);
    }

    public void removeAccount(Account account) {
        mAccountManager.removeAccount(account);
    }

    public void setOnChange(Runnable onChange) {
        mAccountManager.setOnChange(onChange);
    }

    public void checkAuth() {
        mAccountManager.checkAuth();
    }
}
