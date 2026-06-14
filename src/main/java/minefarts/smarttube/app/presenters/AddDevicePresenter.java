package minefarts.smarttube.app.presenters;

import android.annotation.SuppressLint;
import android.content.Context;
import minefarts.smarttube.utils.ServiceManager;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.app.views.AddDeviceView;
import minefarts.smarttube.utils.rx.RxHelper;
import io.reactivex.disposables.Disposable;

public class AddDevicePresenter extends BasePresenter<AddDeviceView> {

    private static final String TAG = AddDevicePresenter.class.getSimpleName();
    
    @SuppressLint("StaticFieldLeak")
    private static AddDevicePresenter sInstance;
    private Disposable mDeviceCodeAction;

    public static AddDevicePresenter instance(Context context) {
        if (sInstance == null)
            sInstance = new AddDevicePresenter();

        sInstance.setContext(context);

        return sInstance;
    }

    public void unhold() {
        RxHelper.disposeActions(mDeviceCodeAction);
        sInstance = null;
    }

    @Override
    public void onViewDestroyed() {
        super.onViewDestroyed();
        unhold();
    }

    @Override
    public void onViewInitialized() {
        RxHelper.disposeActions(mDeviceCodeAction);
        updateDeviceCode();
    }

    public void onActionClicked() {
        if (getView() != null) {
            getView().close();
        }
    }

    private void updateDeviceCode() {
        mDeviceCodeAction = ServiceManager.getRemoteControlService().getPairingCodeObserve()
                .subscribe(
                        deviceCode -> getView().showCode(deviceCode),
                        error -> Log.e(TAG, "Get pairing code error: %s", error.getMessage())
                );
    }

    public void start() {
        RxHelper.disposeActions(mDeviceCodeAction);
        getViewManager().startView(AddDeviceView.class);
    }
}
