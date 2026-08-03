package minefarts.smarttube.source;

/** @deprecated Use {@link ConcatenatingMediaSource} instead. */
@Deprecated
public final class DynamicConcatenatingMediaSource extends ConcatenatingMediaSource {

  /**
   * @deprecated Use {@link ConcatenatingMediaSource#ConcatenatingMediaSource(MediaSource...)}
   *     instead.
   */
  @Deprecated
  public DynamicConcatenatingMediaSource() {}

  /**
   * @deprecated Use {@link ConcatenatingMediaSource#ConcatenatingMediaSource(boolean,
   *     MediaSource...)} instead.
   */
  @Deprecated
  public DynamicConcatenatingMediaSource(boolean isAtomic) {
    super(isAtomic);
  }

  /**
   * @deprecated Use {@link ConcatenatingMediaSource#ConcatenatingMediaSource(boolean, ShuffleOrder,
   *     MediaSource...)} instead.
   */
  @Deprecated
  public DynamicConcatenatingMediaSource(boolean isAtomic, ShuffleOrder shuffleOrder) {
    super(isAtomic, shuffleOrder);
  }
}
