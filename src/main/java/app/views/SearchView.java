package SmartTubeApp.app.views;

import SmartTubeApp.app.models.data.VideoGroup;
import SmartTubeApp.app.models.search.MediaServiceSearchTagProvider;

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
