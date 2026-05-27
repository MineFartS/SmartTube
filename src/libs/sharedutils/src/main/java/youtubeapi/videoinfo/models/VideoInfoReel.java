package minefarts.sharedutils.videoinfo.models;

import minefarts.googlecommon.common.converters.jsonpath.JsonPath;

public class VideoInfoReel {
    @JsonPath("$.playerResponse")
    private VideoInfo mVideoInfo;

    public VideoInfo getVideoInfo() {
        return mVideoInfo;
    }
}
