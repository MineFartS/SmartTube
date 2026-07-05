package minefarts.smarttube;

import com.liskovsoft.youtubeapi.app.potokennp2.core.PoTokenProvider;
import com.liskovsoft.googlecommon.common.locale.LocaleManager;
import com.liskovsoft.youtubeapi.common.helpers.AppClient;
import com.liskovsoft.youtubeapi.app.PoTokenGate;

import minefarts.smarttube.utils.locale.LocaleUpdater;
import minefarts.smarttube.utils.MediaItemService;
import minefarts.smarttube.utils.helpers.FileHelpers;
import minefarts.smarttube.app.models.playback.controllers.VideoStateController;
import minefarts.smarttube.app.models.playback.PlayerEventListener;
import minefarts.smarttube.app.presenters.PlaybackPresenter;

import android.util.Base64;
import android.content.Context;
import android.os.Looper;
import android.os.Handler;

public class CacheManager {

    public static void clear() {

        //=======================
        // LocaleUpdater

        LocaleUpdater.sCachedLocale = null;

        //=======================
        // MediaItemService

        MediaItemService MIS = MediaItemService.instance();

        MIS.mCachedFormatInfo = null;

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