package minefarts.smarttube.text;

import minefarts.smarttube.decoder.Decoder;

/**
 * Decodes {@link Subtitle}s from {@link SubtitleInputBuffer}s.
 */
public interface SubtitleDecoder extends
    Decoder<SubtitleInputBuffer, SubtitleOutputBuffer, SubtitleDecoderException> {

  /**
   * Informs the decoder of the current playback position.
   * <p>
   * Must be called prior to each attempt to dequeue output buffers from the decoder.
   *
   * @param positionUs The current playback position in microseconds.
   */
  void setPositionUs(long positionUs);

}
