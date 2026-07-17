package minefarts.smarttube.utils.browse;

import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.app.AppService;
import minefarts.smarttube.utils.browse.models.grid.GridTab;
import minefarts.smarttube.utils.browse.models.grid.GridTabContinuation;
import minefarts.smarttube.utils.browse.models.grid.GridTabList;
import minefarts.smarttube.utils.browse.models.guide.Guide;
import minefarts.smarttube.utils.browse.models.sections.SectionContinuation;
import minefarts.smarttube.utils.browse.models.sections.SectionList;
import minefarts.smarttube.utils.browse.models.sections.SectionTab;
import minefarts.smarttube.utils.browse.models.sections.SectionTabContinuation;
import minefarts.smarttube.utils.browse.models.sections.SectionTabList;
import minefarts.smarttube.google.common.helpers.RetrofitHelper;
import minefarts.smarttube.google.common.helpers.RetrofitOkHttpHelper;
import com.liskovsoft.youtubeapi.common.helpers.AppClient;

import retrofit2.Call;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrowseService {

    private static final String TAG = BrowseService.class.getSimpleName();
    
    private final BrowseApi mBrowseManagerSigned;
    private final AppService mAppService;
    private final BrowseApiHelper mBrowseApi;

    private static BrowseService sInstance;
    private Map<String, Guide> mGuideMap = new HashMap<>();

    private BrowseService() {
        mBrowseManagerSigned = RetrofitHelper.create(BrowseApi.class);
        mAppService = AppService.instance();
        mBrowseApi = BrowseApiHelper.INSTANCE;
    }

    public static BrowseService instance() {
        if (sInstance == null) {
            sInstance = new BrowseService();
        }

        return sInstance;
    }

    public GridTab getSubscriptions() {
        return getGridTab(mBrowseApi.getSubscriptionsQuery(AppClient.WEB));
    }

    public List<GridTab> getSubscribedChannelsByName() {
        List<GridTab> gridTabs = getSubscribedChannelsSection();

        return getPart(gridTabs, 1);
    }

    public List<GridTab> getSubscribedChannelsLastViewed() {
        List<GridTab> gridTabs = getSubscribedChannelsSection();

        if (gridTabs == null) {
            return null;
        }

        List<GridTab> result = getPart(gridTabs, 0);

        // all channels should be unique
        for (GridTab tab : getPart(gridTabs, 1)) {
            if (!result.contains(tab)) {
                result.add(tab);
            }
        }

        return result;
    }

    public List<GridTab> getSubscribedChannelsUpdate() {
        List<GridTab> subscribedChannelsByName = getSubscribedChannelsByName();

        if (subscribedChannelsByName == null) {
            return null;
        }

        Collections.sort(subscribedChannelsByName, (o1, o2) ->
                o1.hasNewContent() && !o2.hasNewContent() ? -1 : !o1.hasNewContent() && o2.hasNewContent() ? 1 : 0);

        return subscribedChannelsByName;
    }

    private List<GridTab> getSubscribedChannelsSection() {
        List<GridTab> gridTabs = getGridTabs(mBrowseApi.getSubscriptionsQuery(AppClient.WEB));
        // Exclude All Subscriptions tab (first one)
        return gridTabs != null ? gridTabs.subList(1, gridTabs.size()) : null;
    }

    public List<GridTab> getSubscribedChannelsAll() {
        return getSubscribedChannelsSection();
    }

    public GridTab getHistory() {
        // Web client version (needs new parser, see history_25.01.2023.json)
        //return getGridTab(BrowseManagerParams.getHistoryQuery());

        return getGridTab(mBrowseApi.getMyLibraryQuery(AppClient.WEB));
    }

    public List<GridTab> getPlaylists() {
        List<GridTab> playlists = getGridTabs(mBrowseApi.getMyLibraryQuery(AppClient.WEB));

        if (playlists != null) {
            GridTab myVideos = playlists.get(1); // save "My videos" for later use
            //GridTab watchLater = playlists.get(2); // save "Watch later" for later use
            playlists.remove(3); // remove "Purchases"
            //playlists.remove(2); // remove "Watch later"
            playlists.remove(1); // remove "My videos"
            playlists.remove(0); // remove "History"
            playlists.add(myVideos); // add "My videos" to the end
            //playlists.add(watchLater); // add "Watch later" to the end
        }

        return playlists;
    }

    public SectionTab getHome() {
        return getSectionTab(mBrowseApi.getHomeQuery(AppClient.WEB));
    }

    public SectionTab getGaming() {
        return getSectionTab(mBrowseApi.getGamingQuery(AppClient.WEB));
    }

    public SectionTab getNews() {
        return getSectionTab(mBrowseApi.getNewsQuery(AppClient.WEB));
    }

    public SectionTab getMusic() {
        return getSectionTab(mBrowseApi.getMusicQuery(AppClient.WEB));
    }

    public SectionList getChannel(String channelId) {
        return getSectionList(mBrowseApi.getChannelQuery(AppClient.WEB, channelId, null));
    }

    public SectionList getChannel(String channelId, String params) {
        return getSectionList(mBrowseApi.getChannelQuery(AppClient.WEB, channelId, params));
    }

    /**
     * Special type of channel that could be found inside Music section (see Liked row More button)
     */
    public GridTab getGridChannel(String channelId) {
        return getGridTab(mBrowseApi.getChannelQuery(AppClient.WEB, channelId, null));
    }

    /**
     * Make synchronized to fix race conditions between launcher channels and section items
     */
    synchronized private List<GridTab> getGridTabs(String query) {
        List<GridTab> result = null;

        Call<GridTabList> wrapper = mBrowseManagerSigned.getGridTabList(query);

        GridTabList browseResult = RetrofitHelper.get(wrapper);

        if (browseResult != null) {
            result = browseResult.getTabs();
        } else {
            Log.e(TAG, "getGridTabs: result is null");
        }

        return result;
    }

    private GridTab getGridTab(String query) {
        List<GridTab> gridTabs = getGridTabs(query);

        return firstWithItems(gridTabs);
    }

    private GridTab firstWithItems(List<GridTab> gridTabs) {
        if (gridTabs == null || gridTabs.isEmpty()) {
            return null;
        }

        for (GridTab tab : gridTabs) {
            if (tab != null && tab.getItemWrappers() != null) {
                return tab;
            }
        }

        return gridTabs.get(0); // fallback to first item (don't know whether it's used somewhere)
    }

    private List<GridTab> getGridTabs(int fromIndex, String query) {
        List<GridTab> gridTabs = getGridTabs(query);

        List<GridTab> result = null;

        if (gridTabs != null) {
            result = new ArrayList<>();

            for (int i = fromIndex; i < gridTabs.size(); i++) {
                GridTab tab = gridTabs.get(i);

                if (tab.isUnselectable()) {
                    continue;
                }

                result.add(tab);
            }
        }

        return result;
    }

    public SectionContinuation continueSection(String nextKey) {
        if (nextKey == null) {
            Log.e(TAG, "continueGridTabResult: next search key is null.");
            return null;
        }

        String query = mBrowseApi.getContinuationQuery(AppClient.TV, nextKey);
        Call<SectionContinuation> wrapper = mBrowseManagerSigned.continueSection(query, mAppService.getVisitorData());

        return RetrofitHelper.get(wrapper);
    }

    public GridTabContinuation continueGridTab(String nextKey) {
        if (nextKey == null) {
            Log.e(TAG, "continueGridTab: next search key is null.");
            return null;
        }

        String query = mBrowseApi.getContinuationQuery(AppClient.TV, nextKey);
        Call<GridTabContinuation> wrapper = mBrowseManagerSigned.continueGridTab(query);

        return RetrofitHelper.get(wrapper);
    }

    public SectionTabContinuation continueSectionTab(String nextKey) {
        if (nextKey == null) {
            Log.e(TAG, "continueGridTabResult: next search key is null.");
            return null;
        }

        String query = mBrowseApi.getContinuationQuery(AppClient.TV, nextKey);

        Call<SectionTabContinuation> wrapper = mBrowseManagerSigned.continueSectionTab(query, mAppService.getVisitorData());

        return RetrofitHelper.get(wrapper);
    }

    private Guide getGuide() {
        Call<Guide> wrapper = mBrowseManagerSigned.getGuide(mBrowseApi.getGuideQuery());

        return RetrofitHelper.get(wrapper);
    }

    public String getSuggestToken() {
        String result = null;

        String authorization = RetrofitOkHttpHelper.getAuthHeaders().get("Authorization");

        Guide guide = mGuideMap.get(authorization);

        if (guide == null) {
            mGuideMap.clear();
            guide = getGuide();

            if (guide != null) {
                mGuideMap.put(authorization, guide);
                result = guide.getSuggestToken();
            }
        } else {
            result = guide.getSuggestToken();
        }

        return result;
    }

    private SectionTabList getSectionTabList(String query) {
        Log.d(TAG, "Getting section tab list for query: %s", query);

        Call<SectionTabList> wrapper = mBrowseManagerSigned.getSectionTabList(query, mAppService.getVisitorData());

        return RetrofitHelper.get(wrapper);
    }

    private SectionTab getSectionTab(String query) {
        SectionTabList tabs = getSectionTabList(query);

        if (tabs == null) {
            Log.e(TAG, "getRowsTab: tabs result is empty");
            return null;
        }

        return firstNotEmpty(tabs);
    }

    private SectionList getSectionList(String query) {
        Call<SectionList> wrapper = mBrowseManagerSigned.getSectionList(query, mAppService.getVisitorData());

        return RetrofitHelper.get(wrapper);
    }

    private SectionTab firstNotEmpty(SectionTabList tabs) {
        SectionTab result = null;

        if (tabs.getTabs() != null) {
            // find first not empty tab
            for (SectionTab tab : tabs.getTabs()) {
                if (tab.getSections() != null) {
                    result = tab;
                    break;
                }
            }
        } else {
            Log.e(TAG, "firstNotEmpty: tabs are empty");
        }

        return result;
    }

    /**
     * Channels are split by different criteria e.g. (popular and alphanumeric order)
     */
    private List<GridTab> getPart(List<GridTab> gridTabs, int partIndex) {
        List<GridTab> azGridTabs = null;

        if (gridTabs != null) {
            azGridTabs = new ArrayList<>();

            int partIndexFound = 0;

            for (GridTab tab : gridTabs) {
                if (tab.isUnselectable()) {
                    partIndexFound++;
                } else if (partIndexFound == partIndex) {
                    azGridTabs.add(tab);
                }
            }

            if (azGridTabs.isEmpty()) {
                azGridTabs = gridTabs;
            }
        }

        return azGridTabs;
    }
}
