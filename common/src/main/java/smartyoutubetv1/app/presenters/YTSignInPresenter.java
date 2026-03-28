package smartyoutubetv1.app.presenters;

import android.annotation.SuppressLint;
import android.content.Context;

import com.liskovsoft.mediaserviceinterfaces.ServiceManager;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import smartyoutubetv1.app.presenters.dialogs.AccountSelectionPresenter;
import com.liskovsoft.youtubeapi.service.YouTubeServiceManager;

import io.reactivex.disposables.Disposable;

public class YTSignInPresenter extends SignInPresenter {
    
    private static final String TAG = YTSignInPresenter.class.getSimpleName();
    
    @SuppressLint("StaticFieldLeak")
    private static YTSignInPresenter sInstance;
    private final ServiceManager mService;
    private Disposable mSignInAction;

    private YTSignInPresenter(Context context) {
        super(context);
        mService = YouTubeServiceManager.instance();
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
        mSignInAction = mService.getSignInService().signInObserve().subscribe(
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
