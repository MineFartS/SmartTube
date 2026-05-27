package minefarts.smarttube.ui.main;

import android.content.Context;
import androidx.multidex.MultiDexApplication;

import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.app.models.data.BrowseSection;
import minefarts.smarttube.app.presenters.BrowsePresenter;
import minefarts.smarttube.app.views.AddDeviceView;
import minefarts.smarttube.app.views.AppDialogView;
import minefarts.smarttube.app.views.BrowseView;
import minefarts.smarttube.app.views.ChannelUploadsView;
import minefarts.smarttube.app.views.ChannelView;
import minefarts.smarttube.app.models.playback.PlayerEngine;
import minefarts.smarttube.app.views.SearchView;
import minefarts.smarttube.app.views.SignInView;
import minefarts.smarttube.app.views.SplashView;
import minefarts.smarttube.app.views.ViewManager;
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
