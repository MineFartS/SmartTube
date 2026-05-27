package minefarts.smarttube.metadata.id3;

import minefarts.smarttube.metadata.Metadata;

/**
 * Base class for ID3 frames.
 */
public abstract class Id3Frame implements Metadata.Entry {

  /**
   * The frame ID.
   */
  public final String id;

  public Id3Frame(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return id;
  }

  @Override
  public int describeContents() {
    return 0;
  }

}
