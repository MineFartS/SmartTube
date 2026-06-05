package minefarts.smarttube.source;

import java.io.IOException;

/**
 * Thrown when a live playback falls behind the available media window.
 */
public final class BehindLiveWindowException extends IOException {

  public BehindLiveWindowException() {
    super();
  }

}
