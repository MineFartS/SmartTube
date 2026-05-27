package minefarts.smarttube.source;

import minefarts.smarttube.C;
import minefarts.smarttube.FormatHolder;
import minefarts.smarttube.decoder.DecoderInputBuffer;
import java.io.IOException;

/**
 * An empty {@link SampleStream}.
 */
public final class EmptySampleStream implements SampleStream {

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void maybeThrowError() throws IOException {
    // Do nothing.
  }

  @Override
  public int readData(FormatHolder formatHolder, DecoderInputBuffer buffer,
      boolean formatRequired) {
    buffer.setFlags(C.BUFFER_FLAG_END_OF_STREAM);
    return C.RESULT_BUFFER_READ;
  }

  @Override
  public int skipData(long positionUs) {
    return 0;
  }

}
