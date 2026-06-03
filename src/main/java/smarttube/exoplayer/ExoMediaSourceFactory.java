package minefarts.smarttube.exoplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.NonNull;

import minefarts.smarttube.C;
import minefarts.smarttube.okhttp.OkHttpDataSourceFactory;
import minefarts.smarttube.extractor.DefaultExtractorsFactory;
import minefarts.smarttube.source.ExtractorMediaSource;
import minefarts.smarttube.source.MediaSource;
import minefarts.smarttube.dash.DashChunkSource;
import minefarts.smarttube.dash.DashMediaSource;
import minefarts.smarttube.dash.DefaultDashChunkSource;
import minefarts.smarttube.dash.manifest.DashManifest;
import minefarts.smarttube.dash.manifest.DashManifestParser;
import minefarts.smarttube.dash.manifest.DashManifestParser2;
import minefarts.smarttube.dash.manifest.Period;
import minefarts.smarttube.dash.manifest.ProgramInformation;
import minefarts.smarttube.dash.manifest.UtcTimingElement;
import minefarts.smarttube.hls.HlsMediaSource;
import minefarts.smarttube.sabr.DefaultSabrChunkSource;
import minefarts.smarttube.sabr.SabrChunkSource;
import minefarts.smarttube.sabr.SabrMediaSource;
import minefarts.smarttube.sabr.manifest.SabrManifest;
import minefarts.smarttube.sabr.manifest.SabrManifestParser;
import minefarts.smarttube.ss.DefaultSsChunkSource;
import minefarts.smarttube.ss.SsMediaSource;
import minefarts.smarttube.upstream.DataSource;
import minefarts.smarttube.upstream.DataSource.Factory;
import minefarts.smarttube.upstream.DefaultDataSourceFactory;
import minefarts.smarttube.upstream.DefaultHttpDataSourceFactory;
import minefarts.smarttube.upstream.HttpDataSource;
import minefarts.smarttube.upstream.HttpDataSource.BaseFactory;
import minefarts.smarttube.utils.data.MediaItemFormatInfo;
import minefarts.smarttube.utils.helpers.FileHelpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.okhttp.OkHttpManager;
import minefarts.smarttube.exoplayer.errors.DashDefaultLoadErrorHandlingPolicy;
import minefarts.smarttube.exoplayer.errors.SabrDefaultLoadErrorHandlingPolicy;
import minefarts.smarttube.exoplayer.errors.TrackErrorFixer;
import minefarts.smarttube.prefs.PlayerTweaksData;
import minefarts.smarttube.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executors;

public class ExoMediaSourceFactory {

    private static final String TAG = ExoMediaSourceFactory.class.getSimpleName();

    private static final int MAX_SEGMENTS_PER_LOAD = 3; // default - 1 (1-5)

    public static final String USER_AGENT_TV  = "Mozilla/5.0 (Linux armeabi-v7a; Android 7.1.2; Fire OS 6.0) Cobalt/22.lts.3.306369-gold (unlike Gecko) v8/8.8.278.8-jit gles Starboard/13, Amazon_ATV_mediatek8695_2019/NS6294 (Amazon, AFTMM, Wireless) com.amazon.firetv.youtube/22.3.r2.v66.0";
    public static final String USER_AGENT_WEB = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private final Context mContext;
    private static final Uri DASH_MANIFEST_URI = Uri.parse("https://example.com/test.mpd");
    private static final String DASH_MANIFEST_EXTENSION = "mpd";
    private static final String HLS_PLAYLIST_EXTENSION = "m3u8";

    private TrackErrorFixer mTrackErrorFixer;
    private Factory mMediaDataSourceFactory;

    public ExoMediaSourceFactory(Context context) {
        mContext = context;
    }

    public MediaSource fromSabrFormatInfo(MediaItemFormatInfo formatInfo) {

        SabrManifestParser parser = new SabrManifestParser();

        // Are you using FrameworkSampleSource or ExtractorSampleSource when you build your player?
        SabrMediaSource sabrSource = new SabrMediaSource.Factory(getSabrChunkSourceFactory(), null)
            .setLoadErrorHandlingPolicy(new SabrDefaultLoadErrorHandlingPolicy())
            .createMediaSource(parser.parse(formatInfo));
        
        if (mTrackErrorFixer != null)
            sabrSource.addEventListener(Utils.sHandler, mTrackErrorFixer);
        
        return sabrSource;
    }

    public MediaSource fromDashFormatInfo(MediaItemFormatInfo formatInfo) {
        
        // Are you using FrameworkSampleSource or ExtractorSampleSource when you build your player?
        DashMediaSource dashSource = new DashMediaSource.Factory(getDashChunkSourceFactory(), null)
            .setLoadErrorHandlingPolicy(new DashDefaultLoadErrorHandlingPolicy())
            .createMediaSource(getManifest(formatInfo));

        if (mTrackErrorFixer != null)
            dashSource.addEventListener(Utils.sHandler, mTrackErrorFixer);

        return dashSource;
    }

    public MediaSource fromDashManifest(InputStream dashManifest) {

        // Are you using FrameworkSampleSource or ExtractorSampleSource when you build your player?
        DashMediaSource dashSource = new DashMediaSource.Factory(getDashChunkSourceFactory(), null)
            .setLoadErrorHandlingPolicy(new DashDefaultLoadErrorHandlingPolicy())
            .createMediaSource(getManifest(DASH_MANIFEST_URI, dashManifest));

        if (mTrackErrorFixer != null)
            dashSource.addEventListener(Utils.sHandler, mTrackErrorFixer);
        
        return dashSource;
    }

    public MediaSource fromDashManifestUrl(String dashManifestUrl) {
        return buildMediaSource(Uri.parse(dashManifestUrl), DASH_MANIFEST_EXTENSION);
    }

    public MediaSource fromHlsPlaylist(String hlsPlaylist) {
        return buildMediaSource(Uri.parse(hlsPlaylist), HLS_PLAYLIST_EXTENSION);
    }

    public MediaSource fromUrlList(List<String> urlList) {

        MediaSource[] mediaSources = new MediaSource[urlList.size()];

        for (int i = 0; i < urlList.size(); i++) {
            mediaSources[i] = buildMediaSource(Uri.parse(urlList.get(i)), null);
        }

        return mediaSources[0]; // item with max resolution
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {

        int type = TextUtils.isEmpty(overrideExtension) ? 
            Utils.inferContentType(uri) : 
            Utils.inferContentType("." + overrideExtension);
        
        switch (type) {

            case C.TYPE_SS:
                SsMediaSource ssSource = new SsMediaSource.Factory(
                    new DefaultSsChunkSource.Factory(getMediaDataSourceFactory()),
                    getMediaDataSourceFactory()
                ).createMediaSource(uri);
                
                if (mTrackErrorFixer != null) 
                    ssSource.addEventListener(Utils.sHandler, mTrackErrorFixer);
                
                return ssSource;
            
            case C.TYPE_DASH:
                DashMediaSource dashSource = new DashMediaSource.Factory(
                    getDashChunkSourceFactory(),
                    getMediaDataSourceFactory()
                )
                    .setManifestParser(new LiveDashManifestParser()) // Don't make static! Need state reset for each live source.
                    .setLoadErrorHandlingPolicy(new DashDefaultLoadErrorHandlingPolicy())
                    .createMediaSource(uri);

                if (mTrackErrorFixer != null)
                    dashSource.addEventListener(Utils.sHandler, mTrackErrorFixer);
                
                return dashSource;
                
            case C.TYPE_HLS:
                
                HlsMediaSource hlsSource = new HlsMediaSource.Factory(getMediaDataSourceFactory()).createMediaSource(uri);
                
                if (mTrackErrorFixer != null)
                    hlsSource.addEventListener(Utils.sHandler, mTrackErrorFixer);
                
                return hlsSource;

            case C.TYPE_OTHER:
                ExtractorMediaSource extractorSource = new ExtractorMediaSource.Factory(getMediaDataSourceFactory())
                        .setExtractorsFactory(new DefaultExtractorsFactory())
                        .createMediaSource(uri);
                if (mTrackErrorFixer != null) {
                    extractorSource.addEventListener(Utils.sHandler, mTrackErrorFixer);
                }
                return extractorSource;

            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }

        }

    }

    private DashManifest getManifest(MediaItemFormatInfo formatInfo) {
        DashManifestParser2 parser = new DashManifestParser2();
        return parser.parse(formatInfo);
    }

    private DashManifest getManifest(Uri uri, InputStream mpdContent) {
        DashManifestParser parser = new StaticDashManifestParser();
        DashManifest result;
        try {
            result = parser.parse(uri, mpdContent);
        } catch (IOException e) {
            throw new IllegalStateException("Malformed mpd file:\n" + mpdContent, e);
        }
        return result;
    }

    private DashManifest getManifest(Uri uri, String mpdContent) {
        DashManifestParser parser = new StaticDashManifestParser();
        DashManifest result;
        try {
            result = parser.parse(uri, FileHelpers.toStream(mpdContent));
        } catch (IOException e) {
            throw new IllegalStateException("Malformed mpd file:\n" + mpdContent, e);
        }
        return result;
    }

    public void setTrackErrorFixer(TrackErrorFixer trackErrorFixer) {
        mTrackErrorFixer = trackErrorFixer;
    }

    public void release() {
        mMediaDataSourceFactory = null;
    }

    @NonNull
    private SabrChunkSource.Factory getSabrChunkSourceFactory() {
        return new DefaultSabrChunkSource.Factory(getMediaDataSourceFactory(), MAX_SEGMENTS_PER_LOAD);
    }

    @NonNull
    private DashChunkSource.Factory getDashChunkSourceFactory() {
        return new DefaultDashChunkSource.Factory(getMediaDataSourceFactory(), MAX_SEGMENTS_PER_LOAD);
    }

    private Factory getMediaDataSourceFactory() {

        if (mMediaDataSourceFactory == null) {

            PlayerTweaksData tweaksData = PlayerTweaksData.instance(mContext);
            
            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(
                USER_AGENT_TV, 
                null, 
                (int) OkHttpManager.CONNECT_TIMEOUT_MS,
                (int) OkHttpManager.READ_TIMEOUT_MS, 
                true
            );
            
            mMediaDataSourceFactory = new DefaultDataSourceFactory(
                mContext, 
                null, 
                dataSourceFactory
            );

        }

        return mMediaDataSourceFactory;
    }

    // EXO: 2.10 - 2.12
    private static class StaticDashManifestParser extends DashManifestParser {
        @Override
        protected DashManifest buildMediaPresentationDescription(
                long availabilityStartTime,
                long durationMs,
                long minBufferTimeMs,
                boolean dynamic,
                long minUpdateTimeMs,
                long timeShiftBufferDepthMs,
                long suggestedPresentationDelayMs,
                long publishTimeMs,
                ProgramInformation programInformation,
                UtcTimingElement utcTiming,
                Uri location,
                List<Period> periods) {
            return new DashManifest(
                    availabilityStartTime,
                    durationMs,
                    minBufferTimeMs,
                    false,
                    minUpdateTimeMs,
                    timeShiftBufferDepthMs,
                    suggestedPresentationDelayMs,
                    publishTimeMs,
                    programInformation,
                    utcTiming,
                    location,
                    periods);
        }
    }

}
