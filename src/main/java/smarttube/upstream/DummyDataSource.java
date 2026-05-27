package minefarts.smarttube.upstream;

import android.net.Uri;
import androidx.annotation.Nullable;
import java.io.IOException;

/**
 * A dummy DataSource which provides no data. {@link #open(DataSpec)} throws {@link IOException}.
 */
public final class DummyDataSource implements DataSource {

  public static final DummyDataSource INSTANCE = new DummyDataSource();

  /** A factory that produces {@link DummyDataSource}. */
  public static final Factory FACTORY = DummyDataSource::new;

  private DummyDataSource() {}

  @Override
  public void addTransferListener(TransferListener transferListener) {
    // Do nothing.
  }

  @Override
  public long open(DataSpec dataSpec) throws IOException {
    throw new IOException("Dummy source");
  }

  @Override
  public int read(byte[] buffer, int offset, int readLength) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nullable Uri getUri() {
    return null;
  }

  @Override
  public void close() throws IOException {
    // do nothing.
  }
}
