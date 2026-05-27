package minefarts.exoplayer2.upstream;

import android.net.Uri;
import androidx.annotation.Nullable;
import minefarts.exoplayer2.C;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A component from which streams of data can be read.
 */
public interface DataSource {

  /**
   * A factory for {@link DataSource} instances.
   */
  interface Factory {

    /**
     * Creates a {@link DataSource} instance.
     */
    DataSource createDataSource();
  }

  /**
   * Adds a {@link TransferListener} to listen to data transfers. This method is not thread-safe.
   *
   * @param transferListener A {@link TransferListener}.
   */
  void addTransferListener(TransferListener transferListener);

  /**
   * Opens the source to read the specified data.
   * <p>
   * Note: If an {@link IOException} is thrown, callers must still call {@link #close()} to ensure
   * that any partial effects of the invocation are cleaned up.
   *
   * @param dataSpec Defines the data to be read.
   * @throws IOException If an error occurs opening the source. {@link DataSourceException} can be
   *     thrown or used as a cause of the thrown exception to specify the reason of the error.
   * @return The number of bytes that can be read from the opened source. For unbounded requests
   *     (i.e. requests where {@link DataSpec#length} equals {@link C#LENGTH_UNSET}) this value
   *     is the resolved length of the request, or {@link C#LENGTH_UNSET} if the length is still
   *     unresolved. For all other requests, the value returned will be equal to the request's
   *     {@link DataSpec#length}.
   */
  long open(DataSpec dataSpec) throws IOException;

  /**
   * Reads up to {@code readLength} bytes of data and stores them into {@code buffer}, starting at
   * index {@code offset}.
   *
   * <p>If {@code readLength} is zero then 0 is returned. Otherwise, if no data is available because
   * the end of the opened range has been reached, then {@link C#RESULT_END_OF_INPUT} is returned.
   * Otherwise, the call will block until at least one byte of data has been read and the number of
   * bytes read is returned.
   *
   * @param buffer The buffer into which the read data should be stored.
   * @param offset The start offset into {@code buffer} at which data should be written.
   * @param readLength The maximum number of bytes to read.
   * @return The number of bytes read, or {@link C#RESULT_END_OF_INPUT} if no data is available
   *     because the end of the opened range has been reached.
   * @throws IOException If an error occurs reading from the source.
   */
  int read(byte[] buffer, int offset, int readLength) throws IOException;

  /**
   * When the source is open, returns the {@link Uri} from which data is being read. The returned
   * {@link Uri} will be identical to the one passed {@link #open(DataSpec)} in the {@link DataSpec}
   * unless redirection has occurred. If redirection has occurred, the {@link Uri} after redirection
   * is returned.
   *
   * @return The {@link Uri} from which data is being read, or null if the source is not open.
   */
  @Nullable Uri getUri();

  /**
   * When the source is open, returns the response headers associated with the last {@link #open}
   * call. Otherwise, returns an empty map.
   */
  default Map<String, List<String>> getResponseHeaders() {
    return Collections.emptyMap();
  }

  /**
   * Closes the source.
   * <p>
   * Note: This method must be called even if the corresponding call to {@link #open(DataSpec)}
   * threw an {@link IOException}. See {@link #open(DataSpec)} for more details.
   *
   * @throws IOException If an error occurs closing the source.
   */
  void close() throws IOException;
}
