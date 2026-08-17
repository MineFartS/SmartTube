package minefarts.smarttube.ui.playback.actions;

import android.content.Context;

import minefarts.smarttube.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import minefarts.smarttube.utils.helpers.MessageHelpers;
import com.liskovsoft.googlecommon.common.helpers.RetrofitHelper;
import com.liskovsoft.youtubeapi.common.helpers.PostDataHelper;
import minefarts.smarttube.utils.SignInService;
import minefarts.smarttube.utils.actions.ActionsApi;
import minefarts.smarttube.utils.actions.models.ActionResult;
import com.liskovsoft.youtubeapi.channelgroups.ChannelGroupServiceImpl;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.youtubeapi.next.v2.WatchNextServiceWrapper;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;

import io.reactivex.Observable;

import retrofit2.Call;

// An action for displaying subscribe states.
public class SubscribeAction extends TwoStateAction {

    private static Context mContext;

    private static final ActionsApi mActionsApi = RetrofitHelper.create(ActionsApi.class);
    
    private static final SignInService mSignInService = SignInService.instance();

    private static final WatchNextServiceWrapper mWatchNextService = WatchNextServiceWrapper.INSTANCE;

    public SubscribeAction(Context context) {
        
        super(
            context, 
            R.id.action_subscribe, 
            R.drawable.action_subscribe
        );

        mContext = context;

        String[] labels = new String[2];

        // Note, labels denote the action taken when clicked
        labels[INDEX_OFF] = "Unsubscribed";
        labels[INDEX_ON] = "Subscribed";

        setLabels(labels);

    }

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
        
            MessageHelpers.showMessage(mContext, "Unsubscribed");
        
        } else {
            
            wrapper = mActionsApi.subscribe(query);
            
            video.isSubscribed = true;
            
            MessageHelpers.showMessage(mContext, "Subscribed");
        
        }

        ChannelGroupServiceImpl.subscribe(video.isSubscribed, video.channelId, video.getAuthor(), video.getCardImageUrl());

        RetrofitHelper.get(wrapper);

    }

}
