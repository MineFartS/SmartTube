package smartyoutubetv1.utils;

import android.content.Context;
import smartyoutubetv1.app.presenters.BrowsePresenter;
import smartyoutubetv1.app.presenters.ChannelPresenter;
import smartyoutubetv1.app.presenters.ChannelUploadsPresenter;
import smartyoutubetv1.app.presenters.PlaybackPresenter;
import smartyoutubetv1.app.presenters.SearchPresenter;
import smartyoutubetv1.app.views.BrowseView;
import smartyoutubetv1.app.views.ChannelUploadsView;
import smartyoutubetv1.app.views.ChannelView;
import smartyoutubetv1.app.views.PlaybackView;
import smartyoutubetv1.app.views.SearchView;
import smartyoutubetv1.app.views.ViewManager;

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
        } else if (topView == PlaybackView.class) {
            PlaybackView playbackView = PlaybackPresenter.instance(context).getView();
            if (playbackView != null) {
                playbackView.showProgressBar(show);
            }
        }
    }
}
