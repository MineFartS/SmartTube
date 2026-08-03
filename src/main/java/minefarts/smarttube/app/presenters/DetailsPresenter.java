package minefarts.smarttube.app.presenters;

import android.annotation.SuppressLint;
import android.content.Context;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.app.views.DetailsView;
import minefarts.smarttube.app.views.ViewManager;

public class DetailsPresenter extends BasePresenter<DetailsView> {

    @SuppressLint("StaticFieldLeak")
    private static DetailsPresenter sInstance;

    private Video mVideo;

    public static DetailsPresenter instance(Context context) {
        if (sInstance == null)
            sInstance = new DetailsPresenter();

        sInstance.setContext(context);

        return sInstance;
    }

    @Override
    public void onViewInitialized() {
        getView().openVideo(mVideo);
    }

    public void openVideo(Video item) {
        mVideo = item;
        getViewManager().startView(DetailsView.class);
    }
}
