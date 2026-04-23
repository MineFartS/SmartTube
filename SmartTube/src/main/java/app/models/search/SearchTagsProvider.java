package SmartTubeApp.app.models.search;

import SmartTubeApp.app.models.search.vineyard.Tag;

import java.util.List;

public interface SearchTagsProvider {
    interface ResultsCallback {
        void onResults(List<Tag> results);
    }
    void search(String query, ResultsCallback callback);
}
