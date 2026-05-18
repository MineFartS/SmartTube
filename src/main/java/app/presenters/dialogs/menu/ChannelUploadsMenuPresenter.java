package minefarts.smarttube.app.presenters.dialogs.menu;

import android.content.Context;

import com.liskovsoft.sharedutils.MediaItemService;
import minefarts.smarttube.misc.ServiceManager;
import com.liskovsoft.sharedutils.data.MediaItem;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.rx.RxHelper;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.app.presenters.ChannelPresenter;
import minefarts.smarttube.app.presenters.ChannelUploadsPresenter;
import minefarts.smarttube.app.presenters.dialogs.menu.VideoMenuPresenter.VideoMenuCallback;
import minefarts.smarttube.ui.playback.actions.SubscribeAction;

import io.reactivex.disposables.Disposable;

import java.util.List;

public class ChannelUploadsMenuPresenter extends BaseMenuPresenter {
    
    private final MediaItemService mItemManager;
    private final AppDialogPresenter mDialogPresenter;
    private Video mVideo;
    private VideoMenuCallback mCallback;

    private ChannelUploadsMenuPresenter(Context context) {
        super(context);
        mItemManager = ServiceManager.getMediaItemService();
        mDialogPresenter = AppDialogPresenter.instance(context);
    }

    public static ChannelUploadsMenuPresenter instance(Context context) {
        return new ChannelUploadsMenuPresenter(context);
    }

    @Override
    protected Video getVideo() {
        return mVideo;
    }

    @Override
    protected AppDialogPresenter getDialogPresenter() {
        return mDialogPresenter;
    }

    @Override
    protected VideoMenuCallback getCallback() {
        return mCallback;
    }

    public void showMenu(Video video, VideoMenuCallback callback) {
        mCallback = callback;
        showMenu(video);
    }

    public void showMenu(Video video) {
        if (video == null || !video.belongsToChannelUploads()) return;

        mVideo = video;

        prepareAndShowDialog();
    }

    private void prepareAndShowDialog() {
        // Doesn't need this since this is the main action.
        //appendOpenChannelUploadsButton();
        appendOpenChannelButton();
        appendUnsubscribeButton();
        appendMarkAsWatched();
        appendTogglePinVideoToSidebarButton();

        mDialogPresenter.showDialog(mVideo.getTitle());
    }

    private void appendOpenChannelButton() {
        if (!ChannelPresenter.canOpenChannel(mVideo)) {
            return;
        }

        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(getContext().getString(R.string.open_channel), optionItem -> ChannelPresenter.instance(getContext()).openChannel(mVideo)));
    }

    private void appendUnsubscribeButton() {
        if (mVideo == null) return;

        mDialogPresenter.appendSingleButton(UiOptionItem.from(
            "Unsubscribe", 
            optionItem -> SubscribeAction.toggle(mVideo)
        ));
    }

    private void appendMarkAsWatched() {
        if (mVideo == null || !mVideo.hasNewContent) {
            return;
        }

        boolean contentAlreadyLoaded = mVideo.groupPosition == 0;

        if (contentAlreadyLoaded) {
            return;
        }

        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(getContext().getString(R.string.mark_as_watched), optionItem -> {
                    ServiceManager.loadChannelUploads(mVideo, (group) -> {});
                    MessageHelpers.showMessage(getContext(), R.string.channel_marked_as_watched);
                }));
    }

}
