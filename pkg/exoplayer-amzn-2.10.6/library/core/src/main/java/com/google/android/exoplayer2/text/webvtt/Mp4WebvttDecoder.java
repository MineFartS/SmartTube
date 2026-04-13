
package com.google.android.exoplayer2.text.webvtt;

import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.SimpleSubtitleDecoder;
import com.google.android.exoplayer2.text.SubtitleDecoderException;
import com.google.android.exoplayer2.util.ParsableByteArray;
import com.google.android.exoplayer2.util.Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link SimpleSubtitleDecoder} for Webvtt embedded in a Mp4 container file.
 */
public final class Mp4WebvttDecoder extends SimpleSubtitleDecoder {

  private static final int BOX_HEADER_SIZE = 8;

  private static final int TYPE_payl = Util.getIntegerCodeForString("payl");
  private static final int TYPE_sttg = Util.getIntegerCodeForString("sttg");
  private static final int TYPE_vttc = Util.getIntegerCodeForString("vttc");

  private final ParsableByteArray sampleData;
  private final WebvttCue.Builder builder;

  public Mp4WebvttDecoder() {
    super("Mp4WebvttDecoder");
    sampleData = new ParsableByteArray();
    builder = new WebvttCue.Builder();
  }

  @Override
  protected Mp4WebvttSubtitle decode(byte[] bytes, int length, boolean reset)
      throws SubtitleDecoderException {
    // Webvtt in Mp4 samples have boxes inside of them, so we have to do a traditional box parsing:
    // first 4 bytes size and then 4 bytes type.
    sampleData.reset(bytes, length);
    List<Cue> resultingCueList = new ArrayList<>();
    while (sampleData.bytesLeft() > 0) {
      if (sampleData.bytesLeft() < BOX_HEADER_SIZE) {
        throw new SubtitleDecoderException("Incomplete Mp4Webvtt Top Level box header found.");
      }
      int boxSize = sampleData.readInt();
      int boxType = sampleData.readInt();
      if (boxType == TYPE_vttc) {
        resultingCueList.add(parseVttCueBox(sampleData, builder, boxSize - BOX_HEADER_SIZE));
      } else {
        // Peers of the VTTCueBox are still not supported and are skipped.
        sampleData.skipBytes(boxSize - BOX_HEADER_SIZE);
      }
    }
    return new Mp4WebvttSubtitle(resultingCueList);
  }

  private static Cue parseVttCueBox(ParsableByteArray sampleData, WebvttCue.Builder builder,
        int remainingCueBoxBytes) throws SubtitleDecoderException {
    builder.reset();
    while (remainingCueBoxBytes > 0) {
      if (remainingCueBoxBytes < BOX_HEADER_SIZE) {
        throw new SubtitleDecoderException("Incomplete vtt cue box header found.");
      }
      int boxSize = sampleData.readInt();
      int boxType = sampleData.readInt();
      remainingCueBoxBytes -= BOX_HEADER_SIZE;
      int payloadLength = boxSize - BOX_HEADER_SIZE;
      String boxPayload =
          Util.fromUtf8Bytes(sampleData.data, sampleData.getPosition(), payloadLength);
      sampleData.skipBytes(payloadLength);
      remainingCueBoxBytes -= payloadLength;
      if (boxType == TYPE_sttg) {
        WebvttCueParser.parseCueSettingsList(boxPayload, builder);
      } else if (boxType == TYPE_payl) {
        WebvttCueParser.parseCueText(null, boxPayload.trim(), builder, Collections.emptyList());
      } else {
        // Other VTTCueBox children are still not supported and are ignored.
      }
    }
    return builder.build();
  }

}
