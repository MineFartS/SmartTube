package com.liskovsoft.mediaserviceinterfaces.data;

public class SponsorSegmentImpl implements SponsorSegment {
    public long mStartMs;
    public long mEndMs;
    public String mCategory;
    public String mAction;

    public SponsorSegmentImpl() {}

    public SponsorSegmentImpl(long startMs, long endMs, String category, String action) {
        this.mStartMs = startMs;
        this.mEndMs = endMs;
        this.mCategory = category;
        this.mAction = action;
    }

    @Override
    public long getStartMs() { return mStartMs; }

    @Override
    public long getEndMs() { return mEndMs; }

    @Override
    public String getCategory() { return mCategory; }

    @Override
    public String getAction() { return mAction; }
}