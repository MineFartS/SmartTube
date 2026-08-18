package minefarts.smarttube.ui.main;

import androidx.multidex.MultiDexApplication;

import com.liskovsoft.sharedutils.helpers.Helpers;
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

public class MainApplication extends MultiDexApplication { // fix: Didn't find class "com.google.firebase.provider.FirebaseInitProvider"
    static {
        // fix youtube bandwidth throttling (best - false)???
        // false is better for streams (less buffering)
        System.setProperty("http.keepAlive", "false");
        // fix ipv6 infinite video buffering???
        // Better to remove this fix at all. Users complain about infinite loading.
        //System.setProperty("java.net.preferIPv6Addresses", "true");
        // Another IPv6 fix (no effect)
        // https://stackoverflow.com/questions/1920623/sometimes-httpurlconnection-getinputstream-executes-too-slowly
        //System.setProperty("java.net.preferIPv4Stack" , "true");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupViewManager();
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

}
