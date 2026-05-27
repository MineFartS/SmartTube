package minefarts.exoplayer2.upstream.cache;

import minefarts.exoplayer2.upstream.DataSink;

/**
 * A {@link DataSink.Factory} that produces {@link CacheDataSink}.
 */
public final class CacheDataSinkFactory implements DataSink.Factory {

  private final Cache cache;
  private final long fragmentSize;
  private final int bufferSize;

  /** @see CacheDataSink#CacheDataSink(Cache, long) */
  public CacheDataSinkFactory(Cache cache, long fragmentSize) {
    this(cache, fragmentSize, CacheDataSink.DEFAULT_BUFFER_SIZE);
  }

  /** @see CacheDataSink#CacheDataSink(Cache, long, int) */
  public CacheDataSinkFactory(Cache cache, long fragmentSize, int bufferSize) {
    this.cache = cache;
    this.fragmentSize = fragmentSize;
    this.bufferSize = bufferSize;
  }

  @Override
  public DataSink createDataSink() {
    return new CacheDataSink(cache, fragmentSize, bufferSize);
  }
}
