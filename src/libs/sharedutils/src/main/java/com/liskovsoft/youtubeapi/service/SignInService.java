package com.liskovsoft.sharedutils.service;

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
import com.liskovsoft.googleapi.oauth2.manager.AccountManager;
import io.reactivex.Observable;

import java.util.List;
import java.util.Map;

public class SignInService {

    private static final String TAG = SignInService.class.getSimpleName();
    
    private static final long TOKEN_REFRESH_PERIOD_MS = 60 * 60 * 1_000; // NOTE: auth token max lifetime is 60 min
    
    private static SignInService sInstance;
    private final AccountManager mAccountManager;
    private String mCachedAuthorizationHeader;
    private long mCacheUpdateTime;

    private SignInService() {

        mAccountManager = AccountManager.instance();

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

    public interface OnAccountChange {
        void onAccountChanged(Account account);
    }

    public void checkAuth() {
        mAccountManager.checkAuth();
    }

    public void addOnAccountChange(OnAccountChange listener) {
        mAccountManager.addOnAccountChange(listener);
    }

    @NonNull
    private static AuthService getAuthService() {
        return AuthService.instance();
    }

}
