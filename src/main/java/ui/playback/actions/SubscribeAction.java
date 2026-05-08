package SmartTubeApp.ui.playback.actions;

import android.content.Context;

import SmartTubeApp.R;
import SmartTubeApp.app.models.data.Video;
import com.liskovsoft.sharedutils.service.YouTubeMediaItemService;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;

/**
 * An action for displaying subscribe states.
 */
public class SubscribeAction extends TwoStateAction {

    private static Context mContext;
    private static YouTubeMediaItemService mMediaItemService;

    public SubscribeAction(Context context) {
        
        super(
            context, 
            R.id.action_subscribe, 
            R.drawable.action_subscribe
        );

        mContext = context;
        mMediaItemService = YouTubeMediaItemService.instance();

        String[] labels = new String[2];
        // Note, labels denote the action taken when clicked
        labels[INDEX_OFF] = context.getString(R.string.unsubscribed_from_channel);
        labels[INDEX_ON] = context.getString(R.string.subscribed_to_channel);
        setLabels(labels);

    }

    public static void toggle(Video video) {
        if (video == null) return;

        if (video.isSubscribed) {
            mMediaItemService.unsubscribe(video.channelId);
            video.isSubscribed = false;
            MessageHelpers.showMessage(mContext, "Unsubscribed");
        } else {
            mMediaItemService.subscribe(video.channelId);
            video.isSubscribed = true;
            MessageHelpers.showMessage(mContext, "Subscribed");
        }

    }

}
