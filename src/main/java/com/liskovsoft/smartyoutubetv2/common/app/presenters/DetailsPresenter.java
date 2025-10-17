package com.liskovsoft.smartyoutubetv2.common.app.presenters;

import android.annotation.SuppressLint;
import android.content.Context;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.base.BasePresenter;
import com.liskovsoft.smartyoutubetv2.common.app.views.DetailsView;
import com.liskovsoft.smartyoutubetv2.common.app.views.ViewManager;

/**
 * Simple presenter that manages the DetailsView.
 *
 * Responsibilities:
 * - Hold a reference to the Video to be shown in the details screen.
 * - Start the DetailsView and deliver the video when the view is ready.
 *
 * This presenter is lightweight and tied to application context (singleton).
 */
public class DetailsPresenter extends BasePresenter<DetailsView> {
    @SuppressLint("StaticFieldLeak")
    private static DetailsPresenter sInstance;

    // Currently selected video to show in the details screen. Stored until view initializes.
    private Video mVideo;

    private DetailsPresenter(Context context) {
        super(context);
    }

    /**
     * Singleton accessor. Creates presenter if needed and updates stored context.
     */
    public static DetailsPresenter instance(Context context) {
        if (sInstance == null) {
            sInstance = new DetailsPresenter(context);
        }

        sInstance.setContext(context);

        return sInstance;
    }

    /**
     * Called when the DetailsView is initialized.
     * Push the stored video into the view so UI can render it.
     */
    @Override
    public void onViewInitialized() {
        getView().openVideo(mVideo);
    }

    /**
     * Request to open details for a given video.
     * Stores the video and ensures the DetailsView is started.
     *
     * @param item Video to display
     */
    public void openVideo(Video item) {
        mVideo = item;
        getViewManager().startView(DetailsView.class);
    }
}
