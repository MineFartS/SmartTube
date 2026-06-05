package minefarts.smarttube.extractor.ts;

import static minefarts.smarttube.extractor.ts.TsPayloadReader.FLAG_DATA_ALIGNMENT_INDICATOR;

import minefarts.smarttube.C;
import minefarts.smarttube.Format;
import minefarts.smarttube.extractor.ExtractorOutput;
import minefarts.smarttube.extractor.TrackOutput;
import minefarts.smarttube.extractor.ts.TsPayloadReader.DvbSubtitleInfo;
import minefarts.smarttube.extractor.ts.TsPayloadReader.TrackIdGenerator;
import minefarts.smarttube.utils.MimeTypes;
import minefarts.smarttube.utils.ParsableByteArray;
import java.util.Collections;
import java.util.List;

/**
 * Parses DVB subtitle data and extracts individual frames.
 */
public final class DvbSubtitleReader implements ElementaryStreamReader {

  private final List<DvbSubtitleInfo> subtitleInfos;
  private final TrackOutput[] outputs;

  private boolean writingSample;
  private int bytesToCheck;
  private int sampleBytesWritten;
  private long sampleTimeUs;

  /**
   * @param subtitleInfos Information about the DVB subtitles associated to the stream.
   */
  public DvbSubtitleReader(List<DvbSubtitleInfo> subtitleInfos) {
    this.subtitleInfos = subtitleInfos;
    outputs = new TrackOutput[subtitleInfos.size()];
  }

  @Override
  public void seek() {
    writingSample = false;
  }

  @Override
  public void createTracks(ExtractorOutput extractorOutput, TrackIdGenerator idGenerator) {
    for (int i = 0; i < outputs.length; i++) {
      DvbSubtitleInfo subtitleInfo = subtitleInfos.get(i);
      idGenerator.generateNewId();
      TrackOutput output = extractorOutput.track(idGenerator.getTrackId(), C.TRACK_TYPE_TEXT);
      output.format(
          Format.createImageSampleFormat(
              idGenerator.getFormatId(),
              MimeTypes.APPLICATION_DVBSUBS,
              null,
              Format.NO_VALUE,
              0,
              Collections.singletonList(subtitleInfo.initializationData),
              subtitleInfo.language,
              null));
      outputs[i] = output;
    }
  }

  @Override
  public void packetStarted(long pesTimeUs, @TsPayloadReader.Flags int flags) {
    if ((flags & FLAG_DATA_ALIGNMENT_INDICATOR) == 0) {
      return;
    }
    writingSample = true;
    sampleTimeUs = pesTimeUs;
    sampleBytesWritten = 0;
    bytesToCheck = 2;
  }

  @Override
  public void packetFinished() {
    if (writingSample) {
      for (TrackOutput output : outputs) {
        output.sampleMetadata(sampleTimeUs, C.BUFFER_FLAG_KEY_FRAME, sampleBytesWritten, 0, null);
      }
      writingSample = false;
    }
  }

  @Override
  public void consume(ParsableByteArray data) {
    if (writingSample) {
      if (bytesToCheck == 2 && !checkNextByte(data, 0x20)) {
        // Failed to check data_identifier
        return;
      }
      if (bytesToCheck == 1 && !checkNextByte(data, 0x00)) {
        // Check and discard the subtitle_stream_id
        return;
      }
      int dataPosition = data.getPosition();
      int bytesAvailable = data.bytesLeft();
      for (TrackOutput output : outputs) {
        data.setPosition(dataPosition);
        output.sampleData(data, bytesAvailable);
      }
      sampleBytesWritten += bytesAvailable;
    }
  }

  private boolean checkNextByte(ParsableByteArray data, int expectedValue) {
    if (data.bytesLeft() == 0) {
      return false;
    }
    if (data.readUnsignedByte() != expectedValue) {
      writingSample = false;
    }
    bytesToCheck--;
    return writingSample;
  }

}
