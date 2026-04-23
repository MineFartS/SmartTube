package com.liskovsoft.youtubeapi.data;

public interface PlaylistInfo {
    String getTitle();
    String getPlaylistId();
    boolean isSelected();
    int getSize();
    int getCurrentIndex();
}
