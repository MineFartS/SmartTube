package minefarts.smarttube.utils.data;

import com.liskovsoft.youtubeapi.app.PoTokenGate;

import minefarts.smarttube.utils.data.MediaSubtitle;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.rx.RxHelper;
import minefarts.smarttube.utils.app.AppService;
import minefarts.smarttube.utils.common.helpers.AppClient;
import minefarts.smarttube.utils.formatbuilders.hlsbuilder.YouTubeUrlListBuilder;
import minefarts.smarttube.utils.formatbuilders.mpdbuilder.YouTubeMPDBuilder;
import minefarts.smarttube.utils.formatbuilders.storyboard.YouTubeStoryParser;
import minefarts.smarttube.utils.formatbuilders.storyboard.YouTubeStoryParser.Storyboard;
import minefarts.smarttube.utils.videoinfo.models.CaptionTrack;
import minefarts.smarttube.utils.videoinfo.models.VideoDetails;
import minefarts.smarttube.utils.videoinfo.models.VideoInfo;
import minefarts.smarttube.utils.videoinfo.models.formats.AdaptiveVideoFormat;
import minefarts.smarttube.utils.videoinfo.models.formats.RegularVideoFormat;
import minefarts.smarttube.utils.service.data.MediaFormat;
import minefarts.smarttube.utils.service.data.YouTubeMediaSubtitle;
import minefarts.smarttube.utils.service.data.MediaItemStoryboard;

import io.reactivex.Observable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MediaItemFormatInfo {

    private static final String TAG = MediaItemFormatInfo.class.getSimpleName();

    private String mLengthSeconds;
    private String mTitle;
    private String mAuthor;
    private String mViewCount;
    private String mDescription;
    private String mVideoId;
    private String mChannelId;
    private boolean mIsLive;
    private boolean mIsLiveContent;
    private boolean mIsLowLatencyLiveStream;
    private boolean mIsStreamSeekable;
    private List<MediaFormat> mAdaptiveFormats;
    private List<MediaFormat> mUrlFormats;
    private List<MediaSubtitle> mSubtitles;
    private String mDashManifestUrl;
    private String mHlsManifestUrl;
    private String mEventId; // used in tracking
    private String mVisitorMonitoringData; // used in tracking
    private String mOfParam; // used in tracking
    private String mStoryboardSpec;
    private boolean mIsUnplayable;
    private String mPlayabilityStatus;
    private String mStartTimestamp;
    private String mUploadDate;
    private long mStartTimeMs;
    private int mStartSegmentNum;
    private int mSegmentDurationUs;
    private boolean mHasExtendedHlsFormats;
    private float mLoudnessDb;
    private boolean mContainsAdaptiveVideoFormats;
    private boolean mIsAuth;
    private boolean mIsSynced;
    private boolean mIsUnknownError;
    private String mPaidContentText;
    private String mClickTrackingParams;
    private String mVideoPlaybackUstreamerConfig;
    private String mServerAbrStreamingUrl;
    private String mPoToken;
    private AppClient mClient;

    private static final Pattern durationPattern1 = Pattern.compile("dur=([^&]*)");
    private static final Pattern durationPattern2 = Pattern.compile("/dur/([^/]*)");

    public static MediaItemFormatInfo from(VideoInfo videoInfo) {
        if (videoInfo == null) {
            return null;
        }

        MediaItemFormatInfo formatInfo = new MediaItemFormatInfo();

        if (videoInfo.getAdaptiveFormats() != null) {
            formatInfo.mContainsAdaptiveVideoFormats = videoInfo.containsAdaptiveVideoInfo();

            formatInfo.mAdaptiveFormats = new ArrayList<>();

            for (AdaptiveVideoFormat format : videoInfo.getAdaptiveFormats()) {
                formatInfo.mAdaptiveFormats.add(MediaFormat.from(format));
            }
        }

        if (videoInfo.getRegularFormats() != null) {
            formatInfo.mUrlFormats = new ArrayList<>();

            for (RegularVideoFormat format : videoInfo.getRegularFormats()) {
                formatInfo.mUrlFormats.add(MediaFormat.from(format));
            }
        }

        VideoDetails videoDetails = videoInfo.getVideoDetails();

        if (videoDetails != null) {
            formatInfo.mLengthSeconds = videoDetails.getLengthSeconds();
            formatInfo.mVideoId = videoDetails.getVideoId();
            formatInfo.mViewCount = videoDetails.getViewCount();
            formatInfo.mTitle = videoDetails.getTitle();
            formatInfo.mDescription = videoDetails.getShortDescription();
            formatInfo.mChannelId = videoDetails.getChannelId();
            formatInfo.mAuthor = videoDetails.getAuthor();
            formatInfo.mIsLive = videoDetails.isLive();
            formatInfo.mIsLiveContent = videoDetails.isLiveContent();
            formatInfo.mIsLowLatencyLiveStream = videoDetails.isLowLatencyLiveStream();
        }

        formatInfo.mDashManifestUrl = videoInfo.getDashManifestUrl();
        formatInfo.mHlsManifestUrl = videoInfo.getHlsManifestUrl();
        // BEGIN Tracking params
        formatInfo.mEventId = videoInfo.getEventId();
        formatInfo.mVisitorMonitoringData = videoInfo.getVisitorMonitoringData();
        formatInfo.mOfParam = videoInfo.getOfParam();
        // END Tracking params
        formatInfo.mStoryboardSpec = videoInfo.getStoryboardSpec();
        formatInfo.mIsUnplayable = videoInfo.isUnplayable();
        formatInfo.mIsAuth = videoInfo.isAuth();
        formatInfo.mIsUnknownError = videoInfo.isUnknownRestricted();
        formatInfo.mPlayabilityStatus = videoInfo.getPlayabilityStatus();
        formatInfo.mIsStreamSeekable = videoInfo.isHfr() || videoInfo.isStreamSeekable();
        formatInfo.mStartTimestamp = videoInfo.getStartTimestamp();
        formatInfo.mUploadDate = videoInfo.getUploadDate();
        formatInfo.mStartTimeMs = videoInfo.getStartTimeMs();
        formatInfo.mStartSegmentNum = videoInfo.getStartSegmentNum();
        formatInfo.mSegmentDurationUs = videoInfo.getSegmentDurationUs();
        formatInfo.mHasExtendedHlsFormats = videoInfo.hasExtendedHlsFormats();
        formatInfo.mLoudnessDb = videoInfo.getLoudnessDb();
        formatInfo.mPaidContentText = videoInfo.getPaidContentText();
        formatInfo.mVideoPlaybackUstreamerConfig = videoInfo.getVideoPlaybackUstreamerConfig();
        formatInfo.mServerAbrStreamingUrl = videoInfo.getServerAbrStreamingUrl();
        formatInfo.mPoToken = videoInfo.getPoToken();
        formatInfo.mClient = videoInfo.getClient();

        List<CaptionTrack> captionTracks = videoInfo.getCaptionTracks();

        if (captionTracks != null) {
            formatInfo.mSubtitles = new ArrayList<>();

            for (CaptionTrack track : captionTracks) {
                formatInfo.mSubtitles.add(YouTubeMediaSubtitle.from(track));
            }
        }

        return formatInfo;
    }

    public interface ClientInfo {
        String getClientName();
        String getClientVersion();
    }

    public List<MediaFormat> getAdaptiveFormats() {
        return mAdaptiveFormats;
    }

    public List<MediaFormat> getUrlFormats() {
        return mUrlFormats;
    }

    public List<MediaSubtitle> getSubtitles() {
        return mSubtitles;
    }

    public String getLengthSeconds() {
        if (mLengthSeconds == null) // try to get duration from video url
            mLengthSeconds = extractDurationFromTrack();
        
        return mLengthSeconds;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getViewCount() {
        return mViewCount;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getVideoId() {
        return mVideoId;
    }

    public String getChannelId() {
        return mChannelId;
    }

    public boolean isLive() {
        return mIsLive;
    }

    public boolean isLiveContent() {
        return mIsLiveContent;
    }
    
    public boolean isLowLatencyStream() {
        return mIsLowLatencyLiveStream;
    }

    public boolean containsMedia() {
        return containsDashUrl() || containsHlsUrl() || mContainsAdaptiveVideoFormats || containsUrlFormats();
    }

    public boolean containsSabrFormats() {
        return mContainsAdaptiveVideoFormats && mAdaptiveFormats.get(0).getFormatType() == MediaFormat.FORMAT_TYPE_SABR;
    }

    public boolean containsDashFormats() {
        return mContainsAdaptiveVideoFormats && mAdaptiveFormats.get(0).getFormatType() == MediaFormat.FORMAT_TYPE_DASH;
    }

    public boolean containsHlsUrl() {
        return mHlsManifestUrl != null;
    }

    public boolean containsDashUrl() {
        return mDashManifestUrl != null;
    }

    public boolean containsUrlFormats() {
        return mUrlFormats != null;
    }

    public boolean hasExtendedHlsFormats() {
        return mHasExtendedHlsFormats;
    }

    public float getVolumeLevel() {
        float result = 1.0f; // the live loudness

        if (mLoudnessDb != 0) {
            // Original tv web: Math.min(1, 10 ** (-loudnessDb / 20))
            // -5db...5db (0.7...1.4) Base formula: normalLevel*10^(-db/20)
            // Low test - R.E.M. and high test - Lindemann
            float normalLevel = (float) Math.pow(10.0f, mLoudnessDb / 20.0f);
            if (normalLevel > 1.95) { // don't normalize?
                // System of a Down - Lonely Day
                //normalLevel = 1.0f;
                normalLevel = 1.5f;
            }
            // Calculate the result as subtract of the video volume and the max volume
            result = 2.0f - normalLevel;
        }

        return result / 2;
    }

    public String getHlsManifestUrl() {
        return mHlsManifestUrl;
    }

    public String getDashManifestUrl() {
        return mDashManifestUrl;
    }

    public InputStream createMpdStream() {
        return YouTubeMPDBuilder.from(this).build();
    }

    public Observable<InputStream> createMpdStreamObservable() {
        return RxHelper.fromCallable(this::createMpdStream);
    }

    public List<String> createUrlList() {
        return YouTubeUrlListBuilder.from(this).buildUriList();
    }

    public MediaItemStoryboard createStoryboard() {
        if (mStoryboardSpec == null) {
            return null;
        }

        YouTubeStoryParser storyParser = YouTubeStoryParser.from(mStoryboardSpec);
        storyParser.setSegmentDurationUs(getSegmentDurationUs());
        // TODO: need to calculate real segment shift for 60 hrs streams (e.g. euronews live)
        storyParser.setStartSegmentNum(getStartSegmentNum());
        Storyboard storyboard = storyParser.extractStory();

        return MediaItemStoryboard.from(storyboard);
    }

    public boolean isUnplayable() {
        return mIsUnplayable;
    }

    public boolean isAuth() {
        return mIsAuth;
    }

    public boolean isSynced() {
        return mIsSynced;
    }

    public boolean isUnknownError() {
        return mIsUnknownError;
    }

    public String getPlayabilityStatus() {
        return mPlayabilityStatus;
    }

    public boolean isStreamSeekable() {
        return mIsStreamSeekable;
    }

    public String getStartTimestamp() {
        return mStartTimestamp;
    }

    public String getUploadDate() {
        return mUploadDate;
    }

    public long getStartTimeMs() {
        return mStartTimeMs;
    }

    public int getStartSegmentNum() {
        return mStartSegmentNum;
    }

    public int getSegmentDurationUs() {
        return mSegmentDurationUs;
    }

    public String getPaidContentText() {
        return mPaidContentText;
    }

    public String getVideoPlaybackUstreamerConfig() {
        return mVideoPlaybackUstreamerConfig;
    }

    public String getServerAbrStreamingUrl() {
        return mServerAbrStreamingUrl;
    }

    public String getPoToken() {
        return mPoToken;
    }

    public ClientInfo getClientInfo() {
        return mClient;
    }

    public String getEventId() {
        return mEventId;
    }

    public String getVisitorMonitoringData() {
        return mVisitorMonitoringData;
    }

    public String getOfParam() {
        return mOfParam;
    }

    public String getClickTrackingParams() {
        return mClickTrackingParams;
    }

    public void setClickTrackingParams(String clickTrackingParams) {
        mClickTrackingParams = clickTrackingParams;
    }

    /**
     * Sync history data<br/>
     * Intended to merge signed and unsigned infos (no-playback fix)
     */
    public void sync(MediaItemFormatInfo formatInfo) {
        mIsSynced = true;

        if (formatInfo == null || Helpers.anyNull(formatInfo.getEventId(), formatInfo.getVisitorMonitoringData(), formatInfo.getOfParam())) return;

        // Intended to merge signed and unsigned infos (no-playback fix)
        mEventId = formatInfo.getEventId();
        mVisitorMonitoringData = formatInfo.getVisitorMonitoringData();
        mOfParam = formatInfo.getOfParam();
        mIsAuth = formatInfo.isAuth();
    }

    /**
     * Extracts time from video url (if present).
     * Url examples:
     * <br/>
     * "http://example.com?dur=544.99&key=val&key2=val2"
     * <br/>
     * "http://example.com/dur/544.99/key/val/key2/val2"
     *
     * @return duration as string
     */
    private String extractDurationFromTrack() {
        if (mAdaptiveFormats == null && mUrlFormats == null) {
            return null;
        }

        String url = null;
        // mMP4Videos
        List<MediaFormat> videos = mAdaptiveFormats != null ? mAdaptiveFormats : mUrlFormats;
        for (MediaFormat item : videos) {
            url = item.getUrl();
            break; // get first item
        }
        String result = Helpers.runMultiMatcher(url, durationPattern1, durationPattern2);

        if (result == null) {
            //throw new IllegalStateException("Videos in the list doesn't have a duration. Content: " + mMP4Videos);
            Log.e(TAG, "Videos in the list doesn't have a duration. Content: " + videos);
        }

        return result;
    }


}
