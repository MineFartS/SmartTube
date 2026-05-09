package minefarts.smarttube.misc;

import android.content.Context;

import minefarts.smarttube.app.models.data.VideoGroup;

import java.util.ArrayList;

public class BrowseProcessorManager implements BrowseProcessor {
    private final ArrayList<BrowseProcessor> mProcessors;

    public BrowseProcessorManager(Context context, OnItemReady onItemReady) {
        mProcessors = new ArrayList<>();
                
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
