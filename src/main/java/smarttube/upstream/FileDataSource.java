package minefarts.smarttube.upstream;

import android.net.Uri;
import androidx.annotation.Nullable;
import minefarts.smarttube.C;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

/** A {@link DataSource} for reading local files. */
public final class FileDataSource extends BaseDataSource {

  /**
   * Thrown when IOException is encountered during local file read operation.
   */
  public static class FileDataSourceException extends IOException {

    public FileDataSourceException(IOException cause) {
      super(cause);
    }

  }

  private @Nullable RandomAccessFile file;
  private @Nullable Uri uri;
  private long bytesRemaining;
  private boolean opened;

  public FileDataSource() {
    super(/* isNetwork= */ false);
  }

  /**
   * @param listener An optional listener.
   * @deprecated Use {@link #FileDataSource()} and {@link #addTransferListener(TransferListener)}
   */
  @Deprecated
  public FileDataSource(@Nullable TransferListener listener) {
    this();
    if (listener != null) {
      addTransferListener(listener);
    }
  }

  @Override
  public long open(DataSpec dataSpec) throws FileDataSourceException {
    try {
      uri = dataSpec.uri;
      transferInitializing(dataSpec);
      file = new RandomAccessFile(dataSpec.uri.getPath(), "r");
      file.seek(dataSpec.position);
      bytesRemaining = dataSpec.length == C.LENGTH_UNSET ? file.length() - dataSpec.position
          : dataSpec.length;
      if (bytesRemaining < 0) {
        throw new EOFException();
      }
    } catch (IOException e) {
      throw new FileDataSourceException(e);
    }

    opened = true;
    transferStarted(dataSpec);

    return bytesRemaining;
  }

  @Override
  public int read(byte[] buffer, int offset, int readLength) throws FileDataSourceException {
    if (readLength == 0) {
      return 0;
    } else if (bytesRemaining == 0) {
      return C.RESULT_END_OF_INPUT;
    } else {
      int bytesRead;
      try {
        bytesRead = file.read(buffer, offset, (int) Math.min(bytesRemaining, readLength));
      } catch (IOException e) {
        throw new FileDataSourceException(e);
      }

      if (bytesRead > 0) {
        bytesRemaining -= bytesRead;
        bytesTransferred(bytesRead);
      }

      return bytesRead;
    }
  }

  @Override
  public @Nullable Uri getUri() {
    return uri;
  }

  @Override
  public void close() throws FileDataSourceException {
    uri = null;
    try {
      if (file != null) {
        file.close();
      }
    } catch (IOException e) {
      throw new FileDataSourceException(e);
    } finally {
      file = null;
      if (opened) {
        opened = false;
        transferEnded();
      }
    }
  }

}
