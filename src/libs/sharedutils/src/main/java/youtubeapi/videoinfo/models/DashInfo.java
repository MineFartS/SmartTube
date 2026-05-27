package minefarts.sharedutils.videoinfo.models;

public interface DashInfo {
    int getSegmentDurationUs();
    long getStartTimeMs();
    int getStartSegmentNum();
    boolean isSeekable();
}
