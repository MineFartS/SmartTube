package minefarts.sharedutils.browse.v1.models.sections;

import minefarts.googlecommon.common.converters.jsonpath.JsonPath;
import minefarts.sharedutils.common.models.items.ItemWrapper;

import java.util.List;

public class SectionContinuation {
    @JsonPath({
            "$.continuationContents.horizontalListContinuation.items[*]",
            "$.continuationContents.playlistVideoListContinuation.contents[*]" // Two columns playlist (v2)
    })
    private List<ItemWrapper> mItemWrappers;

    @JsonPath({
            "$.continuationContents.horizontalListContinuation.continuations[0].nextContinuationData.continuation",
            "$.continuationContents.playlistVideoListContinuation.continuations[0].nextContinuationData.continuation" // Two columns playlist (v2)
    })
    private String mNextPageKey;

    /**
     * Generic wrapper if there's no continuation content
     */
    @JsonPath("$.responseContext.visitorData")
    private String mVisitorData;

    public String getNextPageKey() {
        return mNextPageKey;
    }

    public List<ItemWrapper> getItemWrappers() {
        return mItemWrappers;
    }

    public String getVisitorData() {
        return mVisitorData;
    }
}
