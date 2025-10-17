package com.liskovsoft.smartyoutubetv2.common.misc;

/**
 * Manager that composes multiple BrowseProcessor implementations and applies them
 * to VideoGroup instances. Keeps processors in a list and forwards process/dispose calls.
 */

/**
 * Coordinates background processing of browse rows (VideoGroup).
 * Responsible for scheduling lightweight post-processing (e.g. enriching items,
 * prefetching thumbnails, applying local filters) to avoid blocking UI updates.
 */

import android.content.Context;

import com.liskovsoft.smartyoutubetv2.common.app.models.data.VideoGroup;

import java.util.ArrayList;

public class BrowseProcessorManager implements BrowseProcessor {
    private final ArrayList<BrowseProcessor> mProcessors;

    public BrowseProcessorManager(Context context, OnItemReady onItemReady) {
        mProcessors = new ArrayList<>();
        mProcessors.add(new DeArrowProcessor(context, onItemReady));
        mProcessors.add(new UnlocalizedTitleProcessor(context, onItemReady));
    }

    @Override
    public void process(VideoGroup videoGroup) {
        for (BrowseProcessor processor : mProcessors) {
            processor.process(videoGroup);
        }
    }

    @Override
    public void dispose() {
        for (BrowseProcessor processor : mProcessors) {
            processor.dispose();
        }
    }
}
