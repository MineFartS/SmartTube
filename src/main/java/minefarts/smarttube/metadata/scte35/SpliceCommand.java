package minefarts.smarttube.metadata.scte35;

import minefarts.smarttube.metadata.Metadata;

/**
 * Superclass for SCTE35 splice commands.
 */
public abstract class SpliceCommand implements Metadata.Entry {

  @Override
  public String toString() {
    return "SCTE-35 splice command: type=" + getClass().getSimpleName();
  }

  // Parcelable implementation.

  @Override
  public int describeContents() {
    return 0;
  }

}
