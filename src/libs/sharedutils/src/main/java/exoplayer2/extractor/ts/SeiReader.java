package minefarts.exoplayer2.extractor.ts;

import minefarts.exoplayer2.C;
import minefarts.exoplayer2.Format;
import minefarts.exoplayer2.extractor.ExtractorOutput;
import minefarts.exoplayer2.extractor.TrackOutput;
import minefarts.exoplayer2.extractor.ts.TsPayloadReader.TrackIdGenerator;
import minefarts.exoplayer2.text.cea.CeaUtil;
import minefarts.exoplayer2.util.Assertions;
import minefarts.exoplayer2.util.MimeTypes;
import minefarts.exoplayer2.util.ParsableByteArray;
import java.util.List;

/**
 * Consumes SEI buffers, outputting contained CEA-608 messages to a {@link TrackOutput}.
 */
/* package */ final class SeiReader {

  private final List<Format> closedCaptionFormats;
  private final TrackOutput[] outputs;

  /**
   * @param closedCaptionFormats A list of formats for the closed caption channels to expose.
   */
  public SeiReader(List<Format> closedCaptionFormats) {
    this.closedCaptionFormats = closedCaptionFormats;
    outputs = new TrackOutput[closedCaptionFormats.size()];
  }

  public void createTracks(ExtractorOutput extractorOutput, TrackIdGenerator idGenerator) {
    for (int i = 0; i < outputs.length; i++) {
      idGenerator.generateNewId();
      TrackOutput output = extractorOutput.track(idGenerator.getTrackId(), C.TRACK_TYPE_TEXT);
      Format channelFormat = closedCaptionFormats.get(i);
      String channelMimeType = channelFormat.sampleMimeType;
      Assertions.checkArgument(MimeTypes.APPLICATION_CEA608.equals(channelMimeType)
          || MimeTypes.APPLICATION_CEA708.equals(channelMimeType),
          "Invalid closed caption mime type provided: " + channelMimeType);
      String formatId = channelFormat.id != null ? channelFormat.id : idGenerator.getFormatId();
      output.format(
          Format.createTextSampleFormat(
              formatId,
              channelMimeType,
              /* codecs= */ null,
              /* bitrate= */ Format.NO_VALUE,
              channelFormat.selectionFlags,
              channelFormat.language,
              channelFormat.accessibilityChannel,
              /* drmInitData= */ null,
              Format.OFFSET_SAMPLE_RELATIVE,
              channelFormat.initializationData));
      outputs[i] = output;
    }
  }

  public void consume(long pesTimeUs, ParsableByteArray seiBuffer) {
    CeaUtil.consume(pesTimeUs, seiBuffer, outputs);
  }

}
