package minefarts.smarttube;

import android.os.Looper;
import androidx.annotation.Nullable;
import minefarts.smarttube.audio.MediaCodecAudioRenderer;
import minefarts.smarttube.metadata.MetadataRenderer;
import minefarts.smarttube.source.ClippingMediaSource;
import minefarts.smarttube.source.ConcatenatingMediaSource;
import minefarts.smarttube.source.LoopingMediaSource;
import minefarts.smarttube.source.MediaSource;
import minefarts.smarttube.source.MergingMediaSource;
import minefarts.smarttube.source.ProgressiveMediaSource;
import minefarts.smarttube.source.SingleSampleMediaSource;
import minefarts.smarttube.text.TextRenderer;
import minefarts.smarttube.trackselection.TrackSelector;
import minefarts.smarttube.upstream.DataSource;
import minefarts.smarttube.video.MediaCodecVideoRenderer;

public interface ExoPlayer extends Player {

  /** @deprecated Use {@link PlayerMessage.Target} instead. */
  @Deprecated
  interface ExoPlayerComponent extends PlayerMessage.Target {}

  /** @deprecated Use {@link PlayerMessage} instead. */
  @Deprecated
  final class ExoPlayerMessage {

    /** The target to receive the message. */
    public final PlayerMessage.Target target;
    /** The type of the message. */
    public final int messageType;
    /** The message. */
    public final Object message;

    /** @deprecated Use {@link ExoPlayer#createMessage(PlayerMessage.Target)} instead. */
    @Deprecated
    public ExoPlayerMessage(PlayerMessage.Target target, int messageType, Object message) {
      this.target = target;
      this.messageType = messageType;
      this.message = message;
    }
  }

  /** Returns the {@link Looper} associated with the playback thread. */
  Looper getPlaybackLooper();

  /**
   * Retries a failed or stopped playback. Does nothing if the player has been reset, or if playback
   * has not failed or been stopped.
   */
  void retry();

  /**
   * Prepares the player to play the provided {@link MediaSource}. Equivalent to
   * {@code prepare(mediaSource, true, true)}.
   */
  void prepare(MediaSource mediaSource);

  /**
   * Prepares the player to play the provided {@link MediaSource}, optionally resetting the playback
   * position the default position in the first {@link Timeline.Window}.
   *
   * @param mediaSource The {@link MediaSource} to play.
   * @param resetPosition Whether the playback position should be reset to the default position in
   *     the first {@link Timeline.Window}. If false, playback will start from the position defined
   *     by {@link #getCurrentWindowIndex()} and {@link #getCurrentPosition()}.
   * @param resetState Whether the timeline, manifest, tracks and track selections should be reset.
   *     Should be true unless the player is being prepared to play the same media as it was playing
   *     previously (e.g. if playback failed and is being retried).
   */
  void prepare(MediaSource mediaSource, boolean resetPosition, boolean resetState);

  /**
   * Creates a message that can be sent to a {@link PlayerMessage.Target}. By default, the message
   * will be delivered immediately without blocking on the playback thread. The default {@link
   * PlayerMessage#getType()} is 0 and the default {@link PlayerMessage#getPayload()} is null. If a
   * position is specified with {@link PlayerMessage#setPosition(long)}, the message will be
   * delivered at this position in the current window defined by {@link #getCurrentWindowIndex()}.
   * Alternatively, the message can be sent at a specific window using {@link
   * PlayerMessage#setPosition(int, long)}.
   */
  PlayerMessage createMessage(PlayerMessage.Target target);

  /** @deprecated Use {@link #createMessage(PlayerMessage.Target)} instead. */
  @Deprecated
  @SuppressWarnings("deprecation")
  void sendMessages(ExoPlayerMessage... messages);

  /**
   * @deprecated Use {@link #createMessage(PlayerMessage.Target)} with {@link
   *     PlayerMessage#blockUntilDelivered()}.
   */
  @Deprecated
  @SuppressWarnings("deprecation")
  void blockingSendMessages(ExoPlayerMessage... messages);

  /**
   * Sets the parameters that control how seek operations are performed.
   *
   * @param seekParameters The seek parameters, or {@code null} to use the defaults.
   */
  void setSeekParameters(@Nullable SeekParameters seekParameters);

  /** Returns the currently active {@link SeekParameters} of the player. */
  SeekParameters getSeekParameters();

  /**
   * Sets whether the player is allowed to keep holding limited resources such as video decoders,
   * even when in the idle state. By doing so, the player may be able to reduce latency when
   * starting to play another piece of content for which the same resources are required.
   *
   * <p>This mode should be used with caution, since holding limited resources may prevent other
   * players of media components from acquiring them. It should only be enabled when <em>both</em>
   * of the following conditions are true:
   *
   * <ul>
   *   <li>The application that owns the player is in the foreground.
   *   <li>The player is used in a way that may benefit from foreground mode. For this to be true,
   *       the same player instance must be used to play multiple pieces of content, and there must
   *       be gaps between the playbacks (i.e. {@link #stop} is called to halt one playback, and
   *       {@link #prepare} is called some time later to start a new one).
   * </ul>
   *
   * <p>Note that foreground mode is <em>not</em> useful for switching between content without gaps
   * between the playbacks. For this use case {@link #stop} does not need to be called, and simply
   * calling {@link #prepare} for the new media will cause limited resources to be retained even if
   * foreground mode is not enabled.
   *
   * <p>If foreground mode is enabled, it's the application's responsibility to disable it when the
   * conditions described above no longer hold.
   *
   * @param foregroundMode Whether the player is allowed to keep limited resources even when in the
   *     idle state.
   */
  void setForegroundMode(boolean foregroundMode);
}
