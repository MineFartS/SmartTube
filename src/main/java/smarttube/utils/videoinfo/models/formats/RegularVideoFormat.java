package minefarts.smarttube.utils.videoinfo.models.formats;

import minefarts.smarttube.google.common.converters.jsonpath.JsonPath;

public class RegularVideoFormat extends VideoFormat {
    @JsonPath("$.audioQuality")
    private String mAudioQuality;

    public String getAudioQuality() {
        return mAudioQuality;
    }
}
