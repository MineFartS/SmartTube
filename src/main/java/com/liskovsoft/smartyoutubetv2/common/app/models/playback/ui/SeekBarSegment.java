package com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui;

/** Represents a colored segment on the player's seek bar (start/end as progress [0..1]). */
public class SeekBarSegment {
    public float startProgress;
    public float endProgress;
    public int color = Color.GREEN;
}
