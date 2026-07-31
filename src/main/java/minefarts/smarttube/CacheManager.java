package minefarts.smarttube;

import com.liskovsoft.googlecommon.common.locale.LocaleManager;
import com.liskovsoft.sharedutils.locale.LocaleUpdater;
import com.liskovsoft.sharedutils.helpers.FileHelpers;
import com.liskovsoft.youtubeapi.common.helpers.AppClient;
import com.liskovsoft.youtubeapi.service.YouTubeSignInService;
import com.liskovsoft.youtubeapi.service.YouTubeMediaItemService;
import com.liskovsoft.youtubeapi.service.internal.MediaServiceData;
import com.liskovsoft.youtubeapi.app.AppService;
import com.liskovsoft.youtubeapi.app.AppApi;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.listener.PlayerEventListener;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.PlaybackPresenter;

import minefarts.smarttube.utils.app.nsigsolver.V8ChallengeProvider;

import java.util.concurrent.Executors;

import android.util.Base64;
import android.content.Context;
import android.os.Looper;
import android.os.Handler;

public class CacheManager {

    public static void clear() {

        Context context = ContextManager.get();

        //=======================
        // YouTubeSignInService

        YouTubeSignInService SIS = YouTubeSignInService.instance();

        SIS.invalidateCache();
        
        //=======================
        // AppService
        
        AppService AS = AppService.instance();

        AS.invalidateCache();
        AS.resetClientPlaybackNonce();

        //=======================
        // LocaleUpdater

        LocaleUpdater.clearCache();

        //=======================
        // YouTubeMediaItemService

        YouTubeMediaItemService MIS = YouTubeMediaItemService.instance();

        MIS.invalidateCache();
        
        //=======================
        // MediaServiceData

        MediaServiceData MSD = MediaServiceData.instance();
        
        MSD.setPoToken(null);

        //=======================
        // FileHelpers

        FileHelpers.deleteCache(context);

        //=======================
        // V8ChallengeProvider

        V8ChallengeProvider.v8Executor = Executors.newSingleThreadExecutor();
        V8ChallengeProvider.v8Runtime = null;

        //=======================

    }

}