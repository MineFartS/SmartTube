package SmartTubeApp.app.models.errors;

import android.content.Context;
import SmartTubeApp.R;
import SmartTubeApp.app.presenters.YTSignInPresenter;

public class SignInError implements ErrorFragmentData {
    private final Context mContext;

    public SignInError(Context context) {
        mContext = context;
    }

    @Override
    public void onAction() {
        YTSignInPresenter.instance(mContext).start();
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
