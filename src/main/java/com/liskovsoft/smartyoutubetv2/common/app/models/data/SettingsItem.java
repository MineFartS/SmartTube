package com.liskovsoft.smartyoutubetv2.common.app.models.data;

/**
 * Lightweight model representing a single settings entry.
 * Contains a display title, an action to run when activated, and an optional image resource id.
 */
public class SettingsItem {
    // Display title for this settings item
    public final String title;
    // Action to execute when the item is selected (may be null)
    public final Runnable onClick;
    // Optional drawable resource id for an icon; -1 means no icon
    public int imageResId;

    /**
     * Construct a SettingsItem without an icon.
     *
     * @param title   visible title for the item
     * @param onClick action to execute when the item is clicked; may be null
     */
    public SettingsItem(String title, Runnable onClick) {
        this(title, onClick, -1);
    }

    /**
     * Construct a SettingsItem with an optional icon.
     *
     * @param title      visible title for the item
     * @param onClick    action to execute when the item is clicked; may be null
     * @param imageResId drawable resource id for the icon, or -1 if none
     */
    public SettingsItem(String title, Runnable onClick, int imageResId) {
        this.title = title;
        this.onClick = onClick;
        this.imageResId = imageResId;
    }
}
