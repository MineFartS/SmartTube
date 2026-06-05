package minefarts.smarttube.utils.search;

import minefarts.smarttube.utils.app.AppService;
import minefarts.smarttube.google.common.helpers.RetrofitHelper;
import minefarts.smarttube.google.common.locale.LocaleManager;
import minefarts.smarttube.utils.search.models.SearchResult;
import minefarts.smarttube.utils.search.models.SearchResultContinuation;
import minefarts.smarttube.utils.search.models.SearchTags;
import minefarts.smarttube.utils.prefs.GlobalPreferences;
import minefarts.smarttube.utils.common.helpers.PostDataHelper;
import minefarts.smarttube.utils.data.SearchOptions;
import minefarts.smarttube.utils.helpers.Helpers;

import retrofit2.Call;

import java.util.List;

public class SearchService {
    
    private final SearchApi mSearchApi;
    private final LocaleManager mLocaleManager;
    private final AppService mAppService;

    private static final String FIRST_SEARCH = "\"query\":\"%s\"";
    private static final String FIRST_SEARCH_EXT = "\"query\":\"%s\",\"params\":\"%s\"";
    private static final String CONTINUATION_SEARCH = "\"continuation\":\"%s\"";

    private static final int MAX_PARAMS = 7;

    private static final int UPLOAD_DATE_ALL = 0;
    private static final int UPLOAD_DATE_LAST_HOUR = 1;
    private static final int UPLOAD_DATE_TODAY = 2;
    private static final int UPLOAD_DATE_THIS_WEEK = 3;
    private static final int UPLOAD_DATE_THIS_MONTH = 4;
    private static final int UPLOAD_DATE_THIS_YEAR = 5;

    private static final int DURATION_ANY = 0;
    private static final int DURATION_UNDER_4 = 1;
    private static final int DURATION_BETWEEN_4_20 = 2;
    private static final int DURATION_OVER_20 = 3;

    private static final int TYPE_ANY = 0;
    private static final int TYPE_VIDEO = 1;
    private static final int TYPE_CHANNEL = 2;
    private static final int TYPE_PLAYLIST = 3;
    private static final int TYPE_MOVIE = 4;

    private static final int FEATURE_ANY = 0;
    private static final int FEATURE_LIVE = 1;
    private static final int FEATURE_4K = 2;
    private static final int FEATURE_HDR = 3;
    private static final int FEATURE_LIVE_4K = 4;
    private static final int FEATURE_4K_HDR = 5;
    private static final int FEATURE_LIVE_4K_HDR = 6;

    private static final int SORT_BY_RELEVANCE = 0;
    private static final int SORT_BY_UPLOAD_DATE = 1;
    private static final int SORT_BY_VIEW_COUNT = 2;
    private static final int SORT_BY_RATING = 3;

    private static String[][][][][] sParams;

    public SearchService() {
        mSearchApi = RetrofitHelper.create(SearchApi.class);
        mLocaleManager = LocaleManager.instance();
        mAppService = AppService.instance();
    }

    private static SearchService sInstance;

    public static SearchService instance() {
        if (sInstance == null)
            sInstance = new SearchService();

        return sInstance;
    }

    /**
     * Method uses results from the {@link #getSearch(String)} call
     * @return video items
     */
    public SearchResultContinuation continueSearch(String nextSearchPageKey) {
        if (nextSearchPageKey == null) return null;

        Call<SearchResultContinuation> wrapper = mSearchApi.continueSearchResult(
            getContinuationQuery(nextSearchPageKey)
        );
        
        return RetrofitHelper.get(wrapper);
    }

    public SearchResult getSearch(String searchText) {
        return getSearch(searchText, -1);
    }

    public SearchResult getSearch(String searchText, int options) {

        Call<SearchResult> wrapper = mSearchApi.getSearchResult(
            getSearchQuery(searchText, options), 
            mAppService.getVisitorData()
        );
        
        return RetrofitHelper.get(wrapper);
    }

    public List<String> getSearchTags(String searchText) {
       
        if (searchText == null)
            searchText = "";

        Call<SearchTags> wrapper = mSearchApi.getSearchTags(searchText);

        SearchTags searchTags = RetrofitHelper.get(wrapper);

        if (searchTags != null)
            return searchTags.getSearchTags();

        return null;
    }

        public static String getSearchQuery(String searchText) {
        return getSearchQuery(searchText, -1);
    }

    public static String getSearchQuery(String searchText, int options) {
        String params = toParams(options);
        String search = params != null ?
                String.format(FIRST_SEARCH_EXT, escape(searchText), params) : String.format(FIRST_SEARCH, escape(searchText));
        return PostDataHelper.createQueryTV(search);
    }

    /**
     * Get data param for the next search
     * @param nextPageKey {@link SearchResult#getNextPageKey()}
     * @return data param
     */
    public static String getContinuationQuery(String nextPageKey) {
        String continuation = String.format(CONTINUATION_SEARCH, nextPageKey);
        return PostDataHelper.createQueryTV(continuation);
    }

    private static String escape(String text) {
        return text
            .replaceAll("'", "\\\\'")
            .replaceAll("\"", "\\\\\"");
    }

    private static void init() {
        if (sParams != null) return;

        sParams = new String[MAX_PARAMS][MAX_PARAMS][MAX_PARAMS][MAX_PARAMS][MAX_PARAMS];

        // Single criteria

        sParams[UPLOAD_DATE_LAST_HOUR][DURATION_ANY][TYPE_ANY][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgQIARAB";
        sParams[UPLOAD_DATE_TODAY][DURATION_ANY][TYPE_ANY][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgQIAhAB";
        sParams[UPLOAD_DATE_THIS_WEEK][DURATION_ANY][TYPE_ANY][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgQIAxAB";
        sParams[UPLOAD_DATE_THIS_MONTH][DURATION_ANY][TYPE_ANY][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgQIBBAB";
        sParams[UPLOAD_DATE_THIS_YEAR][DURATION_ANY][TYPE_ANY][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgQIBRAB";

        sParams[UPLOAD_DATE_ALL][DURATION_UNDER_4][TYPE_ANY][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgQQARgB";
        sParams[UPLOAD_DATE_ALL][DURATION_BETWEEN_4_20][TYPE_ANY][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgQQARgD";
        sParams[UPLOAD_DATE_ALL][DURATION_OVER_20][TYPE_ANY][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgQQARgC";

        sParams[UPLOAD_DATE_ALL][DURATION_ANY][TYPE_VIDEO][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgIQAQ%3D%3D";
        sParams[UPLOAD_DATE_ALL][DURATION_ANY][TYPE_CHANNEL][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgIQAg%3D%3D";
        sParams[UPLOAD_DATE_ALL][DURATION_ANY][TYPE_PLAYLIST][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgIQAw%3D%3D";
        sParams[UPLOAD_DATE_ALL][DURATION_ANY][TYPE_MOVIE][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgIQBA%3D%3D";

        sParams[UPLOAD_DATE_ALL][DURATION_ANY][TYPE_ANY][FEATURE_LIVE][SORT_BY_RELEVANCE] = "EgJAAQ%3D%3D";
        sParams[UPLOAD_DATE_ALL][DURATION_ANY][TYPE_ANY][FEATURE_4K][SORT_BY_RELEVANCE] = "EgJwAQ%3D%3D";
        sParams[UPLOAD_DATE_ALL][DURATION_ANY][TYPE_ANY][FEATURE_HDR][SORT_BY_RELEVANCE] = "EgPIAQE%3D";
        sParams[UPLOAD_DATE_ALL][DURATION_ANY][TYPE_ANY][FEATURE_LIVE_4K][SORT_BY_RELEVANCE] = "EgRAAXAB";
        sParams[UPLOAD_DATE_ALL][DURATION_ANY][TYPE_ANY][FEATURE_4K_HDR][SORT_BY_RELEVANCE] = "EgVwAcgBAQ%3D%3D";
        sParams[UPLOAD_DATE_ALL][DURATION_ANY][TYPE_ANY][FEATURE_LIVE_4K_HDR][SORT_BY_RELEVANCE] = "EgdAAXAByAEB";

        sParams[UPLOAD_DATE_ALL][DURATION_ANY][TYPE_ANY][FEATURE_ANY][SORT_BY_UPLOAD_DATE] = "CAI%3D";
        sParams[UPLOAD_DATE_ALL][DURATION_ANY][TYPE_ANY][FEATURE_ANY][SORT_BY_VIEW_COUNT] = "CAMSAhAB";
        sParams[UPLOAD_DATE_ALL][DURATION_ANY][TYPE_ANY][FEATURE_ANY][SORT_BY_RATING] = "CAESAhAB";


        // Various combinations

        sParams[UPLOAD_DATE_TODAY][DURATION_UNDER_4][TYPE_ANY][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgYIAhABGAE%3D";
        sParams[UPLOAD_DATE_TODAY][DURATION_BETWEEN_4_20][TYPE_ANY][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgYIAhABGAM%3D";
        sParams[UPLOAD_DATE_TODAY][DURATION_OVER_20][TYPE_ANY][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgYIAhABGAI%3D";

        sParams[UPLOAD_DATE_THIS_WEEK][DURATION_UNDER_4][TYPE_ANY][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgYIAxABGAE%3D";
        sParams[UPLOAD_DATE_THIS_WEEK][DURATION_BETWEEN_4_20][TYPE_ANY][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgYIAxABGAM%3D";
        sParams[UPLOAD_DATE_THIS_WEEK][DURATION_OVER_20][TYPE_ANY][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgYIAxABGAI%3D";

        sParams[UPLOAD_DATE_THIS_MONTH][DURATION_UNDER_4][TYPE_ANY][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgYIBBABGAE%3D";
        sParams[UPLOAD_DATE_THIS_MONTH][DURATION_BETWEEN_4_20][TYPE_ANY][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgYIBBABGAM%3D";
        sParams[UPLOAD_DATE_THIS_MONTH][DURATION_OVER_20][TYPE_ANY][FEATURE_ANY][SORT_BY_RELEVANCE] = "EgYIBBABGAI%3D";

        sParams[UPLOAD_DATE_THIS_WEEK][DURATION_UNDER_4][TYPE_ANY][FEATURE_ANY][SORT_BY_UPLOAD_DATE] = "CAISBBABGAI%3D";
        sParams[UPLOAD_DATE_THIS_WEEK][DURATION_BETWEEN_4_20][TYPE_ANY][FEATURE_ANY][SORT_BY_VIEW_COUNT] = "CAMSBBABGAI%3D";
        sParams[UPLOAD_DATE_THIS_WEEK][DURATION_OVER_20][TYPE_ANY][FEATURE_ANY][SORT_BY_RATING] = "CAESBBABGAI%3D";
    }

    private static String toParams(int options) {
        if (options == -1 || options == 0) return null;

        init();

        return sParams[getUploadDateIndex(options)][getDurationIndex(options)][getTypeIndex(options)][getFeaturesIndex(options)][getSortByIndex(options)];
    }

    private static int getUploadDateIndex(int options) {
        int index = UPLOAD_DATE_ALL;

        if (Helpers.check(options, SearchOptions.UPLOAD_DATE_LAST_HOUR)) {
            index = UPLOAD_DATE_LAST_HOUR;
        } else if (Helpers.check(options, SearchOptions.UPLOAD_DATE_TODAY)) {
            index = UPLOAD_DATE_TODAY;
        } else if (Helpers.check(options, SearchOptions.UPLOAD_DATE_THIS_WEEK)) {
            index = UPLOAD_DATE_THIS_WEEK;
        } else if (Helpers.check(options, SearchOptions.UPLOAD_DATE_THIS_MONTH)) {
            index = UPLOAD_DATE_THIS_MONTH;
        } else if (Helpers.check(options, SearchOptions.UPLOAD_DATE_THIS_YEAR)) {
            index = UPLOAD_DATE_THIS_YEAR;
        }

        return index;
    }

    private static int getDurationIndex(int options) {
        int index = DURATION_ANY;

        if (Helpers.check(options, SearchOptions.DURATION_UNDER_4)) {
            index = DURATION_UNDER_4;
        } else if (Helpers.check(options, SearchOptions.DURATION_BETWEEN_4_20)) {
            index = DURATION_BETWEEN_4_20;
        } else if (Helpers.check(options, SearchOptions.DURATION_OVER_20)) {
            index = DURATION_OVER_20;
        }

        return index;
    }

    private static int getTypeIndex(int options) {
        int index = TYPE_ANY;

        if (Helpers.check(options, SearchOptions.TYPE_VIDEO)) {
            index = TYPE_VIDEO;
        } else if (Helpers.check(options, SearchOptions.TYPE_CHANNEL)) {
            index = TYPE_CHANNEL;
        } else if (Helpers.check(options, SearchOptions.TYPE_PLAYLIST)) {
            index = TYPE_PLAYLIST;
        } else if (Helpers.check(options, SearchOptions.TYPE_MOVIE)) {
            index = TYPE_MOVIE;
        }

        return index;
    }

    private static int getFeaturesIndex(int options) {
        int index = FEATURE_ANY;

        if (Helpers.check(options, SearchOptions.FEATURE_LIVE | SearchOptions.FEATURE_4K | SearchOptions.FEATURE_HDR)) {
            index = FEATURE_LIVE_4K_HDR;
        } else if (Helpers.check(options, SearchOptions.FEATURE_LIVE | SearchOptions.FEATURE_4K)) {
            index = FEATURE_LIVE_4K;
        } else if (Helpers.check(options, SearchOptions.FEATURE_4K | SearchOptions.FEATURE_HDR)) {
            index = FEATURE_4K_HDR;
        } else if (Helpers.check(options, SearchOptions.FEATURE_LIVE)) {
            index = FEATURE_LIVE;
        } else if (Helpers.check(options, SearchOptions.FEATURE_4K)) {
            index = FEATURE_4K;
        } else if (Helpers.check(options, SearchOptions.FEATURE_HDR)) {
            index = FEATURE_HDR;
        }

        return index;
    }

    private static int getSortByIndex(int options) {
        int index = SORT_BY_RELEVANCE;

        if (Helpers.check(options, SearchOptions.SORT_BY_UPLOAD_DATE)) {
            index = SORT_BY_UPLOAD_DATE;
        } else if (Helpers.check(options, SearchOptions.SORT_BY_VIEW_COUNT)) {
            index = SORT_BY_VIEW_COUNT;
        } else if (Helpers.check(options, SearchOptions.SORT_BY_RATING)) {
            index = SORT_BY_RATING;
        }

        return index;
    }

}
