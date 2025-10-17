package com.liskovsoft.smartyoutubetv2.common.app.models.data;

import androidx.annotation.Nullable;
import com.liskovsoft.sharedutils.helpers.Helpers;

/**
 * Model representing a section in the Browse UI.
 *
 * Contains id, title, type and optional icon/resource/data payload.
 * Titles are abbreviated to a maximum length to avoid layout issues.
 */
public class BrowseSection {
    // Section view types
    public static final int TYPE_GRID = 0;
    public static final int TYPE_ROW = 1;
    public static final int TYPE_SETTINGS_GRID = 2;
    public static final int TYPE_MULTI_GRID = 3;
    public static final int TYPE_ERROR = 4;
    public static final int TYPE_SHORTS_GRID = 5;

    // Max visible title length (characters). Titles longer than this are abbreviated.
    private static final int MAX_TITLE_LENGTH_CHARS = 30;

    // Unique identifier for the section
    private final int mId;
    // Display title (may be abbreviated)
    private String mTitle;
    // Optional Android resource id for an icon (if available)
    private final int mResId;
    // Optional icon URL (if available)
    private final String mIconUrl;
    // Indicates sections that require authentication to be visible
    private final boolean mIsAuthOnly;
    // Optional arbitrary data attached to the section (e.g., metadata or payload)
    private final Object mData;
    // Whether the section is enabled and should be shown
    private boolean mEnabled = true;
    // Visual type of the section (one of TYPE_*)
    private int mType;

    // Convenience constructors delegating to the full constructor

    public BrowseSection(int id, String title, int type, int resId) {
        this(id, title, type, resId, false);
    }

    public BrowseSection(int id, String title, int type, String iconUrl) {
        this(id, title, type, iconUrl, false);
    }

    public BrowseSection(int id, String title, int type, String iconUrl, boolean isAuthOnly) {
        this(id, title, type, -1, iconUrl, isAuthOnly, null);
    }

    public BrowseSection(int id, String title, int type, String iconUrl, boolean isAuthOnly, Object data) {
        this(id, title, type, -1, iconUrl, isAuthOnly, data);
    }

    public BrowseSection(int id, String title, int type, int resId, boolean isAuthOnly) {
        this(id, title, type, resId, null, isAuthOnly, null);
    }

    public BrowseSection(int id, String title, int type, int resId, boolean isAuthOnly, Object data) {
        this(id, title, type, resId, null, isAuthOnly, data);
    }

    /**
     * Full constructor.
     *
     * @param id section id
     * @param title display title (will be abbreviated if too long)
     * @param type visual type (one of TYPE_*)
     * @param resId local drawable resource id for icon, or -1 if none
     * @param iconUrl remote icon URL, or null
     * @param isAuthOnly true if the section should be shown only for authenticated users
     * @param data optional payload associated with the section
     */
    public BrowseSection(int id, String title, int type, int resId, String iconUrl, boolean isAuthOnly, Object data) {
        mId = id;
        // Abbreviate title to avoid extremely long labels in the UI
        mTitle = Helpers.abbreviate(title, MAX_TITLE_LENGTH_CHARS);
        mType = type;
        mResId = resId;
        mIconUrl = iconUrl;
        mIsAuthOnly = isAuthOnly;
        mData = data;
    }

    /** @return display title (possibly abbreviated) */
    public String getTitle() {
        return mTitle;
    }

    /** Set a new title (note: no automatic re-abbreviation is applied here) */
    public void setTitle(String title) {
        mTitle = title;
    }

    /** @return unique section id */
    public int getId() {
        return mId;
    }

    /** @return visual type constant */
    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    /** @return local resource id for the icon, or -1 if not set */
    public int getResId() {
        return mResId;
    }

    /** @return remote icon URL, or null if not set */
    public String getIconUrl() {
        return mIconUrl;
    }

    /** @return true if this section is visible only for authenticated users */
    public boolean isAuthOnly() {
        return mIsAuthOnly;
    }

    /** Enable or disable this section (disabled sections should be hidden) */
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    /** @return true if the section is enabled */
    public boolean isEnabled() {
        return mEnabled;
    }

    /** @return attached arbitrary data payload, may be null */
    public Object getData() {
        return mData;
    }

    /**
     * Check reserved ids range for default (built-in) sections.
     *
     * Historically, built-in/default sections use ids below 30.
     *
     * @return true if this section id is considered default/built-in
     */
    public boolean isDefault() {
        return mId < 30;
    }

    /**
     * Equality is based on section id only.
     *
     * @param obj other object
     * @return true if other is a BrowseSection with the same id
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof BrowseSection && ((BrowseSection) obj).getId() == getId();
    }
}
