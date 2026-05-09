package minefarts.smarttube.app.presenters;

import android.annotation.SuppressLint;
import android.content.Context;

import minefarts.smarttube.misc.ServiceManager;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import minefarts.smarttube.app.presenters.dialogs.AccountSelectionPresenter;

import io.reactivex.disposables.Disposable;

public class YTSignInPresenter extends SignInPresenter {
    
    private static final String TAG = YTSignInPresenter.class.getSimpleName();
    
    @SuppressLint("StaticFieldLeak")
    private static YTSignInPresenter sInstance;
    private Disposable mSignInAction;

    private YTSignInPresenter(Context context) {
        super(context);
    }

    public static YTSignInPresenter instance(Context context) {
        if (sInstance == null) {
            sInstance = new YTSignInPresenter(context);
        }

        sInstance.setContext(context);

        return sInstance;
    }

    public void unhold() {
        RxHelper.disposeActions(mSignInAction);
        sInstance = null;
    }

    @Override
    public void onViewDestroyed() {
        super.onViewDestroyed();
        unhold();
    }

    @Override
    public void onViewInitialized() {
        super.onViewInitialized();
        RxHelper.disposeActions(mSignInAction);
        updateUserCode();
    }

    @Override
    public void onActionClicked() {
        if (getView() != null) {
            getView().close();
        }
    }

    private void updateUserCode() {
        mSignInAction = ServiceManager.getSignInService().signInObserve().subscribe(
            userCode -> getView().showCode(userCode, "https://youtube.com/tv/activate"),
            error -> {
                Log.e(TAG, "Sign in error: %s", error.getMessage());
                if (getView() != null) {
                    getView().showCode(error.getMessage(), "");
                }
            },
            () -> {
                // Success
                if (getView() != null) {
                    getView().close();
                }

                AccountSelectionPresenter.instance(getContext()).show(true);
            }
        );
    }

    public void start() {
        super.start();
        RxHelper.disposeActions(mSignInAction);
    }
}
