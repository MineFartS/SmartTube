package minefarts.exoplayer2.source.sabr;

import minefarts.exoplayer2.C;
import minefarts.exoplayer2.Format;
import minefarts.exoplayer2.FormatHolder;
import minefarts.exoplayer2.decoder.DecoderInputBuffer;
import minefarts.exoplayer2.metadata.emsg.EventMessage;
import minefarts.exoplayer2.metadata.emsg.EventMessageEncoder;
import minefarts.exoplayer2.source.SampleStream;
import minefarts.exoplayer2.source.sabr.manifest.EventStream;
import minefarts.exoplayer2.util.Util;

import java.io.IOException;

/**
 * A {@link SampleStream} consisting of serialized {@link EventMessage}s read from an
 * {@link EventStream}.
 */
/* package */ final class EventSampleStream implements SampleStream {

  private final Format upstreamFormat;
  private final EventMessageEncoder eventMessageEncoder;

  private long[] eventTimesUs;
  private boolean eventStreamAppendable;
  private EventStream eventStream;

  private boolean isFormatSentDownstream;
  private int currentIndex;
  private long pendingSeekPositionUs;

  public EventSampleStream(
      EventStream eventStream, Format upstreamFormat, boolean eventStreamAppendable) {
    this.upstreamFormat = upstreamFormat;
    this.eventStream = eventStream;
    eventMessageEncoder = new EventMessageEncoder();
    pendingSeekPositionUs = C.TIME_UNSET;
    eventTimesUs = eventStream.presentationTimesUs;
    updateEventStream(eventStream, eventStreamAppendable);
  }

  public String eventStreamId() {
    return eventStream.id();
  }

  public void updateEventStream(EventStream eventStream, boolean eventStreamAppendable) {
    long lastReadPositionUs = currentIndex == 0 ? C.TIME_UNSET : eventTimesUs[currentIndex - 1];

    this.eventStreamAppendable = eventStreamAppendable;
    this.eventStream = eventStream;
    this.eventTimesUs = eventStream.presentationTimesUs;
    if (pendingSeekPositionUs != C.TIME_UNSET) {
      seekToUs(pendingSeekPositionUs);
    } else if (lastReadPositionUs != C.TIME_UNSET) {
      currentIndex =
          Util.binarySearchCeil(
              eventTimesUs, lastReadPositionUs, /* inclusive= */ false, /* stayInBounds= */ false);
    }
  }

  /**
   * Seeks to the specified position in microseconds.
   *
   * @param positionUs The seek position in microseconds.
   */
  public void seekToUs(long positionUs) {
    currentIndex =
        Util.binarySearchCeil(
            eventTimesUs, positionUs, /* inclusive= */ true, /* stayInBounds= */ false);
    boolean isPendingSeek = eventStreamAppendable && currentIndex == eventTimesUs.length;
    pendingSeekPositionUs = isPendingSeek ? positionUs : C.TIME_UNSET;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void maybeThrowError() throws IOException {
    // Do nothing.
  }

  @Override
  public int readData(FormatHolder formatHolder, DecoderInputBuffer buffer,
      boolean formatRequired) {
    if (formatRequired || !isFormatSentDownstream) {
      formatHolder.format = upstreamFormat;
      isFormatSentDownstream = true;
      return C.RESULT_FORMAT_READ;
    }
    if (currentIndex == eventTimesUs.length) {
      if (!eventStreamAppendable) {
        buffer.setFlags(C.BUFFER_FLAG_END_OF_STREAM);
        return C.RESULT_BUFFER_READ;
      } else {
        return C.RESULT_NOTHING_READ;
      }
    }
    int sampleIndex = currentIndex++;
    byte[] serializedEvent = eventMessageEncoder.encode(eventStream.events[sampleIndex]);
    if (serializedEvent != null) {
      buffer.ensureSpaceForWrite(serializedEvent.length);
      buffer.setFlags(C.BUFFER_FLAG_KEY_FRAME);
      buffer.data.put(serializedEvent);
      buffer.timeUs = eventTimesUs[sampleIndex];
      return C.RESULT_BUFFER_READ;
    } else {
      return C.RESULT_NOTHING_READ;
    }
  }

  @Override
  public int skipData(long positionUs) {
    int newIndex =
        Math.max(currentIndex, Util.binarySearchCeil(eventTimesUs, positionUs, true, false));
    int skipped = newIndex - currentIndex;
    currentIndex = newIndex;
    return skipped;
  }

}
