package minefarts.smarttube.utils.videoinfo.models;

public interface DashInfo {
    int getSegmentDurationUs();
    long getStartTimeMs();
    int getStartSegmentNum();
    boolean isSeekable();
}
