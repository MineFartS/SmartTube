package minefarts.sharedutils.videoinfo;

import minefarts.sharedutils.helpers.Helpers;
import minefarts.sharedutils.mylogger.Log;
import minefarts.sharedutils.app.AppService;
import minefarts.googlecommon.common.api.FileApi;
import minefarts.sharedutils.app.PoTokenGate;
import minefarts.sharedutils.common.helpers.AppClient;
import minefarts.googlecommon.common.helpers.RetrofitHelper;
import minefarts.sharedutils.formatbuilders.utils.MediaFormatUtils;
import minefarts.sharedutils.videoinfo.V2.DashInfoApi;
import minefarts.sharedutils.videoinfo.models.VideoUrlHolder;
import minefarts.sharedutils.videoinfo.models.DashInfo;
import minefarts.sharedutils.videoinfo.models.DashInfoContent;
import minefarts.sharedutils.videoinfo.models.DashInfoHeaders;
import minefarts.sharedutils.videoinfo.models.DashInfoUrl;
import minefarts.sharedutils.videoinfo.models.VideoInfo;
import minefarts.sharedutils.videoinfo.models.formats.AdaptiveVideoFormat;
import minefarts.sharedutils.videoinfo.models.formats.VideoFormat;
import minefarts.sharedutils.app.playerdata.PlayerDataExtractor;

import java.util.ArrayList;
import java.util.List;

import kotlin.Pair;

public abstract class VideoInfoServiceBase {

    private static final String TAG = VideoInfoServiceBase.class.getSimpleName();
    
    protected final AppService mAppService;
    private final DashInfoApi mDashInfoApi;
    private final FileApi mFileApi;

    private PlayerDataExtractor mPlayerDataExtractor = null;

    protected VideoInfoServiceBase() {
        mAppService = AppService.instance();
        mDashInfoApi = RetrofitHelper.create(DashInfoApi.class);
        mFileApi = RetrofitHelper.create(FileApi.class);
    }

    // Will be overridden in descendants
    protected AppClient getClient() {
        return null;
    }

    protected void transformFormats(VideoInfo videoInfo) {
        if (videoInfo == null || videoInfo.isUnplayable()) {
            return;
        }

        decipherFormats(videoInfo);

        if (videoInfo.isLive()) {
            Log.d(TAG, "Enable seeking support on live streams...");
            videoInfo.sync(getDashInfo(videoInfo));
        }

        videoInfo.setClient(getClient());
    }

    private void decipherFormats(VideoInfo videoInfo) {
        
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

        if (poToken == null) return;

        for (int i = 0; i < urlHolders.size(); i++) {
            urlHolders.get(i).setPoToken(poToken);
        }

    }

    private static void applySignatures(List<VideoUrlHolder> urlHolders, List<String> signatures) {
        if (signatures == null) {
            return;
        }

        if (signatures.size() != urlHolders.size()) {
            throw new IllegalStateException("Sizes of urlHolders and signatures should match!");
        }

        for (int i = 0; i < urlHolders.size(); i++) {
            urlHolders.get(i).setSignature(signatures.get(i));
        }
    }

    private static void applyNParams(List<VideoUrlHolder> urlHolders, List<String> nParams) {
        if (nParams == null || nParams.isEmpty()) {
            return;
        }

        // All throttled strings has same values
        boolean sameSize = nParams.size() == urlHolders.size();

        for (int i = 0; i < urlHolders.size(); i++) {
            urlHolders.get(i).setNParam(nParams.get(sameSize ? i : 0));
        }
    }

    private DashInfoUrl getDashInfoUrl(String url) {
        if (url == null) {
            return null;
        }

        return RetrofitHelper.get(mDashInfoApi.getDashInfoUrl(url));
    }

    private DashInfoContent getDashInfoContent(String url) {
        if (url == null) {
            return null;
        }

        return RetrofitHelper.get(mDashInfoApi.getDashInfoContent(url));
    }

    private DashInfoHeaders getDashInfoHeaders(String url) {
        if (url == null) {
            return null;
        }

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
        AdaptiveVideoFormat format = getSmallestAudio(videoInfo);

        if (format == null) {
            return null;
        }

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

    private AdaptiveVideoFormat getSmallestAudio(VideoInfo videoInfo) {
        AdaptiveVideoFormat format = Helpers.findFirst(videoInfo.getAdaptiveFormats(),
                item -> MediaFormatUtils.isAudio(item.getMimeType())); // smallest format
        return format;
    }

}
