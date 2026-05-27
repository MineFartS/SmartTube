package minefarts.smarttube.source;

/**
 * Default implementation of {@link CompositeSequenceableLoaderFactory}.
 */
public final class DefaultCompositeSequenceableLoaderFactory
    implements CompositeSequenceableLoaderFactory {

  @Override
  public SequenceableLoader createCompositeSequenceableLoader(SequenceableLoader... loaders) {
    return new CompositeSequenceableLoader(loaders);
  }

}
