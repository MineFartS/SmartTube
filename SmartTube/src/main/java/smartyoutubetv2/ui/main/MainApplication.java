package SmartTubeApp.ui.main;

import androidx.multidex.MultiDexApplication;

import com.liskovsoft.sharedutils.helpers.Helpers;
import smartyoutubetv1.app.models.data.BrowseSection;
import smartyoutubetv1.app.presenters.BrowsePresenter;
import smartyoutubetv1.app.views.AddDeviceView;
import smartyoutubetv1.app.views.AppDialogView;
import smartyoutubetv1.app.views.BrowseView;
import smartyoutubetv1.app.views.ChannelUploadsView;
import smartyoutubetv1.app.views.ChannelView;
import smartyoutubetv1.app.views.PlaybackView;
import smartyoutubetv1.app.views.SearchView;
import smartyoutubetv1.app.views.SignInView;
import smartyoutubetv1.app.views.SplashView;
import smartyoutubetv1.app.views.ViewManager;
import SmartTubeApp.ui.adddevice.AddDeviceActivity;
import SmartTubeApp.ui.browse.BrowseActivity;
import SmartTubeApp.ui.channel.ChannelActivity;
import SmartTubeApp.ui.channeluploads.ChannelUploadsActivity;
import SmartTubeApp.ui.dialogs.AppDialogActivity;
import SmartTubeApp.ui.playback.PlaybackActivity;
import SmartTubeApp.ui.search.tags.SearchTagsActivity;
import SmartTubeApp.ui.signin.SignInActivity;

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
