package minefarts.smarttube.utils.block.data;

import minefarts.smarttube.google.common.converters.jsonpath.JsonPath;

public class Segment {
    
    @JsonPath("$.category")
    public String mCategory;

    @JsonPath("$.actionType")
    public String mActionType;

    @JsonPath("$.segment[0]")
    public float mStart;

    @JsonPath("$.segment[1]")
    public float mEnd;

    @JsonPath("$.UUID")
    public String mUuid;

    public String getCategory() {
        return mCategory;
    }

    public float getStart() {
        return mStart;
    }

    public float getEnd() {
        return mEnd;
    }

    public String getUuid() {
        return mUuid;
    }

    public String getActionType() {
        return mActionType;
    }
    
}
