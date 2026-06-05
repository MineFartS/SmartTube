package minefarts.smarttube.app.presenters;

import android.annotation.SuppressLint;
import android.content.Context;

import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.app.views.SignInView;
import minefarts.smarttube.utils.ServiceManager;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.rx.RxHelper;
import minefarts.smarttube.app.presenters.dialogs.AccountSelectionPresenter;
import minefarts.smarttube.utils.SignInService;

import io.reactivex.disposables.Disposable;

public class SignInPresenter extends BasePresenter<SignInView> {
    
    private static final String TAG = SignInPresenter.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static SignInPresenter sInstance;
    private boolean mIsWaiting;

    private final SignInService mSignInService;
    private Disposable mSignInAction;
    private Runnable mCallback;

    protected SignInPresenter(Context context) {
        super(context);
        mSignInService = SignInService.instance();
    }

    public static SignInPresenter instance(Context context) {
        if (sInstance == null) {
            sInstance = new SignInPresenter(context);
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
        
        if (this.getClass() != SignInPresenter.class) {
            doWait(false);
            return;
        }

        setView(getView());

        RxHelper.disposeActions(mSignInAction);
        updateUserCode();
    }

    public void onActionClicked() {
        
        if (getView() != null) {
            getView().close();
        }

    }

    private void doWait(boolean doWait) {
        mIsWaiting = doWait;
    }

    protected final boolean isWaiting() {
        return mIsWaiting;
    }

    public void start() {
        getViewManager().startView(SignInView.class);
        doWait(true);
        RxHelper.disposeActions(mSignInAction);
    }

    public void start(Runnable onSuccess) {
        start();
        mCallback = onSuccess;
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

}
