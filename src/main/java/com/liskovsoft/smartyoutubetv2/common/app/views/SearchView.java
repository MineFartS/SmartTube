package com.liskovsoft.smartyoutubetv2.common.app.views;

/**
 * View contract for search UI. Displays search rows, handles input/voice trigger and updates results.
 */
public interface SearchView {
    void updateSearch(VideoGroup group);
    void clearSearch();
    void clearSearchTags();
    void setTagsProvider(MediaServiceSearchTagProvider provider);
    void showProgressBar(boolean show);
    void startSearch(String searchText);
    String getSearchText();
    void startVoiceRecognition();
    void finishReally();
}
