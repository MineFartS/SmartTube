package minefarts.exoplayer2.ui;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import minefarts.exoplayer2.offline.Download;
import minefarts.exoplayer2.util.Util;
import java.util.List;

/**
 * @deprecated Using this class can cause notifications to flicker on devices with {@link
 *     Util#SDK_INT} &lt; 21. Use {@link DownloadNotificationHelper} instead.
 */
@Deprecated
public final class DownloadNotificationUtil {

  private DownloadNotificationUtil() {}

  /**
   * Returns a progress notification for the given downloads.
   *
   * @param context A context for accessing resources.
   * @param smallIcon A small icon for the notification.
   * @param channelId The id of the notification channel to use.
   * @param contentIntent An optional content intent to send when the notification is clicked.
   * @param message An optional message to display on the notification.
   * @param downloads The downloads.
   * @return The notification.
   */
  public static Notification buildProgressNotification(
      Context context,
      @DrawableRes int smallIcon,
      String channelId,
      @Nullable PendingIntent contentIntent,
      @Nullable String message,
      List<Download> downloads) {
    return new DownloadNotificationHelper(context, channelId)
        .buildProgressNotification(smallIcon, contentIntent, message, downloads);
  }

  /**
   * Returns a notification for a completed download.
   *
   * @param context A context for accessing resources.
   * @param smallIcon A small icon for the notifications.
   * @param channelId The id of the notification channel to use.
   * @param contentIntent An optional content intent to send when the notification is clicked.
   * @param message An optional message to display on the notification.
   * @return The notification.
   */
  public static Notification buildDownloadCompletedNotification(
      Context context,
      @DrawableRes int smallIcon,
      String channelId,
      @Nullable PendingIntent contentIntent,
      @Nullable String message) {
    return new DownloadNotificationHelper(context, channelId)
        .buildDownloadCompletedNotification(smallIcon, contentIntent, message);
  }

  /**
   * Returns a notification for a failed download.
   *
   * @param context A context for accessing resources.
   * @param smallIcon A small icon for the notifications.
   * @param channelId The id of the notification channel to use.
   * @param contentIntent An optional content intent to send when the notification is clicked.
   * @param message An optional message to display on the notification.
   * @return The notification.
   */
  public static Notification buildDownloadFailedNotification(
      Context context,
      @DrawableRes int smallIcon,
      String channelId,
      @Nullable PendingIntent contentIntent,
      @Nullable String message) {
    return new DownloadNotificationHelper(context, channelId)
        .buildDownloadFailedNotification(smallIcon, contentIntent, message);
  }
}
