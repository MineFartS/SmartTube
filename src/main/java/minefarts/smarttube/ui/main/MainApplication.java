package minefarts.smarttube.ui.main;

import android.content.Context;
import androidx.multidex.MultiDexApplication;

import minefarts.smarttube.utils.helpers.Helpers;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.BrowseSection;
import minefarts.smarttube.app.presenters.BrowsePresenter;
import minefarts.smarttube.app.views.AddDeviceView;
import minefarts.smarttube.app.views.AppDialogView;
import minefarts.smarttube.app.views.BrowseView;
import minefarts.smarttube.app.views.ChannelUploadsView;
import minefarts.smarttube.app.views.ChannelView;
import minefarts.smarttube.ui.playback.PlaybackFragment2;
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
import com.liskovsoft.smartyoutubetv2.tv.ui.search.tags.SearchTagsActivity;
import minefarts.smarttube.ui.signin.SignInActivity;

import java.lang.Thread.UncaughtExceptionHandler;
import java.io.File;

public class MainApplication extends MultiDexApplication {
    
    static {
        System.setProperty("http.keepAlive", "false");
    }

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        //===========================================================================

        // Target the problematic WebView cache path
        File webViewCache = new File(getCacheDir(), "WebView");
        
        // If it exists but is a file, delete it so it can become a folder
        if (webViewCache.exists() && !webViewCache.isDirectory())
            webViewCache.delete();
        
        // Force create the directory structure
        if (!webViewCache.exists())
            webViewCache.mkdirs();

        //===========================================================================

        ViewManager viewManager = ViewManager.instance(this);

        viewManager.setRoot(BrowseActivity.class);

        viewManager.register(SplashView.class, SplashActivity.class);

        viewManager.register(BrowseView.class, BrowseActivity.class);

        viewManager.register(PlaybackFragment2.class, PlaybackActivity.class, BrowseActivity.class);

        viewManager.register(AppDialogView.class, AppDialogActivity.class, BrowseActivity.class);

        viewManager.register(SearchView.class, SearchTagsActivity.class, BrowseActivity.class);

        viewManager.register(SignInView.class, SignInActivity.class, BrowseActivity.class);

        viewManager.register(AddDeviceView.class, AddDeviceActivity.class, BrowseActivity.class);

        viewManager.register(ChannelView.class, ChannelActivity.class, BrowseActivity.class);

        viewManager.register(ChannelUploadsView.class, ChannelUploadsActivity.class, BrowseActivity.class);

        //===========================================================================

    }

}
