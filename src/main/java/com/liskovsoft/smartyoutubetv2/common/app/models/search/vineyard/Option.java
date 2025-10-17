package com.liskovsoft.smartyoutubetv2.common.app.models.search.vineyard;

/** Small POJO representing an option in vineyard search UI. */
public class Option {
    public String title;
    public String value;
    public int iconResource;

    public Option(String title, String value, int iconResource) {
        this.title = title;
        this.value = value;
        this.iconResource = iconResource;
    }
}
