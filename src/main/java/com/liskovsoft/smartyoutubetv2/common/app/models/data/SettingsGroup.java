package com.liskovsoft.smartyoutubetv2.common.app.models.data;

import java.util.List;

/**
 * Represents a group of settings items that belong to the same browse section/category.
 * This wrapper keeps the list of SettingsItem entries together with their BrowseSection metadata.
 */
public class SettingsGroup {
    // List of settings items belonging to this group. May be null or empty.
    private List<SettingsItem> mItems;
    // Category/browse section associated with this group. Expected to be non-null when used.
    private BrowseSection mCategory;

    /**
     * Factory method to create a SettingsGroup from items and a category.
     *
     * @param items    list of SettingsItem; may be null or empty
     * @param category BrowseSection describing the group
     * @return constructed SettingsGroup
     */
    public static SettingsGroup from(List<SettingsItem> items, BrowseSection category) {
        SettingsGroup settingsGroup = new SettingsGroup();
        settingsGroup.mItems = items;
        settingsGroup.mCategory = category;

        return settingsGroup;
    }

    /**
     * Returns the list of settings items for this group.
     *
     * @return list of SettingsItem, possibly null
     */
    public List<SettingsItem> getItems() {
        return mItems;
    }

    /**
     * Returns the browse section (category) associated with this group.
     *
     * @return BrowseSection, may be null if not set
     */
    public BrowseSection getCategory() {
        return mCategory;
    }

    /**
     * Checks whether this group contains any items.
     *
     * @return true if items list is null or empty
     */
    public boolean isEmpty() {
        return mItems == null || mItems.size() == 0;
    }

    /**
     * Convenience method to get the title of the category.
     * Caller should ensure getCategory() is not null to avoid NullPointerException.
     *
     * @return title of the category
     */
    public String getTitle() {
        return mCategory.getTitle();
    }
}
