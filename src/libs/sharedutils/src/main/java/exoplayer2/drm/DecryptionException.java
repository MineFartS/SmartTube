package minefarts.exoplayer2.drm;

/**
 * Thrown when a non-platform component fails to decrypt data.
 */
public class DecryptionException extends Exception {

  /**
   * A component specific error code.
   */
  public final int errorCode;

  /**
   * @param errorCode A component specific error code.
   * @param message The detail message.
   */
  public DecryptionException(int errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

}
