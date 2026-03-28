package smartyoutubetv1.app.models.search;

import smartyoutubetv1.app.models.search.vineyard.Tag;

import java.util.List;

public interface SearchTagsProvider {
    interface ResultsCallback {
        void onResults(List<Tag> results);
    }
    void search(String query, ResultsCallback callback);
}
