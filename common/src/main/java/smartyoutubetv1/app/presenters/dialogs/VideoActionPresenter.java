package smartyoutubetv1.app.presenters.dialogs;

import android.content.Context;

import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import smartyoutubetv1.app.models.data.Video;
import smartyoutubetv1.app.presenters.ChannelUploadsPresenter;
import smartyoutubetv1.app.presenters.PlaybackPresenter;
import smartyoutubetv1.app.presenters.SearchPresenter;
import smartyoutubetv1.app.presenters.base.BasePresenter;
import smartyoutubetv1.misc.MediaServiceManager;
import smartyoutubetv1.utils.LoadingManager;

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
