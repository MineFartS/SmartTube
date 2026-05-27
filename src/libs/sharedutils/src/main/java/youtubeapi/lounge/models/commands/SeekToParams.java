package minefarts.sharedutils.lounge.models.commands;

import minefarts.googlecommon.common.converters.jsonpath.JsonPath;

public class SeekToParams {
    @JsonPath("$.newTime")
    private String mNewTimeSec;

    public String getNewTimeSec() {
        return mNewTimeSec;
    }
}
