package minefarts.smarttube.utils.lounge.models.commands;

import minefarts.smarttube.google.common.converters.jsonpath.JsonPath;

public class SeekToParams {
    @JsonPath("$.newTime")
    private String mNewTimeSec;

    public String getNewTimeSec() {
        return mNewTimeSec;
    }
}
