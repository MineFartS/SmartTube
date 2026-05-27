package minefarts.exoplayer2.upstream;

import androidx.annotation.Nullable;

/**
 * A {@link DataSource.Factory} that produces {@link FileDataSource}.
 */
public final class FileDataSourceFactory implements DataSource.Factory {

  private final @Nullable TransferListener listener;

  public FileDataSourceFactory() {
    this(null);
  }

  public FileDataSourceFactory(@Nullable TransferListener listener) {
    this.listener = listener;
  }

  @Override
  public FileDataSource createDataSource() {
    FileDataSource dataSource = new FileDataSource();
    if (listener != null) {
      dataSource.addTransferListener(listener);
    }
    return dataSource;
  }

}
