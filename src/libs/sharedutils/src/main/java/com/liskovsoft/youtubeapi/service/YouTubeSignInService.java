package com.liskovsoft.sharedutils.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liskovsoft.sharedutils.SignInService;
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

import io.reactivex.Observable;

import java.util.List;
import java.util.Map;

public class YouTubeSignInService implements SignInService {

    private static YouTubeSignInService sInstance;

    private final YouTubeAccountManager mAccountManager;
    private final OAuth2AccountManager mOAuthManager;
    private String mCachedAuthorizationHeader;
    private long mCacheUpdateTime;

    private YouTubeSignInService() {

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

    public static YouTubeSignInService instance() {
        if (sInstance == null) {
            sInstance = new YouTubeSignInService();
        }

        return sInstance;
    }

    @Override
    public Observable<String> signInObserve() {
        return mAccountManager.signInObserve();
    }

    public void checkAuth() {

        mOAuthManager.checkAuth();
        mCachedAuthorizationHeader = mOAuthManager.getAuthorizationHeader();

        mAccountManager.syncStorage();

    }

    @Override
    public boolean isSigned() {
        // Condition created for the case when a device in offline mode.
        return mAccountManager.getSelectedAccount() != null;
    }

    @Override
    public List<Account> getAccounts() {
        return mAccountManager.getAccounts();
    }

    @Nullable
    @Override
    public Account getSelectedAccount() {
        return mAccountManager.getSelectedAccount();
    }

    public void invalidateCache() {
        mCachedAuthorizationHeader = null;
        mCacheUpdateTime = 0;
    }

    // Fix empty content when quickly switch accounts???
    @Override
    public synchronized void selectAccount(Account account) {
        mAccountManager.selectAccount(account);
    }

    @Override
    public synchronized void removeAccount(Account account) {
        mAccountManager.removeAccount(account);
    }

    @Override
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

    @Override
    public void addOnAccountChange(OnAccountChange listener) {
        mAccountManager.addOnAccountChange(listener);
    }
    
}
