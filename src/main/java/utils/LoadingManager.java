package SmartTubeApp.utils;

import android.content.Context;
import SmartTubeApp.app.presenters.BrowsePresenter;
import SmartTubeApp.app.presenters.ChannelPresenter;
import SmartTubeApp.app.presenters.ChannelUploadsPresenter;
import SmartTubeApp.app.presenters.PlaybackPresenter;
import SmartTubeApp.app.presenters.SearchPresenter;
import SmartTubeApp.app.views.BrowseView;
import SmartTubeApp.app.views.ChannelUploadsView;
import SmartTubeApp.app.views.ChannelView;
import SmartTubeApp.app.models.playback.PlayerEngine;
import SmartTubeApp.app.views.SearchView;
import SmartTubeApp.app.views.ViewManager;

public class LoadingManager {
    public static void showLoading(Context context, boolean show, Class<?> view) {
        Class<?> topView = ViewManager.instance(context).getTopView();
        if (topView == view) {
            showLoading(context, show);
        }
    }

    public static void showLoading(Context context, boolean show) {
        Class<?> topView = ViewManager.instance(context).getTopView();

        if (topView == BrowseView.class) {
            BrowseView browseView = BrowsePresenter.instance(context).getView();
            if (browseView != null) {
                browseView.showProgressBar(show);
            }
        } else if (topView == SearchView.class) {
            SearchView searchView = SearchPresenter.instance(context).getView();
            if (searchView != null) {
                searchView.showProgressBar(show);
            }
        } else if (topView == ChannelView.class) {
            ChannelView channelView = ChannelPresenter.instance(context).getView();
            if (channelView != null) {
                channelView.showProgressBar(show);
            }
        } else if (topView == ChannelUploadsView.class) {
            ChannelUploadsView uploadsView = ChannelUploadsPresenter.instance(context).getView();
            if (uploadsView != null) {
                uploadsView.showProgressBar(show);
            }
        } else if (topView == PlayerEngine.class) {
            PlayerEngine playbackView = PlaybackPresenter.instance(context).getView();
            if (playbackView != null) {
                playbackView.showProgressBar(show);
            }
        }
    }
}
