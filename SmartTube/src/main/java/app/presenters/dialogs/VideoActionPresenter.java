package SmartTubeApp.app.presenters.dialogs;

import android.content.Context;

import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import SmartTubeApp.app.models.data.Video;
import SmartTubeApp.app.presenters.ChannelUploadsPresenter;
import SmartTubeApp.app.presenters.PlaybackPresenter;
import SmartTubeApp.app.presenters.SearchPresenter;
import SmartTubeApp.app.presenters.base.BasePresenter;
import SmartTubeApp.misc.MediaServiceManager;
import SmartTubeApp.utils.LoadingManager;

public class VideoActionPresenter extends BasePresenter<Void> {


    private VideoActionPresenter(Context context) {
        super(context);
    }

    public static VideoActionPresenter instance(Context context) {
        return new VideoActionPresenter(context);
    }

    public void apply(Video item) {
        if (item == null) {
            return;
        }

        // Show playlist contents in channel instead of instant playback
        if (item.hasVideo() && !item.isPlaylistInChannel()) {
            PlaybackPresenter.instance(getContext()).openVideo(item);
        } else if (item.hasChannel() || item.belongsToChannelUploads()) {
            MediaServiceManager.chooseChannelPresenter(getContext(), item);
        } else if (item.hasPlaylist() || item.hasNestedItems()) {
            ChannelUploadsPresenter.instance(getContext()).openChannel(item);
        } else if (item.isChapter) {
            PlaybackPresenter.instance(getContext()).setPosition(item.startTimeMs);
        } else if (item.searchQuery != null ) {
            SearchPresenter.instance(getContext()).onSearch(item.searchQuery);
        } else {
            MessageHelpers.showMessage(getContext(), "Video item doesn't contain needed data!");
        }
    }

}
