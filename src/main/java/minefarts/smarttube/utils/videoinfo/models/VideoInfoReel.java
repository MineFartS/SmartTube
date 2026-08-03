package minefarts.smarttube.utils.videoinfo.models;

import minefarts.smarttube.google.common.converters.jsonpath.JsonPath;

public class VideoInfoReel {
    @JsonPath("$.playerResponse")
    private VideoInfo mVideoInfo;

    public VideoInfo getVideoInfo() {
        return mVideoInfo;
    }
}
