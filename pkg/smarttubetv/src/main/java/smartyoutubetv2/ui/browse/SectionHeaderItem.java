package smartyoutubetv2.ui.browse;

import androidx.leanback.widget.HeaderItem;
import smartyoutubetv1.app.models.data.BrowseSection;

public class SectionHeaderItem extends HeaderItem {
    private final BrowseSection mSection;

    public SectionHeaderItem(BrowseSection section) {
        super(section.getId(), section.getTitle());
        mSection = section;
    }

    public int getType() {
        return mSection.getType();
    }

    public int getResId() {
        return mSection.getResId();
    }

    public String getIconUrl() {
        return mSection.getIconUrl();
    }

    public BrowseSection getSection() {
        return mSection;
    }
}
