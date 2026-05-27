package minefarts.smarttube.utils.block.data;

import minefarts.smarttube.google.common.converters.jsonpath.JsonPath;

import java.util.List;

public class SegmentList {
    @JsonPath("$[*]")
    private List<Segment> mSegments;

    public List<Segment> getSegments() {
        return mSegments;
    }
}
