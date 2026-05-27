package minefarts.exoplayer2.metadata;

import minefarts.exoplayer2.Format;
import minefarts.exoplayer2.metadata.emsg.EventMessageDecoder;
import minefarts.exoplayer2.metadata.icy.IcyDecoder;
import minefarts.exoplayer2.metadata.id3.Id3Decoder;
import minefarts.exoplayer2.metadata.scte35.SpliceInfoDecoder;
import minefarts.exoplayer2.util.MimeTypes;

/**
 * A factory for {@link MetadataDecoder} instances.
 */
public interface MetadataDecoderFactory {

  /**
   * Returns whether the factory is able to instantiate a {@link MetadataDecoder} for the given
   * {@link Format}.
   *
   * @param format The {@link Format}.
   * @return Whether the factory can instantiate a suitable {@link MetadataDecoder}.
   */
  boolean supportsFormat(Format format);

  /**
   * Creates a {@link MetadataDecoder} for the given {@link Format}.
   *
   * @param format The {@link Format}.
   * @return A new {@link MetadataDecoder}.
   * @throws IllegalArgumentException If the {@link Format} is not supported.
   */
  MetadataDecoder createDecoder(Format format);

  /**
   * Default {@link MetadataDecoder} implementation.
   *
   * <p>The formats supported by this factory are:
   *
   * <ul>
   *   <li>ID3 ({@link Id3Decoder})
   *   <li>EMSG ({@link EventMessageDecoder})
   *   <li>SCTE-35 ({@link SpliceInfoDecoder})
   *   <li>ICY ({@link IcyDecoder})
   * </ul>
   */
  MetadataDecoderFactory DEFAULT =
      new MetadataDecoderFactory() {

        @Override
        public boolean supportsFormat(Format format) {
          String mimeType = format.sampleMimeType;
          return MimeTypes.APPLICATION_ID3.equals(mimeType)
              || MimeTypes.APPLICATION_EMSG.equals(mimeType)
              || MimeTypes.APPLICATION_SCTE35.equals(mimeType)
              || MimeTypes.APPLICATION_ICY.equals(mimeType);
        }

        @Override
        public MetadataDecoder createDecoder(Format format) {
          switch (format.sampleMimeType) {
            case MimeTypes.APPLICATION_ID3:
              return new Id3Decoder();
            case MimeTypes.APPLICATION_EMSG:
              return new EventMessageDecoder();
            case MimeTypes.APPLICATION_SCTE35:
              return new SpliceInfoDecoder();
            case MimeTypes.APPLICATION_ICY:
              return new IcyDecoder();
            default:
              throw new IllegalArgumentException(
                  "Attempted to create decoder for unsupported format");
          }
        }
      };
}
