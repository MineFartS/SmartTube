package minefarts.smarttube.app.presenters.dialogs;

import android.content.Context;

import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.presenters.ChannelUploadsPresenter;
import minefarts.smarttube.app.presenters.PlaybackPresenter;
import minefarts.smarttube.app.presenters.SearchPresenter;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.misc.ServiceManager;
import minefarts.smarttube.utils.LoadingManager;

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
            ServiceManager.chooseChannelPresenter(getContext(), item);
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
