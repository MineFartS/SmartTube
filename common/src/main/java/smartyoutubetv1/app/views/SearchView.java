package smartyoutubetv1.app.views;

import smartyoutubetv1.app.models.data.VideoGroup;
import smartyoutubetv1.app.models.search.MediaServiceSearchTagProvider;

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
