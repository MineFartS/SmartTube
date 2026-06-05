package minefarts.smarttube.video.spherical;

import androidx.annotation.Nullable;
import minefarts.smarttube.BaseRenderer;
import minefarts.smarttube.C;
import minefarts.smarttube.ExoPlaybackException;
import minefarts.smarttube.Format;
import minefarts.smarttube.FormatHolder;
import minefarts.smarttube.Renderer;
import minefarts.smarttube.decoder.DecoderInputBuffer;
import minefarts.smarttube.utils.MimeTypes;
import minefarts.smarttube.utils.ParsableByteArray;
import minefarts.smarttube.utils.Utils;
import java.nio.ByteBuffer;

/** A {@link Renderer} that parses the camera motion track. */
public class CameraMotionRenderer extends BaseRenderer {

  // The amount of time to read samples ahead of the current time.
  private static final int SAMPLE_WINDOW_DURATION_US = 100000;

  private final FormatHolder formatHolder;
  private final DecoderInputBuffer buffer;
  private final ParsableByteArray scratch;

  private long offsetUs;
  private @Nullable CameraMotionListener listener;
  private long lastTimestampUs;

  public CameraMotionRenderer() {
    super(C.TRACK_TYPE_CAMERA_MOTION);
    formatHolder = new FormatHolder();
    buffer = new DecoderInputBuffer(DecoderInputBuffer.BUFFER_REPLACEMENT_MODE_NORMAL);
    scratch = new ParsableByteArray();
  }

  @Override
  public int supportsFormat(Format format) {
    return MimeTypes.APPLICATION_CAMERA_MOTION.equals(format.sampleMimeType)
        ? FORMAT_HANDLED
        : FORMAT_UNSUPPORTED_TYPE;
  }

  @Override
  public void handleMessage(int messageType, @Nullable Object message) throws ExoPlaybackException {
    if (messageType == C.MSG_SET_CAMERA_MOTION_LISTENER) {
      listener = (CameraMotionListener) message;
    } else {
      super.handleMessage(messageType, message);
    }
  }

  @Override
  protected void onStreamChanged(Format[] formats, long offsetUs) throws ExoPlaybackException {
    this.offsetUs = offsetUs;
  }

  @Override
  protected void onPositionReset(long positionUs, boolean joining) throws ExoPlaybackException {
    resetListener();
  }

  @Override
  protected void onDisabled() {
    resetListener();
  }

  @Override
  public void render(long positionUs, long elapsedRealtimeUs) throws ExoPlaybackException {
    // Keep reading available samples as long as the sample time is not too far into the future.
    while (!hasReadStreamToEnd() && lastTimestampUs < positionUs + SAMPLE_WINDOW_DURATION_US) {
      buffer.clear();
      int result = readSource(formatHolder, buffer, /* formatRequired= */ false);
      if (result != C.RESULT_BUFFER_READ || buffer.isEndOfStream()) {
        return;
      }

      buffer.flip();
      lastTimestampUs = buffer.timeUs;
      if (listener != null) {
        float[] rotation = parseMetadata(buffer.data);
        if (rotation != null) {
          Utils.castNonNull(listener).onCameraMotion(lastTimestampUs - offsetUs, rotation);
        }
      }
    }
  }

  @Override
  public boolean isEnded() {
    return hasReadStreamToEnd();
  }

  @Override
  public boolean isReady() {
    return true;
  }

  private @Nullable float[] parseMetadata(ByteBuffer data) {
    if (data.remaining() != 16) {
      return null;
    }
    scratch.reset(data.array(), data.limit());
    scratch.setPosition(data.arrayOffset() + 4); // skip reserved bytes too.
    float[] result = new float[3];
    for (int i = 0; i < 3; i++) {
      result[i] = Float.intBitsToFloat(scratch.readLittleEndianInt());
    }
    return result;
  }

  private void resetListener() {
    lastTimestampUs = 0;
    if (listener != null) {
      listener.onCameraMotionReset();
    }
  }
}
