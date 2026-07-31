package minefarts.smarttube.utils;

import com.liskovsoft.youtubeapi.common.helpers.PostDataHelper;
import com.liskovsoft.youtubeapi.service.YouTubeSignInService;
import com.liskovsoft.youtubeapi.actions.ActionsApi;
import com.liskovsoft.youtubeapi.actions.models.ActionResult;
import com.liskovsoft.youtubeapi.channelgroups.ChannelGroupServiceImpl;
import com.liskovsoft.youtubeapi.next.v2.WatchNextServiceWrapper;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;
import com.liskovsoft.googlecommon.common.helpers.RetrofitHelper;

import minefarts.smarttube.R;
import minefarts.smarttube.ContextManager;

import io.reactivex.Observable;

import retrofit2.Call;

public class SubscribeController {

    private static final ActionsApi mActionsApi = RetrofitHelper.create(ActionsApi.class);
    
    private static final YouTubeSignInService mSignInService = YouTubeSignInService.instance();

    private static final WatchNextServiceWrapper mWatchNextService = WatchNextServiceWrapper.INSTANCE;

    public static void toggle(Video video) {

        if (video == null || video.channelId == null) return;

        RxHelper.runAsync(() -> dotoggle(video));

    }

    public static void refresh(Video video) {

        MediaItemMetadata metadata = mWatchNextService.getMetadata(video.videoId);
        video.isSubscribed = metadata.isSubscribed();
    
    }

    private static void dotoggle(Video video) {

        mSignInService.checkAuth();

        refresh(video);

        String data = "\"channelIds\":[\"" + video.channelId + "\"],\"params\":\"\"";
        String query = PostDataHelper.createQueryTV(data);

        Call<ActionResult> wrapper;

        if (video.isSubscribed) {
            
            wrapper = mActionsApi.unsubscribe(query);

            video.isSubscribed = false;
        
            MessageHelpers.showMessage(ContextManager.get(), "Unsubscribed");
        
        } else {
            
            wrapper = mActionsApi.subscribe(query);
            
            video.isSubscribed = true;
            
            MessageHelpers.showMessage(ContextManager.get(), "Subscribed");
        
        }

        ChannelGroupServiceImpl.subscribe(video.isSubscribed, video.channelId, null, null);

        RetrofitHelper.get(wrapper);

    }

}
