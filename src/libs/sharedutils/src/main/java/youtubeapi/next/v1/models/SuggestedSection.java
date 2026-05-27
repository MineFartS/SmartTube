package minefarts.sharedutils.next.v1.models;

import minefarts.sharedutils.browse.v1.models.sections.Chip;
import minefarts.googlecommon.common.converters.jsonpath.JsonPath;
import minefarts.sharedutils.common.models.items.ItemWrapper;

import java.util.List;

public class SuggestedSection {
    @JsonPath({"$.title.runs[0].text", "$.title.simpleText"})
    private String mTitle;
    @JsonPath("$.content.horizontalListRenderer.items[*]")
    private List<ItemWrapper> mItemWrappers;
    @JsonPath("$.headerRenderer.chipCloudRenderer.chips[*].chipCloudChipRenderer")
    private List<Chip> mChips;
    @JsonPath("$.content.horizontalListRenderer.continuations[*].nextContinuationData.continuation")
    private List<String> mNextPageKey;

    public String getTitle() {
        return mTitle;
    }

    public List<ItemWrapper> getItemWrappers() {
        return mItemWrappers;
    }

    public List<Chip> getChips() {
        return mChips;
    }

    public String getNextPageKey() {
        if (mNextPageKey == null || mNextPageKey.isEmpty()) {
            return null;
        }

        return mNextPageKey.get(0);
    }
}
