package minefarts.exoplayer2.text;

/**
 * Thrown when an error occurs decoding subtitle data.
 */
public class SubtitleDecoderException extends Exception {

  /**
   * @param message The detail message for this exception.
   */
  public SubtitleDecoderException(String message) {
    super(message);
  }

  /** @param cause The cause of this exception. */
  public SubtitleDecoderException(Exception cause) {
    super(cause);
  }

  /**
   * @param message The detail message for this exception.
   * @param cause The cause of this exception.
   */
  public SubtitleDecoderException(String message, Throwable cause) {
    super(message, cause);
  }

}
