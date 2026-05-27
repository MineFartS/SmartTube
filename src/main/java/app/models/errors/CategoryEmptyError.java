package minefarts.smarttube.app.models.errors;

import android.content.Context;

import androidx.annotation.Nullable;

import minefarts.sharedutils.helpers.Helpers;
import minefarts.smarttube.R;
import minefarts.smarttube.app.presenters.SignInPresenter;
import minefarts.smarttube.utils.Utils;

public class CategoryEmptyError implements ErrorFragmentData {
    private final Context mContext;
    private final Throwable mError;

    public CategoryEmptyError(Context context, @Nullable Throwable error) {
        mContext = context;
        mError = error;
    }

    @Override
    public void onAction() {
        SignInPresenter.instance(mContext).start();
    }

    @Override
    public String getMessage() {
        String result = mContext.getString(R.string.msg_cant_load_content);
        if (mError != null && !Helpers.containsAny(mError.getMessage(), "fromNullable result is null")) {
            String className = mError.getClass().getSimpleName();
            result = String.format("%s: %s", className, Utils.getStackTraceAsString(mError));
            //result = mError.getMessage();
        }
        return result;
    }

    @Override
    public String getActionText() {
        return mError != null && Helpers.startsWith(mError.getMessage(), "AuthError") ? "SIGN IN" : null;
    }
}
