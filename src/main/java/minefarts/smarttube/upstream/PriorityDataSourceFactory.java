package minefarts.smarttube.upstream;

import minefarts.smarttube.upstream.DataSource.Factory;
import minefarts.smarttube.utils.PriorityTaskManager;

/**
 * A {@link DataSource.Factory} that produces {@link PriorityDataSource} instances.
 */
public final class PriorityDataSourceFactory implements Factory {

  private final Factory upstreamFactory;
  private final PriorityTaskManager priorityTaskManager;
  private final int priority;

  /**
   * @param upstreamFactory A {@link DataSource.Factory} to be used to create an upstream {@link
   *     DataSource} for {@link PriorityDataSource}.
   * @param priorityTaskManager The priority manager to which PriorityDataSource task is registered.
   * @param priority The priority of PriorityDataSource task.
   */
  public PriorityDataSourceFactory(Factory upstreamFactory, PriorityTaskManager priorityTaskManager,
      int priority) {
    this.upstreamFactory = upstreamFactory;
    this.priorityTaskManager = priorityTaskManager;
    this.priority = priority;
  }

  @Override
  public PriorityDataSource createDataSource() {
    return new PriorityDataSource(upstreamFactory.createDataSource(), priorityTaskManager,
        priority);
  }

}
