package minefarts.smarttube.audio;

/** Thrown when an audio decoder error occurs. */
public class AudioDecoderException extends Exception {

  /** @param message The detail message for this exception. */
  public AudioDecoderException(String message) {
    super(message);
  }

  /**
   * @param message The detail message for this exception.
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or unknown.
   */
  public AudioDecoderException(String message, Throwable cause) {
    super(message, cause);
  }

}
