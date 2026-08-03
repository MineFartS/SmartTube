package minefarts.smarttube.app.models.errors;

import android.content.Context;
import minefarts.smarttube.R;
import minefarts.smarttube.app.presenters.SignInPresenter;

public class SignInError implements ErrorFragmentData {
    private final Context mContext;

    public SignInError(Context context) {
        mContext = context;
    }

    @Override
    public void onAction() {
        SignInPresenter.instance(mContext).start();
    }

    @Override
    public String getMessage() {
        return "Watch videos you liked, saved, or subscribed";
    }

    @Override
    public String getActionText() {
        return "SIGN IN";
    }
}
