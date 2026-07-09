package minefarts.smarttube.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import minefarts.smarttube.utils.oauth.Account;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.prefs.GlobalPreferences;
import minefarts.smarttube.utils.auth.V2.AuthService;
import minefarts.smarttube.google.common.models.auth.AccessToken;
import minefarts.smarttube.google.common.helpers.RetrofitOkHttpHelper;
import minefarts.smarttube.google.service.oauth.YouTubeAccount;
import minefarts.smarttube.utils.oauth.SignInCode;
import minefarts.smarttube.utils.rx.RxHelper;
import minefarts.smarttube.utils.service.internal.MediaServicePrefs;
import minefarts.smarttube.utils.oauth.Account;
import minefarts.smarttube.utils.misc.WeakHashSet;
import minefarts.smarttube.utils.app.AppService;
import minefarts.smarttube.google.common.models.auth.UserCode;
import minefarts.smarttube.google.common.models.auth.info.AccountInt;
import minefarts.smarttube.utils.videoinfo.V2.VideoInfoService;
import minefarts.smarttube.utils.oauth.OAuth2Api;
import minefarts.smarttube.google.common.helpers.RetrofitHelper;
import minefarts.smarttube.CacheManager;

import io.reactivex.Observable;

import retrofit2.Call;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class SignInService {

    public interface OnAccountChange {
        void onAccountChanged(Account account);
    }

    private static final String TAG = SignInService.class.getSimpleName();

    public static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:device_code";
    public static final String GRANT_TYPE_REFRESH = "refresh_token";

    public static final String DRIVE_SCOPE = "https://www.googleapis.com/auth/drive.file";
    public static final String YOUTUBE_SCOPE = "https://www.googleapis.com/auth/youtube https://www.googleapis.com/auth/youtube.readonly";
    public static final String SIGN_IN_DATA_SCOPE = "email openid profile";
    
    private static final int REFRESH_TOKEN_ATTEMPTS = 200;
    private static final long REFRESH_TOKEN_ATTEMPT_INTERVAL_MS = 5_000;

    private static SignInService sInstance;

    private static final long TOKEN_REFRESH_PERIOD_MS = 60 * 60 * 1_000; // NOTE: auth token max lifetime is 60 min

    private final WeakHashSet<OnAccountChange> mListeners = new WeakHashSet<>();
    public long mCacheUpdateTime;
    private boolean mStorageSynced;
    private UserCode mUserCodeResult;
    private Runnable mOnChange;

    private final OAuth2Api mOAuth2Api;
    private final AppService mAppService;

    private SignInService() {

        mOAuth2Api = RetrofitHelper.create(OAuth2Api.class);
        mAppService = AppService.instance();

        GlobalPreferences.setOnInit(() -> {
            init();
            try {
                checkAuth();
            } catch (Exception e) {
                // Host not found
                e.printStackTrace();
            }
        });

    }

    public static SignInService instance() {
        if (sInstance == null)
            sInstance = new SignInService();

        return sInstance;
    }

    public Observable<SignInCode> signInObserve2() {
        return RxHelper.createLong(emitter -> {
            SignInCode signInCode = getSignInCode();

            if (signInCode == null) {
                RxHelper.onError(emitter, "User code result is empty");
                return;
            }

            emitter.onNext(signInCode);

            waitUserCodeConfirmation();

            emitter.onComplete();
        });
    }

    public boolean isSigned() {
        // Condition created for the case when a device in offline mode.
        return getSelectedAccount() != null;
    }

    public Observable<Boolean> isSignedObserve() {
        return RxHelper.fromCallable(this::isSigned);
    }

    public Observable<List<Account>> getAccountsObserve() {
        return RxHelper.fromCallable(this::getAccounts);
    }


    /**
     * Fix ConcurrentModificationException when using {@link #getSelectedAccount()}
     */
    private final List<Account> mAccounts = new CopyOnWriteArrayList<Account>() {
        @Override
        public boolean add(Account account) {
            if (account == null) {
                return false;
            }

            mergeAndRemove(account);

            return super.add(account);
        }

        private void mergeAndRemove(Account account) {
            int index = indexOf(account);

            if (index != -1) {
                Account matched = get(index);

                // Don't remove these lines or you won't be able to enter to the account.
                while (contains(account)) {
                    remove(account);
                }

                // Do merge after the remove not before!!!
                ((YouTubeAccount) account).merge(matched);
            }
        }
    };

    public Observable<String> signInObserve() {
        return RxHelper.createLong(emitter -> {
            UserCode userCodeResult = getAuthService().getUserCode();

            if (userCodeResult == null) {
                RxHelper.onError(emitter, "User code result is empty");
                return;
            }

            emitter.onNext(userCodeResult.getUserCode());

            try {
                AccessToken token = getAuthService().getAccessTokenWait(userCodeResult.getDeviceCode());

                persistRefreshToken(token.getRefreshToken());

                emitter.onComplete();
            } catch (InterruptedException e) {
                // NOP
            }
        });
    }

    @Nullable
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

        // Use initial account to create auth header and fetch the accounts below
        checkAuth();

        // Remove initial account (with only refresh key)
        mAccounts.remove(tempAccount); // multi thread fix

        List<AccountInt> accountsInt = getAuthService().getAccounts(); // runs under auth header from above

        if (accountsInt != null) {
            for (AccountInt accountInt : accountsInt) {
                YouTubeAccount account = YouTubeAccount.from(accountInt);
                account.setRefreshToken(refreshToken);
                addAccount(account);
            }
        }

        fixSelectedAccount();

        // Apply merged tokens
        checkAuth();

        persistAccounts();
        onAccountChanged();
    }

    private void addAccount(Account newAccount) {
        if (newAccount.isSelected()) {
            for (Account account : mAccounts) {
                ((YouTubeAccount) account).setSelected(false);
            }
        }

        mAccounts.add(newAccount);
    }

    public void selectAccount(Account newAccount) {
        if (Helpers.equals(newAccount, getSelectedAccount())) return;

        for (Account account : mAccounts) {
            ((YouTubeAccount) account).setSelected(newAccount != null && newAccount.equals(account));
        }

        persistAccounts();

        onAccountChanged();
    }

    public void removeAccount(Account account) {
        if (account != null && mAccounts.contains(account)) {
            mAccounts.remove(account);
            persistAccounts();

            onAccountChanged();
        }
    }

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
    }

    private void setAccountManagerData(String data) {
        // We don't have context, so can't create instance here.
        // Let's hope someone already created one for us.
        if (GlobalPreferences.sInstance == null) {
            Log.e(TAG, "GlobalPreferences is null!");
            return;
        }

        GlobalPreferences.sInstance.setMediaServiceAccountData(data);
    }

    private String getAccountManagerData() {
        // We don't have context, so can't create instance here.
        // Let's hope someone already created one for us.
        if (GlobalPreferences.sInstance == null) {
            Log.e(TAG, "GlobalPreferences is null!");
            return null;
        }

        return GlobalPreferences.sInstance.getMediaServiceAccountData();
    }

    public void init() {
        String data = getAccountManagerData();

        if (data != null) {
            String[] split = Helpers.splitArray(data);
            mAccounts.clear();

            for (String spec : split) {
                mAccounts.add(YouTubeAccount.from(spec));
            }
        }
    }

    public void addOnAccountChange(OnAccountChange listener) {
        if (!mListeners.contains(listener)) {
            if (listener instanceof MediaServicePrefs) {
                mListeners.add(0, listener);
            } else {
                mListeners.add(listener);
            }
        }
    }

    /**
     * Fix situations when there is no selected account<br/>
     * Mark first one as selected.
     */
    private void fixSelectedAccount() {
        if (mAccounts.isEmpty()) return;

        if (getSelectedAccount() == null) {
            selectAccount(mAccounts.get(0));
        }
    }

    private void onAccountChanged() {
        CacheManager.clear();
        notifyListeners();
    }

    /**
     * Sync avatars, names and emails
     */
    public void syncStorage() {
        if (mStorageSynced) return;

        List<Account> storedAccounts = getAccounts();

        if (storedAccounts != null && !storedAccounts.isEmpty()) {
            List<AccountInt> newAccounts = getAuthService().getAccounts();

            Account selectedAccount = getSelectedAccount();

            if (newAccounts != null) {
                for (AccountInt newAccount : newAccounts) {
                    YouTubeAccount account = YouTubeAccount.from(newAccount);
                    account.setSelected(account.equals(selectedAccount));
                    addAccount(account);
                }
                persistAccounts();
            }
        }

        mStorageSynced = true;
    }

    private void notifyListeners() {
        Account account = getSelectedAccount();

        // Fix sign in bug
        mListeners.forEach(listener -> {
            if (listener instanceof MediaServicePrefs) {
                listener.onAccountChanged(account);
            } else {
                RxHelper.runUser(() -> listener.onAccountChanged(account));
            }
        });
    }

    @NonNull
    private static AuthService getAuthService() {
        return AuthService.instance();
    }

    /**
     * The code is working limited amount of time. Need to be confirmed instantly.
     */
    public SignInCode getSignInCode() {

        mUserCodeResult = getUserCode();
        
        if (mUserCodeResult != null) {
            return new SignInCode(mUserCodeResult);
        } else {
            return null;
        }

    }

    public void waitUserCodeConfirmation() {
        if (mUserCodeResult == null) return;

        try {
            AccessToken token = getAccessTokenWait(mUserCodeResult.getDeviceCode());

            persistRefreshToken(token.getRefreshToken());
        } catch (InterruptedException e) {
            // NOP
        } finally {
            mUserCodeResult = null;
        }
    }

    public void setOnChange(Runnable onChange) {
        mOnChange = onChange;
    }

    public void checkAuth() {

        if (getAuthorizationHeader() != null 
            && System.currentTimeMillis() - mCacheUpdateTime < TOKEN_REFRESH_PERIOD_MS
        ) return;

        Account account = getSelectedAccount();
        
        String refreshToken = account != null ? ((YouTubeAccount) account).getRefreshToken() : null;

        String authHeader = null;

        if (GlobalPreferences.sInstance != null) {
    
            AccessToken token = null;

            if (refreshToken != null)
                token = updateAccessToken(refreshToken);

            if (token != null)
                authHeader = String.format("%s %s", token.getTokenType(), token.getAccessToken());

        }

        setAuthorizationHeader(authHeader);

        syncStorage();

    }

    public String getAuthorizationHeader() {
        return RetrofitOkHttpHelper.getAuthHeaders().get("Authorization");
    }

    public void setAuthorizationHeader(String authorizationHeader) {
;
        mCacheUpdateTime = System.currentTimeMillis();

        Map<String, String> headers = RetrofitOkHttpHelper.getAuthHeaders();
        headers.clear();

        if (getSelectedAccount() != null) {
            headers.put("Authorization", authorizationHeader);
            String pageIdToken = ((YouTubeAccount) getSelectedAccount()).getPageIdToken();
            if (pageIdToken != null) {
                // Apply branded account rights (restricted videos). Branded refresh token with current account page id.
                headers.put("X-Goog-Pageid", pageIdToken);
            }
        }

    }

/**
     * Returns user code that user should apply on the page<br/>
     * <a href=https://youtube.com/activate>https://youtube.com/activate</a>
     * @return response with user code and device code
     */
    public UserCode getUserCode() {
        Call<UserCode> wrapper = mOAuth2Api.getUserCode(
            mAppService.getClientId(), 
            DRIVE_SCOPE
        );
        return RetrofitHelper.get(wrapper);
    }

    /**
     * Note, before calling this method user should apply the 'user code' on the page<br/>
     * <a href=https://youtube.com/activate>https://youtube.com/activate</a>
     * @param deviceCode the code contained inside the response of the method {@link #getUserCode()}
     * @return refresh token that should be stored inside the app registry for future use
     */
    private AccessToken getAccessToken(String deviceCode) {
        
        Call<AccessToken> wrapper = mOAuth2Api.getAccessToken(
            mAppService.getClientId(), 
            mAppService.getClientSecret(), 
            deviceCode, 
            GRANT_TYPE
        );

        return RetrofitHelper.get(wrapper);
    }

    /**
     * Returns temporal access token that should be refreshed after some period of time
     * @param refreshToken token obtained from previous method
     * @return temporal access token
     */
    public AccessToken updateAccessToken(String refreshToken) {
        
        Call<AccessToken> wrapper = mOAuth2Api.updateAccessToken(
            mAppService.getClientId(), 
            mAppService.getClientSecret(), 
            GRANT_TYPE_REFRESH, 
            refreshToken
        );

        return RetrofitHelper.get(wrapper, true, true);
    }

    public AccessToken getAccessTokenWait(String deviceCode) throws InterruptedException {
        AccessToken tokenResult = null;

        for (int i = 0; i < REFRESH_TOKEN_ATTEMPTS; i++) {
            Thread.sleep(REFRESH_TOKEN_ATTEMPT_INTERVAL_MS);

            tokenResult = getAccessToken(deviceCode);

            if (tokenResult != null && tokenResult.getRefreshToken() != null) {
                break;
            }
        }

        if (tokenResult != null && tokenResult.getRefreshToken() != null) {
            return tokenResult;
        } else {
            String msg = String.format("Error. Refresh token is empty!\nDevice code: %s\nError msg: %s",
                deviceCode,
                tokenResult != null ? tokenResult.getError() : ""
            );

            Log.e(TAG, msg);
            throw new IllegalStateException(msg);
        }
    }

}
