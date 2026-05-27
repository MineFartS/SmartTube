package minefarts.smarttube.upstream;

import android.net.Uri;
import androidx.annotation.Nullable;
import minefarts.smarttube.utils.Assertions;
import minefarts.smarttube.utils.PriorityTaskManager;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A {@link DataSource} that can be used as part of a task registered with a
 * {@link PriorityTaskManager}.
 * <p>
 * Calls to {@link #open(DataSpec)} and {@link #read(byte[], int, int)} are allowed to proceed only
 * if there are no higher priority tasks registered to the {@link PriorityTaskManager}. If there
 * exists a higher priority task then {@link PriorityTaskManager.PriorityTooLowException} is thrown.
 * <p>
 * Instances of this class are intended to be used as parts of (possibly larger) tasks that are
 * registered with the {@link PriorityTaskManager}, and hence do <em>not</em> register as tasks
 * themselves.
 */
public final class PriorityDataSource implements DataSource {

  private final DataSource upstream;
  private final PriorityTaskManager priorityTaskManager;
  private final int priority;

  /**
   * @param upstream The upstream {@link DataSource}.
   * @param priorityTaskManager The priority manager to which the task is registered.
   * @param priority The priority of the task.
   */
  public PriorityDataSource(DataSource upstream, PriorityTaskManager priorityTaskManager,
      int priority) {
    this.upstream = Assertions.checkNotNull(upstream);
    this.priorityTaskManager = Assertions.checkNotNull(priorityTaskManager);
    this.priority = priority;
  }

  @Override
  public void addTransferListener(TransferListener transferListener) {
    upstream.addTransferListener(transferListener);
  }

  @Override
  public long open(DataSpec dataSpec) throws IOException {
    priorityTaskManager.proceedOrThrow(priority);
    return upstream.open(dataSpec);
  }

  @Override
  public int read(byte[] buffer, int offset, int max) throws IOException {
    priorityTaskManager.proceedOrThrow(priority);
    return upstream.read(buffer, offset, max);
  }

  @Override
  public @Nullable Uri getUri() {
    return upstream.getUri();
  }

  @Override
  public Map<String, List<String>> getResponseHeaders() {
    return upstream.getResponseHeaders();
  }

  @Override
  public void close() throws IOException {
    upstream.close();
  }

}
