package com.liskovsoft.sharedutils.service.data;

import com.liskovsoft.sharedutils.service.data.MediaItemStoryboard;
import com.liskovsoft.sharedutils.formatbuilders.storyboard.YouTubeStoryParser;
import com.liskovsoft.sharedutils.formatbuilders.storyboard.YouTubeStoryParser.Size;
import com.liskovsoft.sharedutils.formatbuilders.storyboard.YouTubeStoryParser.Storyboard;

public class MediaItemStoryboard {

    private final Storyboard mStoryboard;
    private Size mSize;

    public MediaItemStoryboard(Storyboard storyboard) {
        mStoryboard = storyboard;
    }

    public static MediaItemStoryboard from(Storyboard storyboard) {
        if (storyboard == null) {
            return null;
        }

        return new MediaItemStoryboard(storyboard);
    }

    public int getGroupDurationMS() {
        return mStoryboard.getGroupDurationMS();
    }

    public Size getGroupSize() {
        if (mSize == null)
            mSize = mStoryboard.getGroupSize();

        return mSize;
    }

    public String getGroupUrl(int imgNum) {
        return mStoryboard.getGroupUrl(imgNum + mSize.getStartNum());
    }

}
