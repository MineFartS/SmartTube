package minefarts.smarttube.source;

import android.net.Uri;
import minefarts.smarttube.ParserException;

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
