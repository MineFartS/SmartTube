package minefarts.sharedutils.block.data;

import minefarts.googlecommon.common.converters.jsonpath.JsonPath;

import java.util.List;

public class SegmentList {
    @JsonPath("$[*]")
    private List<Segment> mSegments;

    public List<Segment> getSegments() {
        return mSegments;
    }
}
