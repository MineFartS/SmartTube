package com.liskovsoft.smartyoutubetv2.common.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import com.liskovsoft.sharedutils.helpers.Helpers;

public class SearchData {

    private static final String SEARCH_DATA = "search_data";
    
    @SuppressLint("StaticFieldLeak")
    private static SearchData sInstance;
    
    private final AppPrefs mAppPrefs;

    private int mSearchOptions;
    private boolean mIsTempBackgroundModeEnabled;
    private Class<?> mTempBackgroundModeClass;
    private boolean mIsTrendingSearchesEnabled;
    private boolean mIsPopularSearchesDisabled;

    private SearchData(Context context) {
        mAppPrefs = AppPrefs.instance(context);
        restoreData();
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
        persistData();
    }

    public void setTrendingSearchesEnabled(boolean enabled) {
        mIsTrendingSearchesEnabled = enabled;
        persistData();
    }

    public boolean isTrendingSearchesEnabled() {
        return mIsTrendingSearchesEnabled;
    }

    public boolean isTempBackgroundModeEnabled() {
        return mIsTempBackgroundModeEnabled;
    }

    public void setTempBackgroundModeEnabled(boolean enabled) {
        mIsTempBackgroundModeEnabled = enabled;
        persistData();
    }

    public Class<?> getTempBackgroundModeClass() {
        return mTempBackgroundModeClass;
    }

    public void setTempBackgroundModeClass(Class<?> clazz) {
        mTempBackgroundModeClass = clazz;
    }

    public boolean isPopularSearchesDisabled() {
        return mIsPopularSearchesDisabled;
    }

    public void setPopularSearchesDisabled(boolean disabled) {
        mIsPopularSearchesDisabled = disabled;
        persistData();
    }

    private void restoreData() {

        String data = mAppPrefs.getData(SEARCH_DATA);
        String[] split = Helpers.splitData(data);

        mSearchOptions = Helpers.parseInt(split, 1, 0);
        mIsTempBackgroundModeEnabled = Helpers.parseBoolean(split, 4, false);
        mIsTrendingSearchesEnabled = Helpers.parseBoolean(split, 7, true);
        mIsPopularSearchesDisabled = Helpers.parseBoolean(split, 9, false);
    
    }

    private void persistData() {
        mAppPrefs.setData(
            SEARCH_DATA,
            Helpers.mergeData(
                mSearchOptions, 
                mIsTempBackgroundModeEnabled, 
                null, 
                mIsTrendingSearchesEnabled,
                mIsPopularSearchesDisabled
            )
        );
    }
}
