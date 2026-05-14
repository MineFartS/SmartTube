package com.liskovsoft.sharedutils.data;

/**
 * <a href="https://wiki.sponsor.ajay.app/w/Segment_Categories">Segment specs</a><br/>
 * <a href="https://wiki.sponsor.ajay.app/w/API_Docs">Segment API</a><br/>
 */
public interface SponsorSegment {

    String CATEGORY_SPONSOR = "Sponsor";
    String CATEGORY_INTERACTION = "Interaction reminder (subscribe)";
    String CATEGORY_SELF_PROMO = "Unpaid/self promotion";

    String ACTION_SKIP = "skip";
    String ACTION_MUTE = "mute";

    long getStartMs();
    long getEndMs();
    String getCategory();
    String getAction();

}
