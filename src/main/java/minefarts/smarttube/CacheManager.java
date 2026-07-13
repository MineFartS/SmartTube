package minefarts.smarttube;

import minefarts.smarttube.utils.locale.LocaleUpdater;
import minefarts.smarttube.utils.SignInService;
import minefarts.smarttube.utils.MediaItemService;
import minefarts.smarttube.utils.videoinfo.V2.VideoInfoService;
import minefarts.smarttube.utils.common.helpers.AppClient;
import minefarts.smarttube.utils.app.AppService;
import minefarts.smarttube.utils.app.potoken.PoTokenGate;
import minefarts.smarttube.utils.service.internal.MediaServiceData;
import minefarts.smarttube.utils.helpers.FileHelpers;
import minefarts.smarttube.app.models.playback.controllers.VideoStateController;
import minefarts.smarttube.app.models.playback.PlayerEventListener;
import minefarts.smarttube.app.presenters.PlaybackPresenter;
import minefarts.smarttube.utils.app.nsigsolver.impl.V8ChallengeProvider;

import android.util.Base64;
import android.content.Context;

public class CacheManager {

    public static void clear() {

        //=======================
        // SignInService

        SignInService SIS = SignInService.instance();
        
        //=======================
        // AppService
        
        AppService AS = AppService.instance();

        AS.mVisitorCookie = null;

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
        VIS.mVideoInfoType = null;
        VIS.persistVideoInfoType();
        
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

        V8ChallengeProvider.v8Runtime.remove();

        //=======================

    }

    public static void releaseEngine() {

        Context context = ContextManager.get();

        //=======================
        // PlaybackPresenter

        PlaybackPresenter PP = PlaybackPresenter.instance(context);

        for (PlayerEventListener listener : PP.mEventListeners) {
            listener.onEngineReleased();
        }

        //=======================

    }

}