package minefarts.exoplayer2.upstream;

import minefarts.exoplayer2.C;
import minefarts.exoplayer2.util.Assertions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A {@link DataSink} for writing to a byte array.
 */
public final class ByteArrayDataSink implements DataSink {

  private ByteArrayOutputStream stream;

  @Override
  public void open(DataSpec dataSpec) throws IOException {
    if (dataSpec.length == C.LENGTH_UNSET) {
      stream = new ByteArrayOutputStream();
    } else {
      Assertions.checkArgument(dataSpec.length <= Integer.MAX_VALUE);
      stream = new ByteArrayOutputStream((int) dataSpec.length);
    }
  }

  @Override
  public void close() throws IOException {
    stream.close();
  }

  @Override
  public void write(byte[] buffer, int offset, int length) throws IOException {
    stream.write(buffer, offset, length);
  }

  /**
   * Returns the data written to the sink since the last call to {@link #open(DataSpec)}, or null if
   * {@link #open(DataSpec)} has never been called.
   */
  public byte[] getData() {
    return stream == null ? null : stream.toByteArray();
  }

}
