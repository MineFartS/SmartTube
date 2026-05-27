package minefarts.smarttube.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import minefarts.sharedutils.helpers.Helpers;

public class SearchData {

    private static final String SEARCH_DATA = "search_data";
    
    @SuppressLint("StaticFieldLeak")
    private static SearchData sInstance;
    
    private final AppPrefs mAppPrefs;

    private int mSearchOptions;
    private boolean mIsTrendingSearchesEnabled;
    private boolean mIsPopularSearchesDisabled;

    private SearchData(Context context) {
        mAppPrefs = AppPrefs.instance(context);
        restoreState();
    }

    public static SearchData instance(Context context) {
        if (sInstance == null) {
            sInstance = new SearchData(context.getApplicationContext());
        }

        return sInstance;
    }

    public int getSearchOptions() {
        return mSearchOptions;
    }

    public void setSearchOptions(int searchOptions) {
        mSearchOptions = searchOptions;
        persistState();
    }

    public void setTrendingSearchesEnabled(boolean enabled) {
        mIsTrendingSearchesEnabled = enabled;
        persistState();
    }

    public boolean isTrendingSearchesEnabled() {
        return mIsTrendingSearchesEnabled;
    }

    public boolean isPopularSearchesDisabled() {
        return mIsPopularSearchesDisabled;
    }

    public void setPopularSearchesDisabled(boolean disabled) {
        mIsPopularSearchesDisabled = disabled;
        persistState();
    }

    private void restoreState() {

        String data = mAppPrefs.getData(SEARCH_DATA);
        String[] split = Helpers.splitData(data);

        /* 0 */ mSearchOptions = Helpers.parseInt(split, 0, 0);
        /* 2 */ mIsTrendingSearchesEnabled = Helpers.parseBoolean(split, 2, true);
        /* 3 */ mIsPopularSearchesDisabled = Helpers.parseBoolean(split, 3, false);
    
    }

    public void persistState() {
        mAppPrefs.setData(
            SEARCH_DATA,
            Helpers.mergeData(
            /* 0 */ mSearchOptions, 
            /* 1 */ null, 
            /* 2 */ mIsTrendingSearchesEnabled,
            /* 3 */ mIsPopularSearchesDisabled
            )
        );
    }
}
