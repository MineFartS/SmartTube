package minefarts.smarttube.google.common.models.auth.info;

import minefarts.smarttube.google.common.converters.jsonpath.JsonPath;
import minefarts.smarttube.google.common.models.V2.TextItem;
import minefarts.smarttube.google.common.models.items.Thumbnail;

import java.util.List;

public class AccountInt {
    @JsonPath("$.accountName")
    private TextItem mName;

    @JsonPath("$.accountByline")
    private TextItem mEmail;

    @JsonPath("$.channelHandle")
    private TextItem mChannelName;

    @JsonPath("$.accountPhoto.thumbnails[*]")
    private List<Thumbnail> mAccountThumbnails;

    @JsonPath("$.isSelected")
    private boolean mIsSelected;

    @JsonPath("$.isDisabled")
    private boolean mIsDisabled;

    @JsonPath("$.hasChannel")
    private boolean mHasChannel;

    @JsonPath("$.serviceEndpoint.selectActiveIdentityEndpoint.supportedTokens[*].pageIdToken.pageId")
    private List<String> mPageIdTokens;

    public String getName() {
        return mName != null ? mName.toString() : null;
    }

    public String getEmail() {
        return mEmail != null ? mEmail.toString() : null;
    }

    public String getChannelName() {
        return mChannelName != null ? mChannelName.toString() : null;
    }

    public List<Thumbnail> getThumbnails() {
        return mAccountThumbnails;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public boolean isDisabled() {
        return mIsDisabled;
    }

    /**
     * Every YouTube account has a channel<br/>
     * This method could be served as a simple testing<br/>
     * whether the account is belongs to YouTube
     */
    public boolean hasChannel() {
        return mHasChannel;
    }

    /**
     * This token token used along with the access token to support restricted videos
     */
    public String getPageIdToken() {
        return mPageIdTokens != null ? mPageIdTokens.get(0) : null;
    }
}
