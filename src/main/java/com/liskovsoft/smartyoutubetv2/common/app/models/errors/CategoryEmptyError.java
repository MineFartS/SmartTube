package com.liskovsoft.smartyoutubetv2.common.app.models.errors;

import android.content.Context;

import androidx.annotation.Nullable;

import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.YTSignInPresenter;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils;

/**
 * Error data used to populate an error fragment when a category is empty or cannot be loaded.
 * Provides a user-visible message and an optional action (e.g. prompt sign-in).
 */
public class CategoryEmptyError implements ErrorFragmentData {
    // Application context used to obtain localized strings/resources
    private final Context mContext;
    // Optional underlying throwable that caused the error (may be null)
    private final Throwable mError;

    /**
     * Construct an error representation for an empty/failed category load.
     *
     * @param context application context for resources
     * @param error   optional underlying error (nullable)
     */
    public CategoryEmptyError(Context context, @Nullable Throwable error) {
        mContext = context;
        mError = error;
    }

    /**
     * Action invoked when the user taps the error action button.
     * For authentication-related errors this starts the sign-in flow.
     */
    @Override
    public void onAction() {
        YTSignInPresenter.instance(mContext).start();
    }

    /**
     * Build a user-visible message for the error fragment.
     * If an underlying error is available and it's not a trivial "fromNullable result is null" message,
     * include detailed class and stack trace information for diagnostics.
     *
     * @return error message string
     */
    @Override
    public String getMessage() {
        String result = mContext.getString(R.string.msg_cant_load_content);
        if (mError != null && !Helpers.containsAny(mError.getMessage(), "fromNullable result is null")) {
            String className = mError.getClass().getSimpleName();
            // Use full stack trace string to provide useful debug information in the UI/logs
            result = String.format("%s: %s", className, Utils.getStackTraceAsString(mError));
            //result = mError.getMessage();
        }
        return result;
    }

    /**
     * Provide text for the action button when applicable.
     * If the underlying error message starts with "AuthError" the action is to sign in.
     *
     * @return localized action text or null if no action should be shown
     */
    @Override
    public String getActionText() {
        return mError != null && Helpers.startsWith(mError.getMessage(), "AuthError") ? mContext.getString(R.string.action_signin) : null;
    }
}
