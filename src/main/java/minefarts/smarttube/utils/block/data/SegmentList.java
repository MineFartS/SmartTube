package minefarts.smarttube.utils.block.data;

import com.liskovsoft.googlecommon.common.converters.jsonpath.JsonPath;

import java.util.List;

public class SegmentList {

    @JsonPath("$[*]")
    public List<Segment> mSegments;
    
}
