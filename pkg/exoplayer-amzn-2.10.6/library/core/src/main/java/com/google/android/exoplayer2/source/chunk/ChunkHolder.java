
package com.google.android.exoplayer2.source.chunk;

/**
 * Holds a chunk or an indication that the end of the stream has been reached.
 */
public final class ChunkHolder {

  /**
   * The chunk.
   */
  public Chunk chunk;

  /**
   * Indicates that the end of the stream has been reached.
   */
  public boolean endOfStream;

  /**
   * Clears the holder.
   */
  public void clear() {
    chunk = null;
    endOfStream = false;
  }

}
