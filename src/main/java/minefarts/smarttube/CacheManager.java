package minefarts.smarttube;

import minefarts.smarttube.google.common.locale.LocaleManager;
import minefarts.smarttube.utils.locale.LocaleUpdater;
import minefarts.smarttube.utils.SignInService;
import minefarts.smarttube.utils.MediaItemService;
import minefarts.smarttube.utils.videoinfo.V2.VideoInfoService;
import minefarts.smarttube.utils.common.helpers.AppClient;
import minefarts.smarttube.utils.app.potokennp2.PoTokenProvider;
import minefarts.smarttube.utils.app.PoTokenGate;
import minefarts.smarttube.utils.service.internal.MediaServiceData;
import minefarts.smarttube.utils.helpers.FileHelpers;

import android.content.Context;

public class CacheManager {

    private static final SignInService sSignInService = SignInService.instance();

    private static final MediaItemService sMediaItemService = MediaItemService.instance();

    public static void clear() {

        //=======================
        // SignInService

        SignInService SIS = SignInService.instance();
        
        SIS.mCachedAuthorizationHeader = null;
        SIS.mCacheUpdateTime = 0;

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
        MediaServiceData MSD = MediaServiceData.instance();
        PoTokenProvider PTP = PoTokenProvider.INSTANCE;
        PoTokenGate PTG = PoTokenGate.INSTANCE;

        VIS.mVideoInfoType = null;
        VIS.persistVideoInfoType();
        
        AppClient client = VIS.getClient();

        PTG.mWebPoToken = null;
        PTP.webPoTokenVisitorData = null;
        PTP.webPoTokenStreamingPot = null;
        MSD.mPoToken = null;

        //=======================
        // FileHelpers

        Context context = ContextManager.get();

        FileHelpers.deleteContent(FileHelpers.getInternalCacheDir(context));
        FileHelpers.deleteContent(FileHelpers.getExternalCacheDir(context));

        //=======================

    }

}