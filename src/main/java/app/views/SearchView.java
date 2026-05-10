package minefarts.smarttube.app.views;

import minefarts.smarttube.app.models.data.VideoGroup;
import minefarts.smarttube.app.models.search.MediaServiceSearchTagProvider;

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
