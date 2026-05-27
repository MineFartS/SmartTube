package minefarts.smarttube.utils;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;

/**
 * An interface through which system clocks can be read and {@link HandlerWrapper}s created. The
 * {@link #DEFAULT} implementation must be used for all non-test cases.
 */
public interface Clock {

  /**
   * Default {@link Clock} to use for all non-test cases.
   */
  Clock DEFAULT = new SystemClock();

  /** @see android.os.SystemClock#elapsedRealtime() */
  long elapsedRealtime();

  /** @see android.os.SystemClock#uptimeMillis() */
  long uptimeMillis();

  /** @see android.os.SystemClock#sleep(long) */
  void sleep(long sleepTimeMs);

  /**
   * Creates a {@link HandlerWrapper} using a specified looper and a specified callback for handling
   * messages.
   *
   * @see Handler#Handler(Looper, Handler.Callback)
   */
  HandlerWrapper createHandler(Looper looper, @Nullable Handler.Callback callback);
}
