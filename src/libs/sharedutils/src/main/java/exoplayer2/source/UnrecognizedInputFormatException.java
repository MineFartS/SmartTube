package minefarts.exoplayer2.source;

import android.net.Uri;
import minefarts.exoplayer2.ParserException;

/**
 * Thrown if the input format was not recognized.
 */
public class UnrecognizedInputFormatException extends ParserException {

  /**
   * The {@link Uri} from which the unrecognized data was read.
   */
  public final Uri uri;

  /**
   * @param message The detail message for the exception.
   * @param uri The {@link Uri} from which the unrecognized data was read.
   */
  public UnrecognizedInputFormatException(String message, Uri uri) {
    super(message);
    this.uri = uri;
  }

}
