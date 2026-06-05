package minefarts.smarttube.utils.search;

import minefarts.smarttube.utils.prefs.GlobalPreferences;
import minefarts.smarttube.utils.search.models.SearchResult;

import java.util.List;

public class SearchServiceWrapper extends SearchService {
    private static SearchServiceWrapper sInstance;

    public static SearchServiceWrapper instance() {
        if (sInstance == null) {
            sInstance = new SearchServiceWrapper();
        }

        return sInstance;
    }

    @Override
    public SearchResult getSearch(String searchText) {
        saveTagIfNeeded(searchText);

        return super.getSearch(searchText);
    }

    @Override
    public SearchResult getSearch(String searchText, int options) {
        saveTagIfNeeded(searchText);

        return super.getSearch(searchText, options);
    }

    @Override
    public List<String> getSearchTags(String searchText) {
        List<String> result = super.getSearchTags(searchText);

        if (result == null || result.isEmpty()) {
            return getTagsIfNeeded();
        }

        return result;
    }

    private List<String> getTagsIfNeeded() {
        if (GlobalPreferences.sInstance != null) {
            return SearchTagStorage.getTags();
        }

        return null;
    }

    private void saveTagIfNeeded(String searchText) {
        if (GlobalPreferences.sInstance != null) {
            SearchTagStorage.saveTag(searchText);
        }
    }
}
