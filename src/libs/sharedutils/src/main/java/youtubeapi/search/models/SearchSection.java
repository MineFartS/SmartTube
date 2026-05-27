package minefarts.sharedutils.search.models;

import minefarts.sharedutils.helpers.Helpers;
import minefarts.googlecommon.common.converters.jsonpath.JsonPath;
import minefarts.googlecommon.common.models.V2.TextItem;
import minefarts.sharedutils.common.models.items.ItemWrapper;

import java.util.List;

public class SearchSection {
    @JsonPath({
            "$.shelfRenderer.headerRenderer.shelfHeaderRenderer.avatarLockup.avatarLockupRenderer.title", // V4
            "$.shelfRenderer.headerRenderer.shelfHeaderRenderer.title", // V3
            "$.itemSectionRenderer.header.itemSectionHeaderRenderer.title",
            "$.itemSectionRenderer.contents[0].shelfRenderer.headerRenderer.shelfHeaderRenderer.title"
    })
    private TextItem mTitle;

    @JsonPath({
            "$.shelfRenderer.content.horizontalListRenderer.items[*]", // V3
            "$.itemSectionRenderer.contents[0].shelfRenderer.content.horizontalListRenderer.items[*]",
            "$.itemSectionRenderer.contents[*]"
    })
    private List<ItemWrapper> mItemWrappers;

    @JsonPath({
            "$.shelfRenderer.content.horizontalListRenderer.continuations[0].nextContinuationData.continuation", // V3
            "$.itemSectionRenderer.continuations[0].nextContinuationData.continuation"
    })
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
}
