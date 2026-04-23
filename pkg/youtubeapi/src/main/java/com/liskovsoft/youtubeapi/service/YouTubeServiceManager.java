package com.liskovsoft.youtubeapi.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liskovsoft.youtubeapi.ChannelGroupService;
import com.liskovsoft.youtubeapi.CommentsService;
import com.liskovsoft.youtubeapi.ContentService;
import com.liskovsoft.youtubeapi.LiveChatService;
import com.liskovsoft.youtubeapi.MediaItemService;
import com.liskovsoft.youtubeapi.NotificationsService;
import com.liskovsoft.youtubeapi.RemoteControlService;
import com.liskovsoft.youtubeapi.ServiceManager;
import com.liskovsoft.youtubeapi.SignInService;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.youtubeapi.app.AppService;
import com.liskovsoft.youtubeapi.channelgroups.ChannelGroupServiceImpl;
import com.liskovsoft.googlecommon.common.locale.LocaleManager;
import com.liskovsoft.youtubeapi.service.internal.MediaServiceData;
import com.liskovsoft.youtubeapi.videoinfo.V2.VideoInfoService;

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
        return getYouTubeContentService();
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
    private static ContentService getYouTubeContentService() {
        return YouTubeContentService.instance();
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
