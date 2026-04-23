package com.liskovsoft.youtubeapi.data;

public interface MediaItemStoryboard {
    int getGroupDurationMS();
    Size getGroupSize();
    String getGroupUrl(int imgNum);
    interface Size {
        int getDurationEachMS();
        int getStartNum();
        int getWidth();
        int getHeight();
        int getRowCount();
        int getColCount();
    }
}
