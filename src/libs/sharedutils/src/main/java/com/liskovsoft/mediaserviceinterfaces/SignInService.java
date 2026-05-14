package com.liskovsoft.sharedutils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liskovsoft.sharedutils.oauth.Account;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.prefs.GlobalPreferences;
import com.liskovsoft.sharedutils.auth.V2.AuthService;
import com.liskovsoft.googlecommon.common.models.auth.AccessToken;
import com.liskovsoft.googlecommon.common.helpers.RetrofitOkHttpHelper;
import com.liskovsoft.googlecommon.service.oauth.YouTubeAccount;
import com.liskovsoft.sharedutils.service.internal.YouTubeAccountManager;
import com.liskovsoft.googleapi.oauth2.manager.OAuth2AccountManager;
import com.liskovsoft.sharedutils.oauth.SignInCode;
import com.liskovsoft.sharedutils.rx.RxHelper;

import io.reactivex.Observable;

import java.util.List;
import java.util.Map;

public class SignInService {

    public interface OnAccountChange {
        void onAccountChanged(Account account);
    }

    private static SignInService sInstance;

    private final YouTubeAccountManager mAccountManager;
    private final OAuth2AccountManager mOAuthManager;
    private String mCachedAuthorizationHeader;
    private long mCacheUpdateTime;

    private SignInService() {

        mAccountManager = YouTubeAccountManager.instance(this);
        mOAuthManager = OAuth2AccountManager.instance();

        GlobalPreferences.setOnInit(() -> {
            mAccountManager.init();
            try {
                checkAuth();
            } catch (Exception e) {
                // Host not found
                e.printStackTrace();
            }
        });

    }

    public static SignInService instance() {
        if (sInstance == null) {
            sInstance = new SignInService();
        }

        return sInstance;
    }

    public Observable<String> signInObserve() {
        return mAccountManager.signInObserve();
    }

    public Observable<SignInCode> signInObserve2() {
        return RxHelper.createLong(emitter -> {
            SignInCode signInCode = mOAuthManager.getSignInCode();

            if (signInCode == null) {
                RxHelper.onError(emitter, "User code result is empty");
                return;
            }

            emitter.onNext(signInCode);

            mOAuthManager.waitUserCodeConfirmation();

            emitter.onComplete();
        });
    }

    public void checkAuth() {

        mOAuthManager.checkAuth();
        mCachedAuthorizationHeader = mOAuthManager.getAuthorizationHeader();

        mAccountManager.syncStorage();

    }

    public boolean isSigned() {
        // Condition created for the case when a device in offline mode.
        return mAccountManager.getSelectedAccount() != null;
    }

    public List<Account> getAccounts() {
        return mAccountManager.getAccounts();
    }

    @Nullable
    public Account getSelectedAccount() {
        return mAccountManager.getSelectedAccount();
    }

    public void invalidateCache() {
        mCachedAuthorizationHeader = null;
        mCacheUpdateTime = 0;
    }

    // Fix empty content when quickly switch accounts???
    public synchronized void selectAccount(Account account) {
        mAccountManager.selectAccount(account);
    }

    public synchronized void removeAccount(Account account) {
        mAccountManager.removeAccount(account);
    }

    public String printDebugInfo() {
        String name = "none";
        String header = "none";
        String token = "none";

        if (mCachedAuthorizationHeader != null) {
            header = "ok";
        }

        Account account = getSelectedAccount();

        if (account instanceof YouTubeAccount) {
            if (account.getName() != null) {
                name = "ok";
            }
            if (((YouTubeAccount) account).getRefreshToken() != null) {
                token = "ok";
            }
        }

        return String.format("name=%s;header=%s;token=%s", name, header, token);
    }

    public void addOnAccountChange(OnAccountChange listener) {
        mAccountManager.addOnAccountChange(listener);
    }

    public Observable<Boolean> isSignedObserve() {
        return RxHelper.fromCallable(this::isSigned);
    }

    public Observable<List<Account>> getAccountsObserve() {
        return RxHelper.fromCallable(this::getAccounts);
    }

}
