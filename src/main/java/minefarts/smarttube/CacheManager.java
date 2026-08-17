package minefarts.smarttube;

import com.liskovsoft.youtubeapi.common.helpers.AppClient;
import com.liskovsoft.googlecommon.common.locale.LocaleManager;
import com.liskovsoft.youtubeapi.videoinfo.V2.VideoInfoService;

import com.liskovsoft.youtubeapi.app.AppService;

import minefarts.smarttube.utils.locale.LocaleUpdater;
import minefarts.smarttube.utils.SignInService;
import minefarts.smarttube.utils.MediaItemService;
import minefarts.smarttube.utils.service.internal.MediaServiceData;
import minefarts.smarttube.utils.helpers.FileHelpers;
import minefarts.smarttube.app.models.playback.controllers.VideoStateController;
import minefarts.smarttube.utils.app.nsigsolver.V8ChallengeProvider;

import java.util.concurrent.Executors;

import android.util.Base64;
import android.content.Context;
import android.os.Looper;
import android.os.Handler;

public class CacheManager {

    public static void clear() {

        //=======================
        // SignInService

        SignInService SIS = SignInService.instance();

        SIS.mCacheUpdateTime = 0;
        
        //=======================
        // AppService
        
        AppService AS = AppService.instance();

        AS.invalidateCache();

        //=======================
        // LocaleUpdater

        LocaleUpdater.sCachedLocale = null;

        //=======================
        // MediaItemService

        MediaItemService MIS = MediaItemService.instance();

        MIS.mCachedFormatInfo = null;

        //=======================
        // VideoInfoService

        VideoInfoService VIS = VideoInfoService.instance();

        VIS.resetInfoType();
        
        //=======================
        // MediaServiceData

        MediaServiceData MSD = MediaServiceData.instance();
        
        MSD.mPoToken = null;

        //=======================
        // FileHelpers

        Context context = ContextManager.get();

        FileHelpers.deleteContent(FileHelpers.getInternalCacheDir(context));
        FileHelpers.deleteContent(FileHelpers.getExternalCacheDir(context));

        //=======================
        // VideoStateController

        VideoStateController.mClientPlaybackNonce = Base64.encodeToString(
            new byte[32], 
            Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP
        );

        //=======================
        // V8ChallengeProvider

        V8ChallengeProvider.v8Executor = Executors.newSingleThreadExecutor();
        V8ChallengeProvider.v8Runtime = null;

        //=======================

    }

}