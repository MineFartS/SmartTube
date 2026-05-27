package minefarts.smarttube.ui.playback.actions;

import android.content.Context;

import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.utils.helpers.MessageHelpers;
import minefarts.smarttube.google.common.helpers.RetrofitHelper;
import minefarts.smarttube.utils.common.helpers.PostDataHelper;
import minefarts.smarttube.utils.SignInService;
import minefarts.smarttube.utils.actions.ActionsApi;
import minefarts.smarttube.utils.actions.models.ActionResult;
import minefarts.smarttube.utils.channelgroups.ChannelGroupServiceImpl;
import minefarts.smarttube.utils.rx.RxHelper;
import minefarts.smarttube.utils.next.v2.WatchNextServiceWrapper;
import minefarts.smarttube.utils.service.data.MediaItemMetadata;

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
