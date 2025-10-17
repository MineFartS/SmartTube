package com.liskovsoft.smartyoutubetv2.common.misc;

import android.content.Context;

import com.liskovsoft.mediaserviceinterfaces.ServiceManager;
import com.liskovsoft.mediaserviceinterfaces.MediaItemService;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.VideoGroup;
import com.liskovsoft.smartyoutubetv2.common.prefs.DeArrowData;
import com.liskovsoft.smartyoutubetv2.common.prefs.common.DataChangeBase.OnDataChange;
import com.liskovsoft.youtubeapi.service.YouTubeServiceManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * Processor that enriches Video objects with data provided by the DeArrow service.
 *
 * Responsibilities:
 * - Listen for DeArrow preference changes and update internal flags accordingly.
 * - For each VideoGroup passed to process(), request replacement metadata (title/thumbnail)
 *   for videos that were not processed yet and apply the received values to Video objects.
 * - Notify the host via OnItemReady when a video has been updated so UI can refresh.
 *
 * Notes:
 * - Keeps a Disposable reference to the current network request and disposes it on demand.
 * - Marks videos as deArrowProcessed to avoid redundant network calls.
 */
public class DeArrowProcessor implements OnDataChange, BrowseProcessor {
    private static final String TAG = DeArrowProcessor.class.getSimpleName();

    // Callback used to notify when a video has been updated with DeArrow data.
    private final OnItemReady mOnItemReady;

    // Media item service used to fetch DeArrow metadata for a list of video ids.
    private final MediaItemService mItemService;

    // Preference holder for DeArrow options (replace titles/thumbnails).
    private final DeArrowData mDeArrowData;

    // Cached flags to avoid repeated preference reads on each process() call.
    private boolean mIsReplaceTitlesEnabled;
    private boolean mIsReplaceThumbnailsEnabled;

    // Disposable for the active DeArrow request (if any).
    private Disposable mResult;

    /**
     * Create a new DeArrowProcessor.
     *
     * @param context    application context used to obtain DeArrowData
     * @param onItemReady callback invoked when a Video has been updated
     */
    public DeArrowProcessor(Context context, OnItemReady onItemReady) {
        mOnItemReady = onItemReady;
        ServiceManager service = YouTubeServiceManager.instance();
        mItemService = service.getMediaItemService();
        mDeArrowData = DeArrowData.instance(context);

        // Subscribe to preference changes so processor updates its internal flags.
        mDeArrowData.setOnChange(this);

        initData();
    }

    /**
     * Called when DeArrowData preferences change.
     * Refreshes cached flags used during processing.
     */
    @Override
    public void onDataChange() {
        initData();
    }

    /**
     * (Re)read preference flags into local variables.
     * Keeps process() cheap by avoiding repeated DeArrowData lookups.
     */
    private void initData() {
        mIsReplaceTitlesEnabled = mDeArrowData.isReplaceTitlesEnabled();
        mIsReplaceThumbnailsEnabled = mDeArrowData.isReplaceThumbnailsEnabled();
    }

    /**
     * Process a VideoGroup by requesting DeArrow metadata for unprocessed videos.
     *
     * For each returned DeArrow item the corresponding Video in the group is updated:
     * - deArrowTitle when title replacement is enabled
     * - altCardImageUrl when thumbnail replacement is enabled
     *
     * After applying updates the processor notifies mOnItemReady for the updated Video.
     *
     * If both replacement flags are disabled or the group is empty the method returns early.
     */
    @Override
    public void process(VideoGroup videoGroup) {
        if ((!mIsReplaceTitlesEnabled && !mIsReplaceThumbnailsEnabled) || videoGroup == null || videoGroup.isEmpty()) {
            return;
        }

        List<String> videoIds = getVideoIds(videoGroup);
        if (videoIds.isEmpty()) {
            return;
        }

        // Cancel any previous request before starting a new one.
        RxHelper.disposeActions(mResult);

        // Request DeArrow metadata for collected video ids.
        mResult = mItemService.getDeArrowDataObserve(videoIds)
                .subscribe(deArrowData -> {
                    // Find corresponding Video in the group and apply received data.
                    Video video = videoGroup.findVideoById(deArrowData.getVideoId());
                    if (video == null) {
                        return;
                    }
                    if (mIsReplaceTitlesEnabled) {
                        video.deArrowTitle = deArrowData.getTitle();
                    }
                    if (mIsReplaceThumbnailsEnabled) {
                        video.altCardImageUrl = deArrowData.getThumbnailUrl();
                    }
                    // Notify that an item was updated so UI can refresh accordingly.
                    mOnItemReady.onItemReady(video);
                },
                error -> {
                    // Non-fatal: log and continue. UI will display whatever is available.
                    Log.d(TAG, "DeArrow cannot process the video");
                });
    }

    /**
     * Dispose any active network request held by this processor.
     * Called when the BrowseProcessorManager disposes its processors.
     */
    @Override
    public void dispose() {
        RxHelper.disposeActions(mResult);
    }

    /**
     * Collect IDs of videos from the group that haven't been processed yet.
     * Marks videos as processed to avoid duplicate future requests.
     *
     * @param videoGroup group to scan
     * @return list of video ids to request from DeArrow
     */
    private List<String> getVideoIds(VideoGroup videoGroup) {
        List<String> result = new ArrayList<>();

        for (Video video : videoGroup.getVideos()) {
            if (video.deArrowProcessed) {
                continue;
            }
            video.deArrowProcessed = true;
            if (video.videoId != null) {
                result.add(video.videoId);
            }
        }

        return result;
    }
}
