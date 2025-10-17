package com.liskovsoft.smartyoutubetv2.common.app.models.errors;

import android.content.Context;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.settings.AccountSettingsPresenter;

/**
 * Error data used when a password-related issue occurs for account settings.
 * Provides an action to prompt the user to enter/check their account password.
 */
public class PasswordError implements ErrorFragmentData {
    // Application context used to obtain localized strings/resources and presenters
    private final Context mContext;

    /**
     * Construct a PasswordError with the application context.
     *
     * @param context application context for resources and presenters
     */
    public PasswordError(Context context) {
        mContext = context;
    }

    /**
     * Called when the user taps the error action button.
     * Starts the dialog to check/enter account password.
     */
    @Override
    public void onAction() {
        AccountSettingsPresenter.instance(mContext).showCheckPasswordDialog();
    }

    /**
     * Returns the message to show in the error fragment.
     * Currently there is no specific message for password errors so null is returned.
     * This can be updated to return a localized string if desired.
     *
     * @return message string or null
     */
    @Override
    public String getMessage() {
        return null;
    }

    /**
     * Returns the localized text for the action button that prompts the user
     * to enter their account password.
     *
     * @return localized action text
     */
    @Override
    public String getActionText() {
        return mContext.getString(R.string.enter_account_password);
    }
}
