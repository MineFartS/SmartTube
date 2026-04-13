
package com.google.android.exoplayer2.ext.rtmp;

import androidx.annotation.Nullable;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource.Factory;
import com.google.android.exoplayer2.upstream.TransferListener;

/**
 * A {@link Factory} that produces {@link RtmpDataSource}.
 */
public final class RtmpDataSourceFactory implements DataSource.Factory {

  private final @Nullable TransferListener listener;

  public RtmpDataSourceFactory() {
    this(null);
  }

  /** @param listener An optional listener. */
  public RtmpDataSourceFactory(@Nullable TransferListener listener) {
    this.listener = listener;
  }

  @Override
  public RtmpDataSource createDataSource() {
    RtmpDataSource dataSource = new RtmpDataSource();
    if (listener != null) {
      dataSource.addTransferListener(listener);
    }
    return dataSource;
  }

}
