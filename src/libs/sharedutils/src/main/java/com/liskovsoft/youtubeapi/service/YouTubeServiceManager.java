package com.liskovsoft.sharedutils.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liskovsoft.sharedutils.ChannelGroupService;
import com.liskovsoft.sharedutils.CommentsService;
import com.liskovsoft.sharedutils.service.ContentService;
import com.liskovsoft.sharedutils.LiveChatService;
import com.liskovsoft.sharedutils.MediaItemService;
import com.liskovsoft.sharedutils.NotificationsService;
import com.liskovsoft.sharedutils.RemoteControlService;
import com.liskovsoft.sharedutils.ServiceManager;
import com.liskovsoft.sharedutils.SignInService;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.sharedutils.app.AppService;
import com.liskovsoft.sharedutils.channelgroups.ChannelGroupServiceImpl;
import com.liskovsoft.googlecommon.common.locale.LocaleManager;
import com.liskovsoft.sharedutils.service.internal.MediaServiceData;
import com.liskovsoft.sharedutils.videoinfo.V2.VideoInfoService;

import io.reactivex.disposables.Disposable;

public class YouTubeServiceManager implements ServiceManager {
    private static final String TAG = YouTubeServiceManager.class.getSimpleName();
    private static YouTubeServiceManager sInstance;
    private Disposable mRefreshCoreDataAction;

    private YouTubeServiceManager() {
        Log.d(TAG, "Starting...");
    }

    public static ServiceManager instance() {
        if (sInstance == null) {
            sInstance = new YouTubeServiceManager();
        }

        return sInstance;
    }

    @Override
    public SignInService getSignInService() {
        return getYouTubeSignInService();
    }

    @Override
    public RemoteControlService getRemoteControlService() {
        return getYouTubeRemoteControlService();
    }

    @Override
    public LiveChatService getLiveChatService() {
        return getYouTubeLiveChatService();
    }

    @Override
    public CommentsService getCommentsService() {
        return getYouTubeCommentsService();
    }

    @Override
    public ContentService getContentService() {
        return ContentService.instance();
    }

    @Override
    public MediaItemService getMediaItemService() {
        return getYouTubeMediaItemService();
    }

    @Override
    public NotificationsService getNotificationsService() {
        return getYouTubeNotificationsService();
    }

    @Override
    public ChannelGroupService getChannelGroupService() {
        return getChannelGroupServiceImpl();
    }

    @Override
    public void invalidateCache() {
        LocaleManager.unhold();
        getYouTubeSignInService().invalidateCache(); // sections infinite loading fix (request timed out fix)
        getAppService().invalidateCache();
        //AppService.instance().invalidateVisitorData();
        getYouTubeMediaItemService().invalidateCache();
        getVideoInfoService().resetInfoType();
    }

    @Override
    public void refreshCacheIfNeeded() {
        refreshCacheIfNeededInt();
    }

    @Override
    public void applyNoPlaybackFix() {
        getYouTubeMediaItemService().invalidateCache();
        getVideoInfoService().switchNextFormat();
    }

    @Override
    public void applySubtitleFix() {
        getYouTubeMediaItemService().invalidateCache();
        getVideoInfoService().switchNextSubtitle();
    }

    private void refreshCacheIfNeededInt() {
        if (RxHelper.isAnyActionRunning(mRefreshCoreDataAction)) {
            return;
        }

        mRefreshCoreDataAction = RxHelper.execute(RxHelper.fromRunnable(getAppService()::refreshCacheIfNeeded));
    }

    @NonNull
    private static YouTubeSignInService getYouTubeSignInService() {
        return YouTubeSignInService.instance();
    }

    @NonNull
    private static YouTubeRemoteControlService getYouTubeRemoteControlService() {
        return YouTubeRemoteControlService.instance();
    }

    @NonNull
    private static YouTubeMediaItemService getYouTubeMediaItemService() {
        return YouTubeMediaItemService.instance();
    }

    @NonNull
    private static YouTubeLiveChatService getYouTubeLiveChatService() {
        return YouTubeLiveChatService.instance();
    }

    @NonNull
    private static CommentsService getYouTubeCommentsService() {
        return YouTubeCommentsService.INSTANCE;
    }

    @NonNull
    private static VideoInfoService getVideoInfoService() {
        return VideoInfoService.instance();
    }

    @NonNull
    private static AppService getAppService() {
        return AppService.instance();
    }

    @NonNull
    private static NotificationsService getYouTubeNotificationsService() {
        return YouTubeNotificationsService.INSTANCE;
    }

    @NonNull
    private static ChannelGroupService getChannelGroupServiceImpl() {
        return ChannelGroupServiceImpl.INSTANCE;
    }

}
