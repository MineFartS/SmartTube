package minefarts.sharedutils.videoinfo.models.formats;

import minefarts.googlecommon.common.converters.jsonpath.JsonPath;

public class RegularVideoFormat extends VideoFormat {
    @JsonPath("$.audioQuality")
    private String mAudioQuality;

    public String getAudioQuality() {
        return mAudioQuality;
    }
}
