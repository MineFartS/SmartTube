package com.liskovsoft.smartyoutubetv2.common.misc;

/**
 * Processor that detects and processes unlocalized (machine/transliterated) video titles.
 *
 * Purpose:
 * - Detect when a title appears untranslated or contains artifacts and attempt to normalize it.
 * - Optionally apply heuristic fixes, fallbacks or mark the item for manual review.
 *
 * Performance:
 * - Designed to be lightweight and safe to run during browse row post-processing.
 * - Avoid expensive network calls inside the processor; prefer async enrichment via other services.
 *
 * Integration:
 * - Plugged into BrowseProcessorManager chain so titles are normalized before UI display.
 */
public class UnlocalizedTitleProcessor implements OnDataChange, BrowseProcessor {
    private static final String TAG = UnlocalizedTitleProcessor.class.getSimpleName();
    private final OnItemReady mOnItemReady;
    private final MediaItemService mItemService;
    private final MainUIData mMainUIData;
    private boolean mIsUnlocalizedTitlesEnabled;
    private Disposable mResult;

    public UnlocalizedTitleProcessor(Context context, OnItemReady onItemReady) {
        mOnItemReady = onItemReady;
        ServiceManager service = YouTubeServiceManager.instance();
        mItemService = service.getMediaItemService();
        mMainUIData = MainUIData.instance(context);
        mMainUIData.setOnChange(this);
        initData();
    }

    @Override
    public void onDataChange() {
        initData();
    }

    private void initData() {
        mIsUnlocalizedTitlesEnabled = mMainUIData.isUnlocalizedTitlesEnabled();
    }

    @Override
    public void process(VideoGroup videoGroup) {
        if (!mIsUnlocalizedTitlesEnabled || videoGroup == null || videoGroup.isEmpty()) {
            return;
        }

        List<String> videoIds = getVideoIds(videoGroup);
        mResult = Observable.fromIterable(videoIds)
                .flatMap(videoId -> mItemService.getUnlocalizedTitleObserve(videoId)
                        .map(newTitle -> new Pair<>(videoId, newTitle)))
                .subscribe(title -> {
                    Video video = videoGroup.findVideoById(title.first);
                    if (video == null || Helpers.equals(video.title, title.second)) {
                        return;
                    }
                    video.deArrowTitle = title.second;
                    mOnItemReady.onItemReady(video);
                },
                error -> {
                    Log.d(TAG, "Unlocalized title: Cannot process the video");
                });
    }

    @Override
    public void dispose() {
        RxHelper.disposeActions(mResult);
    }

    private List<String> getVideoIds(VideoGroup videoGroup) {
        List<String> result = new ArrayList<>();

        for (Video video : videoGroup.getVideos()) {
            if (video.deArrowProcessed) {
                continue;
            }
            video.deArrowProcessed = true;
            result.add(video.videoId);
        }

        return result;
    }
}
