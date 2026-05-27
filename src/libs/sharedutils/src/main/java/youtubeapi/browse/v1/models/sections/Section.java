package minefarts.sharedutils.browse.v1.models.sections;

import minefarts.sharedutils.helpers.Helpers;
import minefarts.googlecommon.common.converters.jsonpath.JsonPath;
import minefarts.googlecommon.common.models.V2.TextItem;
import minefarts.sharedutils.common.models.items.ItemWrapper;

import java.util.List;

public class Section {
    @JsonPath({"$.title", "$.headerRenderer.shelfHeaderRenderer.title", "$.headerRenderer.shelfHeaderRenderer.avatarLockup.avatarLockupRenderer.title"})
    private TextItem mTitle;
    @JsonPath("$.headerRenderer.chipCloudRenderer.chips[*].chipCloudChipRenderer")
    private List<Chip> mChips;
    @JsonPath("$.content.horizontalListRenderer.items[*]")
    private List<ItemWrapper> mItemWrappers;
    @JsonPath("$.content.horizontalListRenderer.continuations[0].nextContinuationData.continuation")
    private String mNextPageKey;

    public String getTitle() {
        return mTitle != null ? Helpers.toString(mTitle.getText()) : null;
    }

    public String getNextPageKey() {
        return mNextPageKey;
    }

    public List<ItemWrapper> getItemWrappers() {
        return mItemWrappers;
    }

    public List<Chip> getChips() {
        return mChips;
    }
}
