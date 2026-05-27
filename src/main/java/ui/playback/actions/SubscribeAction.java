package minefarts.smarttube.ui.playback.actions;

import android.content.Context;

import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.Video;
import minefarts.sharedutils.helpers.MessageHelpers;
import minefarts.googlecommon.common.helpers.RetrofitHelper;
import minefarts.sharedutils.common.helpers.PostDataHelper;
import minefarts.sharedutils.SignInService;
import minefarts.sharedutils.actions.ActionsApi;
import minefarts.sharedutils.actions.models.ActionResult;
import minefarts.sharedutils.channelgroups.ChannelGroupServiceImpl;
import minefarts.sharedutils.rx.RxHelper;
import minefarts.sharedutils.next.v2.WatchNextServiceWrapper;
import minefarts.sharedutils.service.data.MediaItemMetadata;

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

        mSignInService.checkAuth();

        MediaItemMetadata metadata = mWatchNextService.getMetadata(video.videoId);
        video.isSubscribed = metadata.isSubscribed();

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

        ChannelGroupServiceImpl.subscribe(video.isSubscribed, video.channelId);

        RetrofitHelper.get(wrapper);

    }

}
