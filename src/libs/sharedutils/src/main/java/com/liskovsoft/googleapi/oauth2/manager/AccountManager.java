package com.liskovsoft.googleapi.oauth2.manager;

import com.liskovsoft.googleapi.oauth2.OAuth2Service;
import com.liskovsoft.googlecommon.common.models.auth.AccessToken;
import com.liskovsoft.googlecommon.common.models.auth.UserCode;
import com.liskovsoft.googlecommon.service.oauth.YouTubeAccount;
import com.liskovsoft.sharedutils.oauth.Account;
import com.liskovsoft.sharedutils.oauth.SignInCode;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.prefs.GlobalPreferences;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.googlecommon.common.helpers.RetrofitOkHttpHelper;

import java.util.Map;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AccountManager {
    
    private static final String TAG = AccountManager.class.getSimpleName();

    private static final long TOKEN_REFRESH_PERIOD_MS = 60 * 60 * 1_000; // NOTE: auth token max lifetime is 60 min
    
    private static AccountManager sInstance;
    
    private final OAuth2Service mOAuth2Service;
    private UserCode mUserCodeResult;
    private Runnable mOnChange;
    private String mCachedAuthorizationHeader;
    private long mCacheUpdateTime;

    /**
     * Fix ConcurrentModificationException when using {@link #getSelectedAccount()}
     */
    private final List<Account> mAccounts = new CopyOnWriteArrayList<Account>() {
        @Override
        public boolean add(Account account) {
            if (account == null) {
                return false;
            }

            merge(account);

            // Don't remove these lines or you won't be able to enter to the account.
            while (contains(account)) {
                remove(account);
            }

            return super.add(account);
        }

        private void merge(Account account) {
            int index = indexOf(account);

            if (index != -1) {
                Account matched = get(index);
                ((YouTubeAccount) account).merge(matched);
                remove(matched);
            }
        }
    };

    private AccountManager() {
        mOAuth2Service = OAuth2Service.instance();

        GlobalPreferences.setOnInit(() -> {
            init();
            try {
                updateAuthHeadersIfNeeded();
            } catch (Exception e) {
                // Host not found
                e.printStackTrace();
            }
        });
    }

    public static AccountManager instance() {
        if (sInstance == null)
            sInstance = new AccountManager();

        return sInstance;
    }

    /**
     * The code is working limited amount of time. Need to be confirmed instantly.
     */
    public SignInCode getSignInCode() {
        mUserCodeResult = mOAuth2Service.getUserCode();
        return mUserCodeResult != null ? new SignInCode() {
            @Override
            public String getSignInCode() {
                return mUserCodeResult.getUserCode();
            }

            @Override
            public String getSignInUrl() {
                return mUserCodeResult.getVerificationUrl();
            }
        } : null;
    }

    public void waitUserCodeConfirmation() {
        if (mUserCodeResult == null) {
            return;
        }

        try {
            AccessToken token = mOAuth2Service.getAccessTokenWait(mUserCodeResult.getDeviceCode());

            persistRefreshToken(token.getRefreshToken());
        } catch (InterruptedException e) {
            // NOP
        } finally {
            mUserCodeResult = null;
        }
    }

    public List<Account> getAccounts() {
        return mAccounts;
    }

    /**
     * Set selected account token
     */
    private void persistRefreshToken(String refreshToken) {
        if (refreshToken == null) {
            Log.e(TAG, "Refresh token is null");
            return;
        }

        // Create initial account (with only refresh key)
        YouTubeAccount tempAccount = YouTubeAccount.fromToken(refreshToken);
        addAccount(tempAccount);

        //// Use initial account to create auth header
        //checkAuth();
        //
        //// Remove initial account (with only refresh key)
        //removeAccount(tempAccount);
        //
        //List<AccountInt> accountsInt = mOAuth2Service.getAccounts(); // runs under auth header from above
        //
        //if (accountsInt != null) {
        //    for (AccountInt accountInt : accountsInt) {
        //        AccountImpl account = AccountImpl.from(accountInt);
        //        account.setRefreshToken(refreshToken);
        //        addAccount(account);
        //    }
        //}
        //
        //fixSelectedAccount();
        //
        //// Apply merged tokens
        //checkAuth();

        Log.d(TAG, "Success. Refresh token stored successfully in registry: " + refreshToken);
    }

    private void addAccount(Account newAccount) {
        if (newAccount.isSelected()) {
            for (Account account : mAccounts) {
                ((YouTubeAccount) account).setSelected(false);
            }
        }

        mAccounts.add(newAccount);

        persistAccounts();
    }

    public void selectAccount(Account newAccount) {
        if (Helpers.equals(newAccount, getSelectedAccount())) {
            return;
        }

        for (Account account : mAccounts) {
            ((YouTubeAccount) account).setSelected(newAccount != null && newAccount.equals(account));
        }

        persistAccounts();
    }

    public void removeAccount(Account account) {
        if (account != null && mAccounts.contains(account)) {
            mAccounts.remove(account);
            persistAccounts();
        }
    }

    @Override
    public Account getSelectedAccount() {
        for (Account account : mAccounts) {
            if (account != null && account.isSelected()) {
                return account;
            }
        }

        return null;
    }

    private void persistAccounts() {
        setAccountManagerData(Helpers.mergeArray(mAccounts.toArray()));

        invalidateCache();

        if (mOnChange != null) {
            // Fix sign in bug
            RxHelper.runUser(mOnChange);
        }
    }

    private void restoreAccounts() {
        String data = getAccountManagerData();

        if (data != null) {
            String[] split = Helpers.splitArray(data);
            mAccounts.clear();

            for (String spec : split) {
                mAccounts.add(YouTubeAccount.from(spec));
            }
        }

        if (mOnChange != null) {
            mOnChange.run();
        }
    }

    private void setAccountManagerData(String data) {
        // We don't have context, so can't create instance here.
        // Let's hope someone already created one for us.
        if (GlobalPreferences.sInstance == null) {
            Log.e(TAG, "GlobalPreferences is null!");
            return;
        }

        GlobalPreferences.sInstance.setOAuth2AccountData(data);
    }

    private String getAccountManagerData() {
        // We don't have context, so can't create instance here.
        // Let's hope someone already created one for us.
        if (GlobalPreferences.sInstance == null) {
            Log.e(TAG, "GlobalPreferences is null!");
            return null;
        }

        return GlobalPreferences.sInstance.getOAuth2AccountData();
    }

    public void init() {
        restoreAccounts();
    }

    public void setOnChange(Runnable onChange) {
        mOnChange = onChange;
    }

    @Override
    protected AccessToken obtainAccessToken(String refreshToken) {
        // We don't have context, so can't create instance here.
        // Let's hope someone already created one for us.
        if (GlobalPreferences.sInstance == null) {
            Log.e(TAG, "GlobalPreferences is null!");
            return null;
        }

        AccessToken token = null;

        if (refreshToken != null) {
            token = mOAuth2Service.updateAccessToken(refreshToken);
        }

        return token;
    }

    public void checkAuth() {

        if (mCachedAuthorizationHeader != null 
            && Helpers.equals(mCachedAuthorizationHeader, RetrofitOkHttpHelper.getAuthHeaders().get("Authorization"))
            && System.currentTimeMillis() - mCacheUpdateTime < TOKEN_REFRESH_PERIOD_MS
        ) return;

        Account account = getSelectedAccount();

        String refreshToken = account != null ? ((YouTubeAccount) account).getRefreshToken() : null;
        
        AccessToken accessToken = null;
        if (GlobalPreferences.sInstance != null && refreshToken != null) {
            accessToken = getAuthService().updateAccessToken(refreshToken);
        }

        if (accessToken != null) {
            mCachedAuthorizationHeader = accessToken.getTokenType() + " " + accessToken.getAccessToken();
        } else {
            mCachedAuthorizationHeader = null;
            Log.e(TAG, "Access token is null!");
        }

        Map<String, String> headers = RetrofitOkHttpHelper.getAuthHeaders();
        headers.clear();

        if (mCachedAuthorizationHeader != null && account != null) {

            headers.put("Authorization", mCachedAuthorizationHeader);
            
            String pageIdToken = ((YouTubeAccount) account).getPageIdToken();
            
            if (pageIdToken != null) {
                // Apply branded account rights (restricted videos). Branded refresh token with current account page id.
                headers.put("X-Goog-Pageid", pageIdToken);
            }

        }

        mAccountManager.syncStorage();

        mCacheUpdateTime = System.currentTimeMillis();

    }

    /**
     * For testing purposes
     */
    public void setAuthorizationHeader(String authorizationHeader) {

        mCachedAuthorizationHeader = authorizationHeader;
        mCacheUpdateTime = System.currentTimeMillis();

        Map<String, String> headers = RetrofitOkHttpHelper.getAuthHeaders();
        headers.clear();

        if (mCachedAuthorizationHeader != null && getSelectedAccount() != null) {
            headers.put("Authorization", mCachedAuthorizationHeader);
            String pageIdToken = ((YouTubeAccount) getSelectedAccount()).getPageIdToken();
            if (pageIdToken != null) {
                // Apply branded account rights (restricted videos). Branded refresh token with current account page id.
                headers.put("X-Goog-Pageid", pageIdToken);
            }
        }

    }

    public void invalidateCache() {
        mCachedAuthorizationHeader = null;
    }

}
