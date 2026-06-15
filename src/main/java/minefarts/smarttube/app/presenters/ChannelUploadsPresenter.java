package minefarts.smarttube.app.presenters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import minefarts.smarttube.utils.service.data.MediaGroup;
import minefarts.smarttube.utils.data.MediaItem;
import minefarts.smarttube.utils.service.data.MediaItemMetadata;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.helpers.MessageHelpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.rx.RxHelper;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.SimpleMediaItem;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.data.VideoGroup;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.app.models.playback.controllers.VideoLoaderController;
import minefarts.smarttube.app.presenters.dialogs.menu.VideoMenuPresenter;
import minefarts.smarttube.app.presenters.dialogs.menu.VideoMenuPresenter.VideoMenuCallback;
import minefarts.smarttube.app.views.ChannelUploadsView;
import minefarts.smarttube.utils.BrowseProcessorManager;
import minefarts.smarttube.utils.ServiceManager;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.util.List;

public class ChannelUploadsPresenter extends BasePresenter<ChannelUploadsView> {
    private static final String TAG = ChannelUploadsPresenter.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static ChannelUploadsPresenter sInstance;
    private final BrowseProcessorManager mBrowseProcessor;
    private Disposable mUpdateAction;
    private Disposable mScrollAction;
    private Video mChannel;
    private MediaGroup mPendingGroup;
    private VideoGroup mBaseGroup;

    public ChannelUploadsPresenter(Context context) {
        super(context);
        mBrowseProcessor = new BrowseProcessorManager(getContext(), this::syncItem);
    }

    public static ChannelUploadsPresenter instance(Context context) {
        if (sInstance == null) {
            sInstance = new ChannelUploadsPresenter(context);
        }

        sInstance.setContext(context);

        return sInstance;
    }

    @Override
    public void onViewInitialized() {
        super.onViewInitialized();

        refresh();
    }

    @Override
    public void onViewDestroyed() {
        super.onViewDestroyed();
        disposeActions();
    }

    @Override
    public void onFinish() {
        super.onFinish();

        // Destroy the cache only (!) when user pressed back (e.g. wants to explicitly kill the activity)
        // Otherwise keep the cache to easily restore in case activity is killed by the system.
        disposeActions();
        mChannel = null;
        mPendingGroup = null;
        mBaseGroup = null;
    }

    @Override
    public void onVideoItemSelected(Video item) {
        // NOP
    }

    @Override
    public void onVideoItemClicked(Video item) {
        VideoLoaderController.openVideo(item);
    }

    @Override
    public void onVideoItemLongClicked(Video item) {
        VideoMenuPresenter.instance(getContext()).showMenu(
            item, 
            (videoItem, action) -> {
                if (action == VideoMenuCallback.ACTION_REMOVE_FROM_PLAYLIST) {
                    removeItem(videoItem);
                }
            }
        );
    }

    @Override
    public void onScrollEnd(Video item) {
        if (item == null) {
            Log.e(TAG, "Can't scroll. Video is null.");
            return;
        }

        VideoGroup group = item.getGroup();

        if (group == null) {
            Log.e(TAG, "Can't scroll. VideoGroup is null.");
            return;
        }

        Log.d(TAG, "onScrollEnd: Group title: " + group.getTitle());

        boolean scrollInProgress = mScrollAction != null && !mScrollAction.isDisposed();

        if (!scrollInProgress) {
            continueGroup(group);
        }
    }

    @Override
    public boolean hasPendingActions() {
        return RxHelper.isAnyActionRunning(mScrollAction, mUpdateAction);
    }

    public void openChannel(Video item) {
        // Working with uploads or playlists
        if (item == null || (!item.hasNestedItems() && !item.hasPlaylist())) return;

        clear();

        mChannel = item;

        getViewManager().startView(ChannelUploadsView.class);

        if (getView() != null) {
            update(item);
        }
    }

    public void obtainGroup(Video item, VideoGroupCallback callback) {
        if (item != null && item.mediaItem != null) {
            obtainGroup(item.mediaItem, callback);
        }
    }

    public Observable<MediaGroup> obtainUploadsObservable(Video item) {
        if (item == null) {
            return null;
        }

        if (item.mediaItem == null) {
            item.mediaItem = SimpleMediaItem.from(item);
        }

        disposeActions();

        return item.hasNestedItems() || item.isChannel() ?
               getContentService().getGroupObserve(item.mediaItem != null ? item.mediaItem : SimpleMediaItem.from(item)) :
               item.hasReloadPageKey() ?
               getContentService().getGroupObserve(item.getReloadPageKey()) :
               getMediaItemService().getMetadataObserve(item.videoId, item.playlistId, 0, item.playlistParams)
                       .flatMap(mediaItemMetadata -> Observable.just(findPlaylistRow(mediaItemMetadata)));
    }

    public Video getChannel() {
        return mChannel;
    }

    public void setChannel(Video channel) {
        mChannel = channel;
    }

    private void disposeActions() {
        RxHelper.disposeActions(mUpdateAction, mScrollAction);
        ServiceManager.disposeActions();
        mBrowseProcessor.dispose();
    }

    private void continueGroup(VideoGroup group) {
        disposeActions();

        if (getView() == null) {
            Log.e(TAG, "Can't continue group. The view is null.");
            return;
        }

        if (group == null) {
            Log.e(TAG, "Can't continue group. The group is null.");
            return;
        }

        Log.d(TAG, "continueGroup: start continue group: " + group.getTitle());

        getView().showProgressBar(true);

        MediaGroup mediaGroup = group.getMediaGroup();

        Observable<MediaGroup> continuation;

        continuation = getContentService().continueGroupObserve(mediaGroup);

        mScrollAction = continuation
                .subscribe(
                        continueMediaGroup -> {
                            VideoGroup newGroup = VideoGroup.from(group, continueMediaGroup);
                            getView().update(newGroup);
                            mBrowseProcessor.process(newGroup);
                        },
                        error -> {
                            Log.e(TAG, "continueGroup error: %s", error.getMessage());
                            if (getView() != null) {
                                getView().showProgressBar(false);
                            }
                        },
                        () -> getView().showProgressBar(false)
                );
    }

    private void update(Video item) {
        // Liked music fix - not all videos displayed. The behavior with other playlists is buggy.
        if (Helpers.equals(item.playlistId, Video.PLAYLIST_LIKED_MUSIC)) {
            update(item.getGroup());
        } else {
            update(obtainUploadsObservable(item));
        }
    }

    private void update(Observable<MediaGroup> group) {
        Log.d(TAG, "update: Start loading a group...");

        disposeActions();

        getView().showProgressBar(true);

        mUpdateAction = group
                .subscribe(
                        this::update,
                        error -> {
                            Log.e(TAG, "update error: %s", error.getMessage());
                            getView().showProgressBar(false);
                        },
                        () -> getView().showProgressBar(false)
                );
    }

    public void update(MediaGroup mediaGroup) {
        // The view could be running in the background
        getViewManager().startView(ChannelUploadsView.class);

        if (getView() == null) { // starting from outside (e.g. ServiceManager)
            mPendingGroup = mediaGroup; // start loading from this group
            return;
        }

        mBaseGroup = mBaseGroup != null ? VideoGroup.from(mBaseGroup, mediaGroup) : VideoGroup.from(mediaGroup);
        if (mChannel != null && TextUtils.isEmpty(mBaseGroup.getTitle())) {
            mBaseGroup.setTitle(mChannel.getTitle());
        }
        update(mBaseGroup);
    }

    private void update(VideoGroup group) {
        disposeActions();

        if (getView() == null || group == null) return;

        getView().update(group);
        mBrowseProcessor.process(group);

        // Hide loading as long as first group received
        if (!group.isEmpty()) {
            getView().showProgressBar(false);
        }
    }

    private void obtainGroup(MediaItem mediaItem, VideoGroupCallback callback) {
        Log.d(TAG, "obtainGroup: Start loading group...");

        disposeActions();

        //Observable<MediaGroup> group = getContentService().getGroupObserve(mediaItem);

        //mUpdateAction = group
        mUpdateAction = obtainUploadsObservable(Video.from(mediaItem))
                .subscribe(
                        callback::onGroup,
                        error -> Log.e(TAG, "obtainGroup error: %s", error.getMessage())
                );
    }

    public interface VideoGroupCallback {
        void onGroup(MediaGroup mediaGroup);
    }

    /**
     * Playlist usually is the first row with media items.<br/>
     * NOTE: before playlist may be the video description row
     */
    private MediaGroup findPlaylistRow(MediaItemMetadata mediaItemMetadata) {
        if (mediaItemMetadata == null || mediaItemMetadata.getSuggestions() == null) {
            return null;
        }

        for (MediaGroup group : mediaItemMetadata.getSuggestions()) {
            List<MediaItem> mediaItems = group.getMediaItems();
            if (mediaItems != null && mediaItems.size() > 0) {
                return group;
            }
        }

        return null;
    }

    public void clear() {
        disposeActions();
        if (getView() != null) {
            getView().clear();
        }
        mChannel = null;
        mPendingGroup = null;
        mBaseGroup = null;
    }

    public void refresh() {
        if (getView() == null) return;

        if (mPendingGroup != null) {
            getView().clear();
            update(mPendingGroup);
        } else if (mChannel != null) {
            getView().clear();
            update(mChannel);
        }
    }
}
