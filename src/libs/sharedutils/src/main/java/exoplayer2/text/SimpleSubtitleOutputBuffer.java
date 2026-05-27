package minefarts.exoplayer2.text;

/**
 * A {@link SubtitleOutputBuffer} for decoders that extend {@link SimpleSubtitleDecoder}.
 */
/* package */ final class SimpleSubtitleOutputBuffer extends SubtitleOutputBuffer {

  private final SimpleSubtitleDecoder owner;

  /**
   * @param owner The decoder that owns this buffer.
   */
  public SimpleSubtitleOutputBuffer(SimpleSubtitleDecoder owner) {
    super();
    this.owner = owner;
  }

  @Override
  public final void release() {
    owner.releaseOutputBuffer(this);
  }

}
