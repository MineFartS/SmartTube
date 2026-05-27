package com.google.android.exoplayer2.upstream.cache;

import com.google.android.exoplayer2.upstream.DataSpec;

/** Factory for cache keys. */
public interface CacheKeyFactory {

  /**
   * Returns a cache key for the given {@link DataSpec}.
   *
   * @param dataSpec The data being cached.
   */
  String buildCacheKey(DataSpec dataSpec);

  /**
   * Returns the number of parallel tasks used to download segments.
   *
   * @return must be at least 1.
   */
  int maxDownloadParallelSegments();
}
