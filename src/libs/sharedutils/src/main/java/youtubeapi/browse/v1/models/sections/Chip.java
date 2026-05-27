package minefarts.sharedutils.browse.v1.models.sections;

import minefarts.googlecommon.common.converters.jsonpath.JsonPath;
import minefarts.sharedutils.common.models.items.ItemWrapper;

import java.util.List;

public class Chip {
    @JsonPath("$.text.simpleText")
    private String mTitle;

    @JsonPath("$.content.horizontalListRenderer.continuations[0].reloadContinuationData.continuation")
    private String mReloadPageKey;

    @JsonPath("$.content.horizontalListRenderer.continuations[0].nextContinuationData.continuation")
    private String mNextPageKey;

    // Next section presents only inside suggestions
    @JsonPath("$.content.horizontalListRenderer.items[*]")
    private List<ItemWrapper> mItemWrappers;

    public String getTitle() {
        return mTitle;
    }

    public String getReloadPageKey() {
        return mReloadPageKey;
    }

    public String getNextPageKey() {
        return mNextPageKey;
    }

    public List<ItemWrapper> getItemWrappers() {
        return mItemWrappers;
    }
}
