package com.liskovsoft.sharedutils.service.data;

import com.liskovsoft.sharedutils.data.SponsorSegment;
import com.liskovsoft.sharedutils.block.data.Segment;
import com.liskovsoft.sharedutils.block.data.SegmentList;

import java.util.ArrayList;
import java.util.List;

public class YouTubeSponsorSegment implements SponsorSegment {
    private long mStartMs;
    private long mEndMs;
    private String mCategory;
    private String mAction;

    public static List<SponsorSegment> from(SegmentList segmentList) {
        if (segmentList == null || segmentList.getSegments() == null) {
            return null;
        }

        List<SponsorSegment> result = new ArrayList<>();

        for (Segment segment : segmentList.getSegments()) {
            YouTubeSponsorSegment sponsorSegment = new YouTubeSponsorSegment();
            sponsorSegment.mStartMs = (long) (segment.getStart() * 1_000);
            sponsorSegment.mEndMs = (long) (segment.getEnd() * 1_000);
            sponsorSegment.mCategory = segment.getCategory();
            sponsorSegment.mAction = segment.getActionType();
            result.add(sponsorSegment);
        }

        return result;
    }

    @Override
    public long getStartMs() {
        return mStartMs;
    }

    @Override
    public long getEndMs() {
        return mEndMs;
    }

    @Override
    public String getCategory() {
        return mCategory;
    }

    @Override
    public String getAction() {
        return mAction;
    }
}
