package minefarts.smarttube.offline;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A key for a subset of media which can be separately loaded (a "stream").
 *
 * <p>The stream key consists of a period index, a group index within the period and a track index
 * within the group. The interpretation of these indices depends on the type of media for which the
 * stream key is used.
 */
public final class StreamKey implements Comparable<StreamKey>, Parcelable {

  /** The period index. */
  public final int periodIndex;
  /** The group index. */
  public final int groupIndex;
  /** The track index. */
  public final int trackIndex;

  /**
   * @param groupIndex The group index.
   * @param trackIndex The track index.
   */
  public StreamKey(int groupIndex, int trackIndex) {
    this(0, groupIndex, trackIndex);
  }

  /**
   * @param periodIndex The period index.
   * @param groupIndex The group index.
   * @param trackIndex The track index.
   */
  public StreamKey(int periodIndex, int groupIndex, int trackIndex) {
    this.periodIndex = periodIndex;
    this.groupIndex = groupIndex;
    this.trackIndex = trackIndex;
  }

  /* package */ StreamKey(Parcel in) {
    periodIndex = in.readInt();
    groupIndex = in.readInt();
    trackIndex = in.readInt();
  }

  @Override
  public String toString() {
    return periodIndex + "." + groupIndex + "." + trackIndex;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StreamKey that = (StreamKey) o;
    return periodIndex == that.periodIndex
        && groupIndex == that.groupIndex
        && trackIndex == that.trackIndex;
  }

  @Override
  public int hashCode() {
    int result = periodIndex;
    result = 31 * result + groupIndex;
    result = 31 * result + trackIndex;
    return result;
  }

  // Comparable implementation.

  @Override
  public int compareTo(@NonNull StreamKey o) {
    int result = periodIndex - o.periodIndex;
    if (result == 0) {
      result = groupIndex - o.groupIndex;
      if (result == 0) {
        result = trackIndex - o.trackIndex;
      }
    }
    return result;
  }

  // Parcelable implementation.

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(periodIndex);
    dest.writeInt(groupIndex);
    dest.writeInt(trackIndex);
  }

  public static final Parcelable.Creator<StreamKey> CREATOR =
      new Parcelable.Creator<StreamKey>() {

        @Override
        public StreamKey createFromParcel(Parcel in) {
          return new StreamKey(in);
        }

        @Override
        public StreamKey[] newArray(int size) {
          return new StreamKey[size];
        }
      };
}
