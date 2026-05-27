package minefarts.smarttube.extractor.ts;

import minefarts.smarttube.C;
import minefarts.smarttube.Format;
import minefarts.smarttube.extractor.ExtractorOutput;
import minefarts.smarttube.extractor.TrackOutput;
import minefarts.smarttube.text.cea.CeaUtil;
import minefarts.smarttube.utils.Assertions;
import minefarts.smarttube.utils.MimeTypes;
import minefarts.smarttube.utils.ParsableByteArray;
import java.util.List;

/** Consumes user data, outputting contained CEA-608/708 messages to a {@link TrackOutput}. */
/* package */ final class UserDataReader {

  private static final int USER_DATA_START_CODE = 0x0001B2;

  private final List<Format> closedCaptionFormats;
  private final TrackOutput[] outputs;

  public UserDataReader(List<Format> closedCaptionFormats) {
    this.closedCaptionFormats = closedCaptionFormats;
    outputs = new TrackOutput[closedCaptionFormats.size()];
  }

  public void createTracks(
      ExtractorOutput extractorOutput, TsPayloadReader.TrackIdGenerator idGenerator) {
    for (int i = 0; i < outputs.length; i++) {
      idGenerator.generateNewId();
      TrackOutput output = extractorOutput.track(idGenerator.getTrackId(), C.TRACK_TYPE_TEXT);
      Format channelFormat = closedCaptionFormats.get(i);
      String channelMimeType = channelFormat.sampleMimeType;
      Assertions.checkArgument(
          MimeTypes.APPLICATION_CEA608.equals(channelMimeType)
              || MimeTypes.APPLICATION_CEA708.equals(channelMimeType),
          "Invalid closed caption mime type provided: " + channelMimeType);
      output.format(
          Format.createTextSampleFormat(
              idGenerator.getFormatId(),
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

  public void consume(long pesTimeUs, ParsableByteArray userDataPayload) {
    if (userDataPayload.bytesLeft() < 9) {
      return;
    }
    int userDataStartCode = userDataPayload.readInt();
    int userDataIdentifier = userDataPayload.readInt();
    int userDataTypeCode = userDataPayload.readUnsignedByte();
    if (userDataStartCode == USER_DATA_START_CODE
        && userDataIdentifier == CeaUtil.USER_DATA_IDENTIFIER_GA94
        && userDataTypeCode == CeaUtil.USER_DATA_TYPE_CODE_MPEG_CC) {
      CeaUtil.consumeCcData(pesTimeUs, userDataPayload, outputs);
    }
  }
}
