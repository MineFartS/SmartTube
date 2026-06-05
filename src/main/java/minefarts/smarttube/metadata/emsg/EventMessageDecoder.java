package minefarts.smarttube.metadata.emsg;

import androidx.annotation.Nullable;
import minefarts.smarttube.metadata.Metadata;
import minefarts.smarttube.metadata.MetadataDecoder;
import minefarts.smarttube.metadata.MetadataInputBuffer;
import minefarts.smarttube.utils.Assertions;
import minefarts.smarttube.utils.ParsableByteArray;
import java.nio.ByteBuffer;
import java.util.Arrays;

/** Decodes data encoded by {@link EventMessageEncoder}. */
public final class EventMessageDecoder implements MetadataDecoder {

  @SuppressWarnings("ByteBufferBackingArray")
  @Override
  @Nullable
  public Metadata decode(MetadataInputBuffer inputBuffer) {
    ByteBuffer buffer = inputBuffer.data;
    byte[] data = buffer.array();
    int size = buffer.limit();
    EventMessage decodedEventMessage = decode(new ParsableByteArray(data, size));
    if (decodedEventMessage == null) {
      return null;
    } else {
      return new Metadata(decodedEventMessage);
    }
  }

  @Nullable
  public EventMessage decode(ParsableByteArray emsgData) {
    try {
      String schemeIdUri = Assertions.checkNotNull(emsgData.readNullTerminatedString());
      String value = Assertions.checkNotNull(emsgData.readNullTerminatedString());
      long durationMs = emsgData.readUnsignedInt();
      long id = emsgData.readUnsignedInt();
      byte[] messageData =
          Arrays.copyOfRange(emsgData.data, emsgData.getPosition(), emsgData.limit());
      return new EventMessage(schemeIdUri, value, durationMs, id, messageData);
    } catch (RuntimeException e) {
      return null;
    }
  }
}
