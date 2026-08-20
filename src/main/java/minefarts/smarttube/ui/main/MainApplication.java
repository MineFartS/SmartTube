package minefarts.smarttube.ui.main;

import androidx.multidex.MultiDexApplication;

import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.BrowseSection;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.BrowsePresenter;
import com.liskovsoft.smartyoutubetv2.common.app.views.AddDeviceView;
import com.liskovsoft.smartyoutubetv2.common.app.views.AppDialogView;
import com.liskovsoft.smartyoutubetv2.common.app.views.BrowseView;
import com.liskovsoft.smartyoutubetv2.common.app.views.ChannelUploadsView;
import com.liskovsoft.smartyoutubetv2.common.app.views.ChannelView;
import com.liskovsoft.smartyoutubetv2.common.app.views.PlaybackView;
import com.liskovsoft.smartyoutubetv2.common.app.views.SearchView;
import com.liskovsoft.smartyoutubetv2.common.app.views.SignInView;
import com.liskovsoft.smartyoutubetv2.common.app.views.SplashView;
import com.liskovsoft.smartyoutubetv2.common.app.views.ViewManager;

import minefarts.smarttube.ui.adddevice.AddDeviceActivity;
import minefarts.smarttube.ui.browse.BrowseActivity;
import minefarts.smarttube.ui.channel.ChannelActivity;
import minefarts.smarttube.ui.channeluploads.ChannelUploadsActivity;
import minefarts.smarttube.ui.dialogs.AppDialogActivity;
import minefarts.smarttube.ui.playback.PlaybackActivity;
import minefarts.smarttube.ui.search.tags.SearchTagsActivity;
import minefarts.smarttube.ui.signin.SignInActivity;

import java.lang.Thread.UncaughtExceptionHandler;

public class MainApplication extends MultiDexApplication {
    
    static {
        System.setProperty("http.keepAlive", "false");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupViewManager();
        disableSections();
    }

    private void setupViewManager() {

        ViewManager viewManager = ViewManager.instance(this);

        viewManager.setRoot(BrowseActivity.class);

        viewManager.register(SplashView.class, SplashActivity.class); // no parent, because it's root activity

        viewManager.register(BrowseView.class, BrowseActivity.class); // no parent, because it's root activity

        viewManager.register(PlaybackView.class, PlaybackActivity.class, BrowseActivity.class);

        viewManager.register(AppDialogView.class, AppDialogActivity.class, BrowseActivity.class);

        viewManager.register(SearchView.class, SearchTagsActivity.class, BrowseActivity.class);

        viewManager.register(SignInView.class, SignInActivity.class, BrowseActivity.class);

        viewManager.register(AddDeviceView.class, AddDeviceActivity.class, BrowseActivity.class);

        viewManager.register(ChannelView.class, ChannelActivity.class, BrowseActivity.class);

        viewManager.register(ChannelUploadsView.class, ChannelUploadsActivity.class, BrowseActivity.class);

    }

    private void disableSections() {

        BrowsePresenter bp = BrowsePresenter.instance(this);
        
        bp.enableSection(MediaGroup.TYPE_MUSIC, false);
        bp.enableSection(MediaGroup.TYPE_NEWS, false);
        bp.enableSection(MediaGroup.TYPE_GAMING, false);
        bp.enableSection(MediaGroup.TYPE_CHANNEL, false);
        bp.enableSection(MediaGroup.TYPE_KIDS_HOME, false);
        bp.enableSection(MediaGroup.TYPE_TRENDING, false);
        bp.enableSection(MediaGroup.TYPE_SHORTS, false);
        bp.enableSection(MediaGroup.TYPE_NOTIFICATIONS, false);
        bp.enableSection(MediaGroup.TYPE_SPORTS, false);
        bp.enableSection(MediaGroup.TYPE_MOVIES, false);
        bp.enableSection(MediaGroup.TYPE_LIVE, false);
        bp.enableSection(MediaGroup.TYPE_MY_VIDEOS, false);
        bp.enableSection(MediaGroup.TYPE_PLAYBACK_QUEUE, false);
        bp.enableSection(MediaGroup.TYPE_BLOCKED_CHANNELS, false);

    }

}
