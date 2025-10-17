package com.liskovsoft.smartyoutubetv2.common.app.models.errors;

import android.content.Context;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.YTSignInPresenter;

/**
 * Error data used to prompt the user to sign in when required.
 * Supplies a localized message and an action that starts the sign-in flow.
 */
public class SignInError implements ErrorFragmentData {
    // Application context used to obtain localized strings and presenters
    private final Context mContext;

    /**
     * Create a SignInError instance.
     *
     * @param context application context
     */
    public SignInError(Context context) {
        mContext = context;
    }

    /**
     * Action invoked when the user taps the action button — start sign-in flow.
     */
    @Override
    public void onAction() {
        YTSignInPresenter.instance(mContext).start();
    }

    /**
     * Localized message describing why sign-in is needed.
     *
     * @return message string
     */
    @Override
    public String getMessage() {
        return mContext.getString(R.string.library_signin_to_show_more);
    }

    /**
     * Localized text for the action button (e.g. "Sign in").
     *
     * @return action text string
     */
    @Override
    public String getActionText() {
        return mContext.getString(R.string.action_signin);
    }
}
