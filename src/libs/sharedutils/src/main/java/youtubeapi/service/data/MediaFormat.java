package minefarts.sharedutils.service.data;

import androidx.annotation.NonNull;
import minefarts.sharedutils.helpers.Helpers;
import minefarts.sharedutils.formatbuilders.utils.ITagUtils;
import minefarts.sharedutils.videoinfo.models.formats.AdaptiveVideoFormat;
import minefarts.sharedutils.videoinfo.models.formats.RegularVideoFormat;
import minefarts.sharedutils.videoinfo.models.formats.VideoFormat;

import java.util.List;

public class MediaFormat {

    public static final int FORMAT_TYPE_DASH = 0;
    public static final int FORMAT_TYPE_REGULAR = 1;
    public static final int FORMAT_TYPE_SABR = 2;

    private String mIndex;
    private String mIndexRange;
    private String mUrl;
    private String mMimeType;
    private String mITag;
    private boolean mIsDrc;
    private String mClen;
    private String mBitrate;
    private String mProjectionType;
    private String mXtags;
    private int mWidth;
    private int mHeight;
    private String mInit;
    private String mFps;
    private String mLmt;
    private String mQualityLabel;
    private String mFormat;
    private boolean mIsOtf;
    private String mOtfInitUrl;
    private String mOtfTemplateUrl;
    private String mSourceUrl;
    private List<String> mSegmentUrlList;
    private List<String> mGlobalSegmentList;
    private String mAudioQuality;
    private int mFormatType;
    private String mLanguage;
    private int mTargetDurationSec;
    private int mMaxDvrDurationSec;
    private int mApproxDurationMs;

    public static MediaFormat from(AdaptiveVideoFormat format) {
        MediaFormat mediaFormat = createBaseFormat(format);

        mediaFormat.mFormatType = format.isBroken() ? FORMAT_TYPE_SABR : FORMAT_TYPE_DASH;

        mediaFormat.mIndex = format.getIndex();

        if (format.getIndexRange() != null) {
            mediaFormat.mIndexRange = format.getIndexRange().toString();
        }

        mediaFormat.mInit = format.getInit();

        return mediaFormat;
    }

    public static MediaFormat from(RegularVideoFormat format) {
        MediaFormat mediaFormat = createBaseFormat(format);

        mediaFormat.mFormatType = FORMAT_TYPE_REGULAR;

        mediaFormat.mAudioQuality = format.getAudioQuality();

        return mediaFormat;
    }

    private static MediaFormat createBaseFormat(VideoFormat format) {
        MediaFormat mediaFormat = new MediaFormat();

        mediaFormat.mUrl = format.getUrl();
        mediaFormat.mMimeType = format.getMimeType();
        String iTag = format.getITag() == 0 ? "" : String.valueOf(format.getITag());
        mediaFormat.mITag = iTag;
        mediaFormat.mIsDrc = format.isDrc();
        mediaFormat.mClen = format.getContentLength();
        String bitrate = format.getBitrate() == 0 ? "" : String.valueOf(format.getBitrate());
        mediaFormat.mBitrate = bitrate;
        mediaFormat.mWidth = format.getWidth();
        mediaFormat.mHeight = format.getHeight();
        String fps = format.getFps() == 0 ? "" : String.valueOf(format.getFps());
        mediaFormat.mFps = fps;
        mediaFormat.mFormat = format.getFormat();
        mediaFormat.mIsOtf = format.isOTF();
        mediaFormat.mOtfInitUrl = format.getOtfInitUrl();
        mediaFormat.mOtfTemplateUrl = format.getOtfTemplateUrl();
        mediaFormat.mSourceUrl = format.getSourceURL();
        mediaFormat.mSegmentUrlList = format.getSegmentUrlList();
        mediaFormat.mGlobalSegmentList = format.getGlobalSegmentList();
        mediaFormat.mLanguage = format.getLanguage();
        mediaFormat.mTargetDurationSec = format.getTargetDurationSec();
        mediaFormat.mLmt = format.getLastModified();
        mediaFormat.mQualityLabel = format.getQualityLabel();
        mediaFormat.mMaxDvrDurationSec = format.getMaxDvrDurationSec();
        mediaFormat.mApproxDurationMs = Helpers.parseInt(format.getApproxDurationMs());

        return mediaFormat;
    }

    public String getUrl() {
        return mUrl;
    }
    
    public void setUrl(String url) {
        mUrl = url;
    }

    public String getMimeType() {
        return mMimeType;
    }
    
    public void setMimeType(String mimeType) {
        mMimeType = mimeType;
    }

    public String getITag() {
        return mITag;
    }
    
    public void setITag(String itag) {
        mITag = itag;
    }

    public boolean isDrc() {
        return mIsDrc;
    }

    public String getClen() {
        return mClen;
    }
    
    public void setClen(String clen) {
        mClen = clen;
    }

    public String getBitrate() {
        return mBitrate;
    }
    
    public void setBitrate(String bitrate) {
        mBitrate = bitrate;
    }

    public String getProjectionType() {
        return mProjectionType;
    }

    public String getXtags() {
        return mXtags;
    }

    public int getWidth() {
        return mWidth;
    }
    
    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public String getIndex() {
        return mIndex;
    }

    public void setIndex(String index) {
        mIndex = index;
    }

    public String getInit() {
        return mInit;
    }

    public void setInit(String init) {
        mInit = init;
    }

    public String getFps() {
        return mFps;
    }

    public void setFps(String fps) {
        mFps = fps;
    }

    public String getLmt() {
        return mLmt;
    }

    public String getQualityLabel() {
        return mQualityLabel;
    }

    public String getFormat() {
        return mFormat;
    }

    public boolean isOtf() {
        return mIsOtf;
    }

    public String getOtfInitUrl() {
        return mOtfInitUrl;
    }

    public String getOtfTemplateUrl() {
        return mOtfTemplateUrl;
    }

    public String getQuality() {
        return null;
    }

    public String getSignature() {
        return null;
    }

    public void setAudioSamplingRate(String audioSamplingRate) {

    }

    public String getAudioSamplingRate() {
        return null;
    }

    public void setSourceUrl(String sourceUrl) {
        mSourceUrl = sourceUrl;
    }

    public String getSourceUrl() {
        return mSourceUrl;
    }

    public List<String> getSegmentUrlList() {
        return mSegmentUrlList;
    }

    public void setSegmentUrlList(List<String> urls) {
        mSegmentUrlList = urls;
    }

    public List<String> getGlobalSegmentList() {
        return mGlobalSegmentList;
    }

    public void setGlobalSegmentList(List<String> segments) {
        mGlobalSegmentList = segments;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public int getTargetDurationSec() {
        return mTargetDurationSec;
    }

    public int getMaxDvrDurationSec() {
        return mMaxDvrDurationSec;
    }

    public int getApproxDurationMs() {
        return mApproxDurationMs;
    }

    public String getIndexRange() {
        return mIndexRange;
    }

    public void setIndexRange(String indexRange) {
        mIndexRange = indexRange;
    }

    public int compareTo(MediaFormat format) {
        if (format == null) {
            return 1;
        }

        return ITagUtils.compare(getITag(), format.getITag());
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(
                "{Url: %s, Source url: %s, Signature: %s, Clen: %s, Width: %s, Height: %s, ITag: %s}",
                getUrl(),
                getSourceUrl(),
                getSignature(),
                getClen(),
                getWidth(),
                getHeight(),
                getITag());
    }

    public String getAudioQuality() {
        return mAudioQuality;
    }

    public int getFormatType() {
        return mFormatType;
    }
}
