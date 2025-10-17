package com.liskovsoft.smartyoutubetv2.common.exoplayer.selector;

/**
 * Builds concise human-friendly track labels used by UI dialogs and overlays.
 *
 * Examples of output:
 * - "1080p/60fps/VP9/4.50Mb/HDR"
 * - "en/5.1/DRC"
 *
 * Notes:
 * - Keep the output short and consistent across places (format selection dialog, debug view).
 * - Avoid locale-specific formatting for technical labels; use resources for UI strings when needed.
 */
public class TrackInfoFormatter2 {
    private String mResolutionStr;
    private String mFpsStr;
    private String mCodecStr;
    private String mBitrateStr;
    private String mHdrStr;
    private String mSpeedStr;
    private String mChannelsStr;
    private String mHighBitrateStr;
    private boolean mEnableBitrate;
    private String mDrcStr;

    public void setFormat(Format format) {
        if (TrackSelectorUtil.isVideo(format)) {
            setVideoFormat(format);
        } else if (TrackSelectorUtil.isAudio(format)) {
            setAudioFormat(format);
        }
    }

    public void setVideoFormat(Format format) {
        if (format == null) {
            return;
        }

        mResolutionStr = TrackSelectorUtil.getShortResolutionLabel(format);

        int fpsNum = extractFps(format);
        mFpsStr = fpsNum == 0 ? "" : String.valueOf(fpsNum);

        String codec = TrackSelectorUtil.extractCodec(format);
        mCodecStr = codec != null ? codec.toUpperCase() : "";

        if (mEnableBitrate) {
            String bitrate = TrackSelectorUtil.extractBitrate(format, 0);
            mBitrateStr = bitrate.isEmpty() ? "" : bitrate.toUpperCase() + "Mb";
        }

        mHdrStr = TrackSelectorUtil.buildHDRString(format);

        mHighBitrateStr = TrackSelectorUtil.buildHighBitrateMark(format);
    }

    public void setAudioFormat(Format format) {
        if (format == null) {
            return;
        }

        mChannelsStr = TrackSelectorUtil.buildChannels(format);

        mDrcStr = TrackSelectorUtil.buildDrcMark(format);
    }

    public void setSpeed(float speed) {
        mSpeedStr = speed != 1.0f ? speed + "x" : "";
    }

    public String getQualityLabel() {
        return combine(mResolutionStr, mFpsStr, mCodecStr, mBitrateStr, mHdrStr, mChannelsStr, mSpeedStr, mHighBitrateStr, mDrcStr);
    }

    private static String combine(String... items) {
        String separator = "/";
        StringBuilder result = new StringBuilder();

        if (items != null && items.length != 0) {
            int index = 0;

            for (String item : items) {
                if (item == null || item.isEmpty()) {
                    continue;
                }

                if (index != 0) {
                    result.append(separator);
                }

                result.append(item);
                index++;
            }
        }

        return result.toString();
    }

    private static int extractFps(Format format) {
        return format.frameRate == Format.NO_VALUE ? 0 : Math.round(format.frameRate);
    }

    public void enableBitrate(boolean enable) {
        mEnableBitrate = enable;
    }
}
