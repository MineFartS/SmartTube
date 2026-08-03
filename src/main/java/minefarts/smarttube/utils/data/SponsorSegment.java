package minefarts.smarttube.utils.data;

import java.util.ArrayList;
import java.util.List;

public class SponsorSegment {
    
    public static final String CATEGORY_SPONSOR = "Sponsor";
    public static final String CATEGORY_INTERACTION = "Interaction reminder (subscribe)";
    public static final String CATEGORY_SELF_PROMO = "Unpaid/self promotion";

    public static final String ACTION_SKIP = "skip";
    public static final String ACTION_MUTE = "mute";
    
    public long mStartMs;
    public long mEndMs;
    
    public String mCategory;
    public String mAction;

    public long getStartMs() {
        return mStartMs;
    }

    public long getEndMs() {
        return mEndMs;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getAction() {
        return mAction;
    }

}
