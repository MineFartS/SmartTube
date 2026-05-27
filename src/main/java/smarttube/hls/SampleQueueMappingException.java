package minefarts.smarttube.hls;

import androidx.annotation.Nullable;
import minefarts.smarttube.source.SampleQueue;
import minefarts.smarttube.source.TrackGroup;
import java.io.IOException;

/** Thrown when it is not possible to map a {@link TrackGroup} to a {@link SampleQueue}. */
public final class SampleQueueMappingException extends IOException {

  /** @param mimeType The mime type of the track group whose mapping failed. */
  public SampleQueueMappingException(@Nullable String mimeType) {
    super("Unable to bind a sample queue to TrackGroup with mime type " + mimeType + ".");
  }
}
