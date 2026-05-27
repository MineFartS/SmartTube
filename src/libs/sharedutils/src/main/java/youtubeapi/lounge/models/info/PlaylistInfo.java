package minefarts.sharedutils.lounge.models.info;

import minefarts.googlecommon.common.converters.jsonpath.JsonPath;

import java.util.List;

public class PlaylistInfo {
    @JsonPath("$.title")
    private String mTitle;

    @JsonPath("$.author")
    private String mAuthor;

    @JsonPath("$.video[*]")
    private List<PlaylistItem> mItems;

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public List<PlaylistItem> getItems() {
        return mItems;
    }
}
