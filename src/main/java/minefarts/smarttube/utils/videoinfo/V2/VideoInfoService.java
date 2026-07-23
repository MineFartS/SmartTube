package minefarts.smarttube.utils.videoinfo.V2;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liskovsoft.youtubeapi.common.helpers.AppClient;
import com.liskovsoft.youtubeapi.app.PoTokenGate;

import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.prefs.GlobalPreferences;
import minefarts.smarttube.utils.app.AppService;
import minefarts.smarttube.google.common.helpers.RetrofitHelper;
import minefarts.smarttube.utils.service.internal.MediaServiceData;
import minefarts.smarttube.utils.videoinfo.InitialResponse;
import minefarts.smarttube.utils.videoinfo.models.CaptionTrack;
import minefarts.smarttube.utils.videoinfo.models.TranslationLanguage;
import minefarts.smarttube.utils.videoinfo.models.VideoInfo;
import minefarts.smarttube.utils.videoinfo.models.VideoInfoHls;
import minefarts.smarttube.utils.videoinfo.models.VideoInfoReel;
import minefarts.smarttube.google.common.api.FileApi;
import minefarts.smarttube.utils.formatbuilders.utils.MediaFormatUtils;
import minefarts.smarttube.utils.videoinfo.models.VideoUrlHolder;
import minefarts.smarttube.utils.videoinfo.models.DashInfo;
import minefarts.smarttube.utils.videoinfo.models.DashInfoContent;
import minefarts.smarttube.utils.videoinfo.models.DashInfoHeaders;
import minefarts.smarttube.utils.videoinfo.models.DashInfoUrl;
import minefarts.smarttube.utils.videoinfo.models.formats.AdaptiveVideoFormat;
import minefarts.smarttube.utils.videoinfo.models.formats.VideoFormat;
import minefarts.smarttube.utils.app.playerdata.PlayerDataExtractor;
import minefarts.smarttube.utils.common.helpers.QueryBuilder;
import minefarts.smarttube.CacheManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;

import kotlin.Pair;

public class VideoInfoService {

    private static final String TAG = VideoInfoService.class.getSimpleName();
    
    private static VideoInfoService sInstance;

    public VideoInfoApi mVideoInfoApi;
    public AppService mAppService;
    public DashInfoApi mDashInfoApi;
    public FileApi mFileApi;
    public MediaServiceData mData;
    
    public static VideoInfoService instance() {
        if (sInstance == null) {
            sInstance = new VideoInfoService();
            sInstance.mVideoInfoApi = RetrofitHelper.create(VideoInfoApi.class);
            sInstance.mAppService = AppService.instance();
            sInstance.mDashInfoApi = RetrofitHelper.create(DashInfoApi.class);
            sInstance.mFileApi = RetrofitHelper.create(FileApi.class);
            sInstance.mData = MediaServiceData.instance();
        }

        return sInstance;
    }
    
    // VIDEO_INFO_TV can bypass "Sign in to confirm you're not a bot" (rare case)
    // VIDEO_INFO_WEB_EMBED - the best one, with no occasional 403 errors
    // VIDEO_INFO_IOS can work without NSig.
    // VIDEO_INFO_TV and VIDEO_INFO_TV_EMBED are the only ones working in North America
    // VIDEO_INFO_MWEB - can bypass SABR-only responses
    private final static AppClient[] VIDEO_INFO_TYPE_LIST = {
            AppClient.WEB_EMBED, // Restricted (18+) videos
            //AppClient.ANDROID_SDK_LESS, // doesn't require pot (hangs on cronet!)
            AppClient.ANDROID_REEL, // doesn't require pot and cipher
            AppClient.TV, // Supports auth. Fixes "please sign in" bug!
            AppClient.TV_LEGACY,
            AppClient.TV_DOWNGRADED,
            AppClient.TV_EMBED, // single audio language
            AppClient.TV_SIMPLY,
            AppClient.GEO, // Fix video clip blocked in current location
            AppClient.MWEB, // single audio language
            AppClient.WEB_SAFARI,
            AppClient.WEB, // Fix video clip blocked in current location
    };

    private PlayerDataExtractor mPlayerDataExtractor = null;
    @Nullable
    public AppClient mVideoInfoType = null;
    
    @Nullable
    private AppClient mRecentInfoType = null;
    
    private boolean mAuthBlock;
    private List<TranslationLanguage> mCachedTranslationLanguages;
    private boolean mIsUnplayable;

    public VideoInfo getVideoInfo(String videoId, String clickTrackingParams) {
        if (videoId == null) return null;

        initInfoTypeIfNeeded();

        CacheManager.clear();

        mAuthBlock = true;

        VideoInfo result = firstInfoWith(videoId, clickTrackingParams, info -> !info.isUnplayable());

        if (result == null) 
            result = firstInfoWith(videoId, clickTrackingParams, info -> true);

        if (result == null) {
            Log.e(TAG, "Can't get video info. videoId: %s", videoId);
            return null;
        }

        applyFixesIfNeeded(result, videoId, clickTrackingParams);

        transformFormats(result);

        persistRecentTypeIfNeeded(result);

        mIsUnplayable = result.isUnplayable();

        return result;
    }

    public VideoInfo getAuthVideoInfo(String videoId, String clickTrackingParams) {
        if (videoId == null) return null;

        mAuthBlock = true;

        // Only the tv client supports auth features
        return getVideoInfo(AppClient.TV, videoId, clickTrackingParams);
    }

    private VideoInfo firstInfoWith(
        String videoId, 
        String clickTrackingParams, 
        Function<VideoInfo, Boolean> infoTester
    ) {
        final AppClient beginType = getDefaultClient();
        AppClient nextType = beginType;

        do {
            
            VideoInfo result = getVideoInfoWithRentFix(nextType, videoId, clickTrackingParams);

            if (result != null && infoTester.apply(result))
                return result;

            nextType = Helpers.getNextValue(VIDEO_INFO_TYPE_LIST, nextType);

        } while (nextType != beginType);

        return null;
    }

    private void initInfoTypeIfNeeded() {
        if (mVideoInfoType != null) return;
        
        int videoInfoType = mData.getVideoInfoType();
        if (videoInfoType != -1) {
            mVideoInfoType = videoInfoType < AppClient.values().length ? AppClient.values()[videoInfoType] : null;
            if (!Arrays.asList(VIDEO_INFO_TYPE_LIST).contains(mVideoInfoType)) {
                mVideoInfoType = VIDEO_INFO_TYPE_LIST[0];
                mData.setVideoInfoType(mVideoInfoType != null ? mVideoInfoType.ordinal() : -1);
            }
        } else {
            mVideoInfoType = VIDEO_INFO_TYPE_LIST[0];
        }
    }

    public void switchNextFormat() {
        initInfoTypeIfNeeded();

        // The Premium is likely broken
        if (mData.isFormatEnabled(MediaServiceData.FORMATS_EXTENDED_HLS)) {
            // Skip additional formats fetching that could produce an error
            mData.setFormatEnabled(MediaServiceData.FORMATS_EXTENDED_HLS, false);
            return;
        }
        // And last, try to switch the client
        mVideoInfoType = Helpers.getNextValue(VIDEO_INFO_TYPE_LIST, mVideoInfoType);
        persistVideoInfoType();
    }

    public void switchNextSubtitle() {
        CaptionTrack.sFormat = Helpers.getNextValue(CaptionTrack.CaptionFormat.values(), CaptionTrack.sFormat);
    }

    private VideoInfo getVideoInfoWithRentFix(AppClient client, String videoId, String clickTrackingParams) {
        VideoInfo result = getVideoInfo(client, videoId, clickTrackingParams);

        if (result != null && result.isRent()) {
            Log.e(TAG, "Found rent content. Show trailer instead...");
            result = getVideoInfo(client, result.getTrailerVideoId(), clickTrackingParams);
        }

        return result;
    }

    private VideoInfo getVideoInfo(AppClient client, String videoId, String clickTrackingParams) {
        if (client.isPlaybackBroken()) return null;

        mRecentInfoType = client;

        if (client == AppClient.INITIAL) {
            return InitialResponse.getVideoInfo(videoId, mAuthBlock);
        }

        String videoInfoQuery = new QueryBuilder(client)
            .setVideoId(videoId)
            .setClickTrackingParams(clickTrackingParams)
            .setPoToken(PoTokenGate.getColdStartPoToken(client, videoId))
            .setVisitorData(PoTokenGate.getVisitorData(client))
            .enableGeoFix(client == AppClient.GEO) // may broke other functionality
            .build();

        return getVideoInfo(client, videoInfoQuery);
    }

    private VideoInfo getVideoInfo(AppClient client, String videoInfoQuery) {
        boolean auth = client.isAuthSupported() && mAuthBlock;

        if (client.isReelClient()) {
            Call<VideoInfoReel> wrapper = mVideoInfoApi.getVideoInfoReel(videoInfoQuery, mAppService.getVisitorData(), client.getUserAgent());
            return getVideoInfoReel(wrapper, auth);
        }

        Call<VideoInfo> wrapper = mVideoInfoApi.getVideoInfo(videoInfoQuery, mAppService.getVisitorData(), client.getUserAgent());
        return getVideoInfo(wrapper, auth);
    }

    private @Nullable VideoInfo getVideoInfo(Call<VideoInfo> wrapper, boolean auth) {
        VideoInfo videoInfo = RetrofitHelper.get(wrapper, auth);

        if (videoInfo == null) {
            return null;
        }

        videoInfo.setAuth(auth);

        return videoInfo;
    }

    private @Nullable VideoInfo getVideoInfoReel(Call<VideoInfoReel> wrapper, boolean auth) {
        VideoInfoReel videoInfo = RetrofitHelper.get(wrapper, auth);

        if (videoInfo == null || videoInfo.getVideoInfo() == null) {
            return null;
        }

        videoInfo.getVideoInfo().setAuth(auth);

        return videoInfo.getVideoInfo();
    }

    private void applyFixesIfNeeded(VideoInfo result, String videoId, String clickTrackingParams) {
        
        if (result == null 
            || result.isUnplayable()
            || !result.hasSubtitles() 
            || result.getTranslationLanguages() == null
            || result.getTranslationLanguages().size() >= 100
        ) return;

        Log.d(TAG, "Enable full list of auto generated subtitles...");

        if (mCachedTranslationLanguages == null || mCachedTranslationLanguages.size() < 100) {
            mAuthBlock = false;
            VideoInfo webInfo = null;
            try {
                webInfo = getVideoInfo(AppClient.WEB, videoId, clickTrackingParams);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (webInfo != null) {
                mCachedTranslationLanguages = webInfo.getTranslationLanguages();
            }
        }

        if (mCachedTranslationLanguages != null) {
            result.setTranslationLanguages(mCachedTranslationLanguages);
        }
        
    }

    public void persistVideoInfoType() {
        if (!GlobalPreferences.isInitialized()) return;

        mData.setVideoInfoType(mVideoInfoType != null ? mVideoInfoType.ordinal() : -1);
    }

    private void persistRecentTypeIfNeeded(VideoInfo videoInfo) {
        if (videoInfo == null || videoInfo.isUnplayable() || mRecentInfoType == null || mRecentInfoType == mVideoInfoType) return;

        mVideoInfoType = mRecentInfoType;
        persistVideoInfoType();
    }

    public AppClient getClient() {
        return mRecentInfoType != null ? mRecentInfoType : getDefaultClient();
    }

    @NonNull
    private AppClient getDefaultClient() {
        return mVideoInfoType != null && Arrays.asList(VIDEO_INFO_TYPE_LIST).contains(mVideoInfoType) ? mVideoInfoType : VIDEO_INFO_TYPE_LIST[0];
    }

    protected void transformFormats(VideoInfo videoInfo) {
        if (videoInfo == null || videoInfo.isUnplayable()) return;

        List<? extends VideoFormat> adaptiveFormats = videoInfo.getAdaptiveFormats();
        List<? extends VideoFormat> regularFormats = videoInfo.getRegularFormats();

        List<VideoUrlHolder> urlHolders = new ArrayList<>();
        if (adaptiveFormats != null)
            for (VideoFormat videoFormat : adaptiveFormats) {
                urlHolders.add(videoFormat.getUrlHolder());
            }
        if (regularFormats != null)
            for (VideoFormat videoFormat : regularFormats) {
                urlHolders.add(videoFormat.getUrlHolder());
            }
        urlHolders.add(videoInfo.getUrlHolder());

        List<String> NParams = new ArrayList<>();
        List<String> SParams = new ArrayList<>();
        for (VideoUrlHolder urlHolder : urlHolders) {
            NParams.add(urlHolder.getNParam());
            SParams.add(urlHolder.getSParam());
        }

        if (mPlayerDataExtractor == null)
            mPlayerDataExtractor = mAppService.getPlayerDataExtractor();

        Pair<List<String>, List<String>> result = mPlayerDataExtractor.bulkSigExtract(NParams, SParams);

        if (result != null) {
            applyNParams(urlHolders, result.getFirst());
            applySignatures(urlHolders, result.getSecond());
        }

        String poToken = PoTokenGate.getPoToken(getClient());
        videoInfo.setPoToken(poToken);

        if (poToken != null) {
            for (int i = 0; i < urlHolders.size(); i++) {
                urlHolders.get(i).setPoToken(poToken);
            }
        }

        if (videoInfo.isLive()) {
            Log.d(TAG, "Enable seeking support on live streams...");
            videoInfo.sync(getDashInfo(videoInfo));
        }

        videoInfo.setClient(getClient());
    }

    private static void applySignatures(List<VideoUrlHolder> urlHolders, List<String> signatures) {
        if (signatures == null) return;

        if (signatures.size() != urlHolders.size()) {
            throw new IllegalStateException("Sizes of urlHolders and signatures should match!");
        }

        for (int i = 0; i < urlHolders.size(); i++) {
            urlHolders.get(i).setSignature(signatures.get(i));
        }
    }

    private static void applyNParams(List<VideoUrlHolder> urlHolders, List<String> nParams) {
        if (nParams == null || nParams.isEmpty()) return;

        // All throttled strings has same values
        boolean sameSize = nParams.size() == urlHolders.size();

        for (int i = 0; i < urlHolders.size(); i++) {
            urlHolders.get(i).setNParam(nParams.get(sameSize ? i : 0));
        }
    }

    private DashInfoUrl getDashInfoUrl(String url) {
        if (url == null) return null;

        return RetrofitHelper.get(mDashInfoApi.getDashInfoUrl(url));
    }

    private DashInfoContent getDashInfoContent(String url) {
        if (url == null) return null;

        return RetrofitHelper.get(mDashInfoApi.getDashInfoContent(url));
    }

    private DashInfoHeaders getDashInfoHeaders(String url) {
        if (url == null) return null;

        // Range doesn't work???
        //return RetrofitHelper.getHeaders(mFileApi.getHeaders(url + SMALL_RANGE));
        return new DashInfoHeaders(RetrofitHelper.getHeaders(mFileApi.getHeaders(url)));
    }

    private DashInfo getDashInfo(VideoInfo videoInfo) {
        if (videoInfo == null || videoInfo.getAdaptiveFormats() == null || videoInfo.getAdaptiveFormats().isEmpty()) {
            return null;
        }

        DashInfo info = getCumulativeDashInfo(videoInfo);

        // Do retry. Sometimes the previous try failed?
        if (info == null || info.getSegmentDurationUs() <= 0 || info.getStartTimeMs() <= 0 || info.getStartSegmentNum() < 0) {
            info = getCumulativeDashInfo(videoInfo);
        }

        return info;
    }

    private DashInfo getCumulativeDashInfo(VideoInfo videoInfo) {
        
        AdaptiveVideoFormat format = Helpers.findFirst(
            videoInfo.getAdaptiveFormats(),
            item -> MediaFormatUtils.isAudio(item.getMimeType())
        ); // smallest format

        if (format == null) return null;

        try {
            return getDashInfoHeaders(format.getUrl());
        } catch (ArithmeticException | NumberFormatException | IllegalStateException ex) {
            try {
                return getDashInfoUrl(format.getUrl());
            } catch (ArithmeticException | NumberFormatException exc) {
                // Empty results received. Url isn't available or something like that
                return getDashInfoContent(format.getUrl());
            }
        }
    }

    /**
     * Call this helper method when building the innerTube JSON request object 
     * inside getVideoInfo, browse, or continueGroup requests.
     */
    private Map<String, Object> buildSafeInnerTubeContext() {
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> client = new HashMap<>();

        // Spoof a modern, stable Android TV instance configuration
        client.put("clientName", "ANDROID_TV");
        client.put("clientVersion", "2.17.008"); // Update to current stable Android TV target
        client.put("osName", "Android");
        client.put("osVersion", "11");
        client.put("platform", "TV");
        client.put("hl", "en");
        client.put("gl", "US");
        client.put("utcOffsetMinutes", 0);

        context.put("client", client);
        
        Map<String, Object> user = new HashMap<>();
        user.put("lockedSafetyMode", false);
        context.put("user", user);

        return context;
    }

    /**
     * Ensure your OkHttp/Retrofit construction injects these headers globally 
     * for all InnerTube JSON endpoints.
     */
    public static Map<String, String> getInnerTubeHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("User-Agent", "com.google.android.youtube.tv/2.17.008 (Linux; U; Android 11; Build/RQ3A.210605.005)");
        headers.put("X-Goog-Api-Format-Version", "2");
        headers.put("Origin", "https://youtube.com");
        return headers;
    }

}
