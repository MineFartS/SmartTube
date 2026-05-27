package minefarts.exoplayer2.metadata;

import androidx.annotation.Nullable;

/**
 * Decodes metadata from binary data.
 */
public interface MetadataDecoder {

  /**
   * Decodes a {@link Metadata} element from the provided input buffer.
   *
   * @param inputBuffer The input buffer to decode.
   * @return The decoded metadata object, or null if the metadata could not be decoded.
   */
  @Nullable
  Metadata decode(MetadataInputBuffer inputBuffer);
}
