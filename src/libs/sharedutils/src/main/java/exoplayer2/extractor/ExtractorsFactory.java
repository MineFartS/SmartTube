package minefarts.exoplayer2.extractor;

/** Factory for arrays of {@link Extractor} instances. */
public interface ExtractorsFactory {

  /** Returns an array of new {@link Extractor} instances. */
  Extractor[] createExtractors();
}
