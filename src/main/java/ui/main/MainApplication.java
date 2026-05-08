package SmartTubeApp.ui.main;

import android.content.Context;
import androidx.multidex.MultiDexApplication;

import com.liskovsoft.sharedutils.helpers.Helpers;
import SmartTubeApp.app.models.data.BrowseSection;
import SmartTubeApp.app.presenters.BrowsePresenter;
import SmartTubeApp.app.views.AddDeviceView;
import SmartTubeApp.app.views.AppDialogView;
import SmartTubeApp.app.views.BrowseView;
import SmartTubeApp.app.views.ChannelUploadsView;
import SmartTubeApp.app.views.ChannelView;
import SmartTubeApp.app.models.playback.PlayerEngine;
import SmartTubeApp.app.views.SearchView;
import SmartTubeApp.app.views.SignInView;
import SmartTubeApp.app.views.SplashView;
import SmartTubeApp.app.views.ViewManager;
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

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        ViewManager viewManager = ViewManager.instance(this);

        viewManager.setRoot(BrowseActivity.class);

        viewManager.register(SplashView.class, SplashActivity.class); // no parent, because it's root activity

        viewManager.register(BrowseView.class, BrowseActivity.class); // no parent, because it's root activity

        viewManager.register(PlayerEngine.class, PlaybackActivity.class, BrowseActivity.class);

        viewManager.register(AppDialogView.class, AppDialogActivity.class, BrowseActivity.class);

        viewManager.register(SearchView.class, SearchTagsActivity.class, BrowseActivity.class);

        viewManager.register(SignInView.class, SignInActivity.class, BrowseActivity.class);

        viewManager.register(AddDeviceView.class, AddDeviceActivity.class, BrowseActivity.class);

        viewManager.register(ChannelView.class, ChannelActivity.class, BrowseActivity.class);

        viewManager.register(ChannelUploadsView.class, ChannelUploadsActivity.class, BrowseActivity.class);

    }

}
