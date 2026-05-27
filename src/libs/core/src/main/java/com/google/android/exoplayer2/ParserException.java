package com.google.android.exoplayer2;

import java.io.IOException;

/**
 * Thrown when an error occurs parsing media data and metadata.
 */
public class ParserException extends IOException {

  public ParserException() {
    super();
  }

  /**
   * @param message The detail message for the exception.
   */
  public ParserException(String message) {
    super(message);
  }

  /**
   * @param cause The cause for the exception.
   */
  public ParserException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message The detail message for the exception.
   * @param cause The cause for the exception.
   */
  public ParserException(String message, Throwable cause) {
    super(message, cause);
  }

}
