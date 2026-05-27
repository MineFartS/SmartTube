package minefarts.exoplayer2.decoder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Buffer for {@link SimpleDecoder} output.
 */
public class SimpleOutputBuffer extends OutputBuffer {

  private final SimpleDecoder<?, SimpleOutputBuffer, ?> owner;

  public ByteBuffer data;

  public SimpleOutputBuffer(SimpleDecoder<?, SimpleOutputBuffer, ?> owner) {
    this.owner = owner;
  }

  /**
   * Initializes the buffer.
   *
   * @param timeUs The presentation timestamp for the buffer, in microseconds.
   * @param size An upper bound on the size of the data that will be written to the buffer.
   * @return The {@link #data} buffer, for convenience.
   */
  public ByteBuffer init(long timeUs, int size) {
    this.timeUs = timeUs;
    if (data == null || data.capacity() < size) {
      data = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }
    data.position(0);
    data.limit(size);
    return data;
  }

  @Override
  public void clear() {
    super.clear();
    if (data != null) {
      data.clear();
    }
  }

  @Override
  public void release() {
    owner.releaseOutputBuffer(this);
  }

}
