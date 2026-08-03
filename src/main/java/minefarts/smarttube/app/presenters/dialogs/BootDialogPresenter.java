package minefarts.smarttube.app.presenters.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;

import minefarts.smarttube.app.presenters.base.BasePresenter;

/**
 * Shows boot dialogs one by one.
 */
public class BootDialogPresenter extends BasePresenter<Void> {

    @SuppressLint("StaticFieldLeak")
    private static BootDialogPresenter sInstance;

    public static BootDialogPresenter instance(Context context) {
        if (sInstance == null)
            sInstance = new BootDialogPresenter();

        sInstance.setContext(context);

        return sInstance;
    }

    public void start() {
        startUpdatePresenter();
    }

    private void startUpdatePresenter() {
        AppUpdatePresenter updatePresenter = AppUpdatePresenter.instance(getContext());

        updatePresenter.start(false);

    }

}
