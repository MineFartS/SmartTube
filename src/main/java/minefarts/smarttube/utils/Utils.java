package minefarts.smarttube.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.BaseInputConnection;
import static android.content.Context.UI_MODE_SERVICE;
import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.media.AudioFormat;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.security.NetworkSecurityPolicy;
import androidx.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.jakewharton.processphoenix.ProcessPhoenix;

import minefarts.smarttube.utils.helpers.DeviceHelpers;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.helpers.MessageHelpers;
import minefarts.smarttube.utils.misc.WeakHashSet;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.BuildConfig;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.ui.playback.PlaybackFragment2;
import minefarts.smarttube.app.models.playback.service.VideoStateService;
import minefarts.smarttube.app.presenters.PlaybackPresenter;
import minefarts.smarttube.app.presenters.SplashPresenter;
import minefarts.smarttube.app.views.ViewManager;
import minefarts.smarttube.exoplayer.selector.FormatItem.VideoPreset;
import minefarts.smarttube.exoplayer.selector.TrackSelectorUtil;
import minefarts.smarttube.exoplayer.selector.track.MediaTrack;
import minefarts.smarttube.utils.MotherActivity;
import minefarts.smarttube.utils.RemoteControlService;
import minefarts.smarttube.utils.RemoteControlWorker;
import minefarts.smarttube.prefs.GeneralData;
import minefarts.smarttube.prefs.HiddenPrefs;
import minefarts.smarttube.prefs.MainUIData;
import minefarts.smarttube.prefs.PlayerTweaksData;
import minefarts.smarttube.prefs.RemoteControlData;
import minefarts.smarttube.utils.service.internal.MediaServiceData;
import minefarts.smarttube.C;
import minefarts.smarttube.ExoPlayerLibraryInfo;
import minefarts.smarttube.Format;
import minefarts.smarttube.ParserException;
import minefarts.smarttube.Renderer;
import minefarts.smarttube.RendererCapabilities;
import minefarts.smarttube.RenderersFactory;
import minefarts.smarttube.SeekParameters;
import minefarts.smarttube.audio.AudioRendererEventListener;
import minefarts.smarttube.drm.DrmSessionManager;
import minefarts.smarttube.drm.FrameworkMediaCrypto;
import minefarts.smarttube.upstream.DataSource;
import minefarts.smarttube.video.VideoRendererEventListener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.compatqual.NullableType;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.PolyNull;

public class Utils {

    private static final String UNIVERSAL_PASSWD = "smarttube";
    private static final int RANDOM_FAIL_REPEAT_TIMES = 10;
    private static final String REMOTE_CONTROL_RECEIVER_CLASS_NAME = "minefarts.smarttube.utils.RemoteControlReceiver";
    private static final String UPDATE_CHANNELS_RECEIVER_CLASS_NAME = "minefarts.smarttube.channels.UpdateChannelsReceiver";
    private static final String BOOTSTRAP_ACTIVITY_CLASS_NAME = "minefarts.smarttube.ui.main.SplashActivity";
    private static final String TASK_ID = RemoteControlWorker.class.getSimpleName();
    private static final String TAG = Utils.class.getSimpleName();
    private static final String QR_CODE_URL_TEMPLATE = "https://api.qrserver.com/v1/create-qr-code/?data=%s";

    public static final Handler sHandler = new Handler(Looper.getMainLooper());

    private static final Runnable sForceFinishTheApp = () -> Runtime.getRuntime().exit(0);

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @TargetApi(17)
    public static void displayShareVideoDialog(Context context, String videoId) {
        displayShareVideoDialog(context, videoId, 0);
    }

    @TargetApi(17)
    public static void displayShareVideoDialog(Context context, String videoId, int posSec) {
        Uri videoUrl = convertToFullVideoUrl(videoId, posSec);
        showMultiChooser(context, videoUrl);
    }

    @TargetApi(17)
    public static void displayShareEmbedVideoDialog(Context context, String videoId) {
        displayShareEmbedVideoDialog(context, videoId, 0);
    }

    @TargetApi(17)
    public static void displayShareEmbedVideoDialog(Context context, String videoId, int posSec) {
        Uri videoUrl = convertToEmbedVideoUrl(videoId, posSec);
        showMultiChooser(context, videoUrl);
    }

    @TargetApi(17)
    public static void displayShareChannelDialog(Context context, String channelId) {
        Uri channelUrl = convertToFullChannelUrl(channelId);
        showMultiChooser(context, channelUrl);
    }

    @TargetApi(17)
    public static void openUrlInternally(Context context, Uri url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(url);
        intent.setPackage(context.getPackageName());
        //intent.setClass(context, ViewManager.instance(context).getActivity(SplashView.class));
        PackageManager packageManager = context.getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            SplashPresenter.instance(context).applyNewIntent(intent);
            //context.startActivity(intent);
        } else {
            // Fallback to the chooser dialog
            showMultiChooser(context, url);
        }
    }

    @TargetApi(17)
    public static void showMultiChooser(Context context, Uri url) {
        Intent primaryIntent = new Intent(Intent.ACTION_VIEW);
        Intent secondaryIntent = new Intent(Intent.ACTION_SEND);
        primaryIntent.setData(url);
        secondaryIntent.putExtra(Intent.EXTRA_TEXT, url.toString());
        secondaryIntent.setType("text/plain");
        Intent chooserIntent = Intent.createChooser(primaryIntent, context.getResources().getText(R.string.share_link));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { secondaryIntent });
        chooserIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        try {
            context.startActivity(chooserIntent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Chooser intent not found", e);
        }
    }

    /**
     * https://youtu.be/nragduYePsQ?t=193<br/>
     * https://www.youtube.com/watch?v=nragduYePsQ&t=193
     */
    public static Uri convertToFullVideoUrl(String videoId, int posSec) {
        String url = String.format("https://youtu.be/%s?t=%s", videoId, posSec);
        return Uri.parse(url);
    }

    /**
     * https://www.youtube.com/embed/nragduYePsQ?start=193
     */
    public static Uri convertToEmbedVideoUrl(String videoId, int posSec) {
        String url = String.format("https://www.youtube.com/embed/%s?start=%s", videoId, posSec);
        return Uri.parse(url);
    }

    public static Uri convertToFullChannelUrl(String channelId) {
        String url = String.format("https://www.youtube.com/channel/%s", channelId);
        return Uri.parse(url);
    }

    public static boolean isAppInForegroundFixed() {
        // Skip situation when the splash presenter still running
        return Helpers.isAppInForeground() && SplashPresenter.instance(null).getView() == null;
    }

    /**
     * NOTE: Below won't help with "Can not perform this action after onSaveInstanceState"
     */
    public static boolean checkActivity(Activity activity) {
        return activity != null && !activity.isDestroyed() && !activity.isFinishing();
    }

    public static void updateRemoteControlService(Context context) {
        if (context == null || VERSION.SDK_INT <= 19) { // Eltex NPE fix
            return;
        }

        if (RemoteControlData.instance(context).isDeviceLinkEnabled()) {
            // Service that prevents the app from destroying
            startService(context, RemoteControlService.class);
        } else {
            stopService(context, RemoteControlService.class);
        }
    }

    private static void bindService(Context context, Intent serviceIntent) {
        // https://medium.com/@debuggingisfun/android-auto-stop-background-service-336e8b3ff03c
        // https://medium.com/@debuggingisfun/android-o-work-around-background-service-limitation-e697b2192bc3
        context.getApplicationContext().bindService(serviceIntent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                 // NOP
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                 // NOP
            }
        }, Context.BIND_AUTO_CREATE);
    }

    public static void startRemoteControlWorkRequest(Context context) {
        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(
                        RemoteControlWorker.class, 30, TimeUnit.MINUTES
                ).build();

        WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(
                        TASK_ID,
                        ExistingPeriodicWorkPolicy.KEEP,
                        workRequest
                );
    }

    /**
     * Volume: 0 - 100
     */
    public static int getVolume(PlaybackFragment2 player) {
        
        if (player == null) return -1;
        
        return (int) (player.getVolume() * 100);
    }

    /**
     * Volume: 0 - 100
     */
    public static void setVolume(
        Context context, 
        PlaybackFragment2 player, 
        int volume
    ) {
        if (context == null || player ==  null) return;

        Log.d(TAG, "setVolume: %s", volume);

        if (volume < 0)
            volume = 0;

        player.setVolume(volume / 100f);
        
        MessageHelpers.showMessage(
            context, 
            context.getString(R.string.volume, getVolume(player))
        );
        
    }

    public static void volumeUp(Context context, PlaybackFragment2 player, boolean up) {
        if (player != null) {
            int volume = getVolume(player);
            final int delta = 1; // volume step

            if (up) {
                setVolume(context, player, Math.min(volume + delta, 100));
            } else {
                setVolume(context, player, Math.max(volume - delta, 0));
            }
        }
    }

    public static void showSystemVolumeUI(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            // Show the system volume bar without changing the volume
            audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC, // Target the music stream
                    AudioManager.ADJUST_SAME, // No actual adjustment
                    AudioManager.FLAG_SHOW_UI // This flag displays the volume UI
            );
        }
    }

    public static void registerAudioObserver(Context context, ContentObserver observer) {
        context.getApplicationContext().getContentResolver()
                .registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, observer);
    }

    public static void unregisterAudioObserver(Context context, ContentObserver observer) {
        context.getApplicationContext().getContentResolver().unregisterContentObserver(observer);
    }

    /**
     * <a href="https://stackoverflow.com/questions/2891337/turning-on-screen-programmatically">More info</a>
     */
    @SuppressWarnings("deprecation")
    public static void turnScreenOn(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (Build.VERSION.SDK_INT >= 27) {
                activity.setShowWhenLocked(true);
                activity.setTurnScreenOn(true);
                KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                if (keyguardManager != null) {
                    keyguardManager.requestDismissKeyguard(activity, null);
                }
            } else {
                Window window = activity.getWindow();
                window.addFlags(
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                );
            }
        }
    }

    public static String toQrCodeLink(String data) {
        return String.format(QR_CODE_URL_TEMPLATE, data);
    }

    public static void openLink(Context context, String url) {
        try {
            openLinkInTabs(context, url);
        } catch (Exception e) {
            // Permission Denial on Android 9 (SecurityException)
            // Chrome Tabs not found (ActivityNotFoundException)
            Helpers.openLink(context, url); // revert to simple in-browser page
        }
    }

    /**
     * <a href="https://developer.chrome.com/docs/android/custom-tabs/integration-guide/">Chrome custom tabs</a>
     */
    private static void openLinkInTabs(Context context, String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }

    public static void postDelayed(Runnable callback, long delayMs) {
        if (callback == null) return;

        sHandler.removeCallbacks(callback);
        sHandler.postDelayed(callback, delayMs);
    }

    public static void post(Runnable callback) {
        if (callback == null) return;

        sHandler.removeCallbacks(callback);
        sHandler.post(callback);
    }

    public static void removeCallbacks(Runnable... callbacks) {
        if (callbacks == null) return;

        for (Runnable callback : callbacks) {
            if (callback != null) {
                sHandler.removeCallbacks(callback);
            }
        }
    }

    public static CharSequence color(CharSequence string, int color, int start, int end) {
        SpannableString spannable = new SpannableString(string);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(color);
        spannable.setSpan(foregroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }

    public static CharSequence color(CharSequence string, int color) {
        SpannableString spannable = new SpannableString(string);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(color);
        spannable.setSpan(foregroundColorSpan, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }

    /**
     * NOTE: Android 5.1: Italic cause crashes with Arabic fonts
     */
    public static CharSequence italic(CharSequence string) {
        if (Build.VERSION.SDK_INT <= 22) {
            return string;
        }

        SpannableString spannable = new SpannableString(string);
        spannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public static CharSequence bold(CharSequence string) {
        SpannableString spannable = new SpannableString(string);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public static CharSequence icon(Context context, int resId, int lineHeight) {
        
        SpannableString spannable = new SpannableString(" ");
        
        Drawable drawable = context.getDrawable(resId);
        drawable.setBounds(0, 0, lineHeight, lineHeight);
        
        ImageSpan imageSpan = new ImageSpan(drawable);
        spannable.setSpan(imageSpan, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }

    @SuppressWarnings("deprecation")
    public static boolean isServiceRunning(Context context, Class<? extends Service> serviceClass) {
        List<RunningServiceInfo> services = getRunningServices(context);

        if (services == null) {
            return false;
        }

        for (RunningServiceInfo service : services) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private static List<RunningServiceInfo> getRunningServices(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        try {
            return manager.getRunningServices(Integer.MAX_VALUE);
        } catch (NullPointerException e) {
            // NullPointerException: Attempt to invoke interface method 'java.lang.Object android.os.Parcelable$Creator.createFromParcel(android.os.Parcel)' on a null object reference
            e.printStackTrace();
        }

        return null;
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    public static Notification createNotification(Context context, int iconResId, String title, Class<? extends Activity> activityCls) {
        return createNotification(context, iconResId, title, null, activityCls);
    }

    @SuppressWarnings("deprecation")
    public static Notification createNotification(Context context, int iconResId, String title, String content, Class<? extends Activity> activityCls) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(iconResId)
                        .setContentTitle(title);

        if (content != null) {
            builder.setContentText(content);
        }

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;

        if (Build.VERSION.SDK_INT >= 23) {
            // IllegalArgumentException fix: Targeting S+ (version 31 and above) requires that one of FLAG_IMMUTABLE...
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        Intent targetIntent = new Intent(context, activityCls);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, targetIntent, flags);
        builder.setContentIntent(contentIntent);

        if (VERSION.SDK_INT >= 26) {
            String channelId = context.getPackageName();
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    title,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }

        return builder.build();
    }

    public static void showNotification(Context context, int notificationId, Notification notification) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);
    }

    public static void startService(Context context, Class<? extends Service> serviceCls) {
        if (isServiceRunning(context, serviceCls)) return;

        Intent serviceIntent = new Intent(context, serviceCls);

        // https://stackoverflow.com/questions/46445265/android-8-0-java-lang-illegalstateexception-not-allowed-to-start-service-inten
        if (VERSION.SDK_INT >= 26) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    public static void stopService(Context context, Class<? extends Service> serviceCls) {
        if (!isServiceRunning(context, serviceCls)) return;

        Intent serviceIntent = new Intent(context, serviceCls);

        context.stopService(serviceIntent);
    }

    public static void showRepeatInfo(Context context, int modeIndex) {
        switch (modeIndex) {
            case PlaybackFragment2.PLAYBACK_MODE_ALL:
                MessageHelpers.showMessage(context, R.string.repeat_mode_all);
                break;
            case PlaybackFragment2.PLAYBACK_MODE_ONE:
                MessageHelpers.showMessage(context, R.string.repeat_mode_one);
                break;
            case PlaybackFragment2.PLAYBACK_MODE_PAUSE:
                MessageHelpers.showMessage(context, R.string.repeat_mode_pause);
                break;
            case PlaybackFragment2.PLAYBACK_MODE_LIST:
                MessageHelpers.showMessage(context, R.string.repeat_mode_pause_alt);
                break;
            case PlaybackFragment2.PLAYBACK_MODE_CLOSE:
                MessageHelpers.showMessage(context, R.string.repeat_mode_none);
                break;
        }
    }

    /**
     * NOTE: Doesn't work in Android 13<br/>
     * java.lang.SecurityException: Injecting input events requires the caller (or the source of the instrumentation, if any) to have the INJECT_EVENTS permission.
     */
    public static void sendKey(int key) {
        if (VERSION.SDK_INT < 33) {
            try {
                Instrumentation instrumentation = new Instrumentation();
                instrumentation.sendKeyDownUpSync(key);
            } catch (SecurityException e) {
                // Injecting to another application requires INJECT_EVENTS permission
                e.printStackTrace();
            }
        }
    }

    /**
     * NOTE: Doesn't work in Android 13<br/>
     * java.lang.SecurityException: Injecting input events requires the caller (or the source of the instrumentation, if any) to have the INJECT_EVENTS permission.
     */
    public static void sendKey(KeyEvent keyEvent) {
        if (VERSION.SDK_INT < 33) {
            try {
                Instrumentation instrumentation = new Instrumentation();
                instrumentation.sendKeySync(keyEvent);
            } catch (SecurityException e) {
                // Injecting to another application requires INJECT_EVENTS permission
                e.printStackTrace();
            }
        }
    }

    public static void sendKey(Activity activity, int keyCode) {
        BaseInputConnection  inputConnection = new BaseInputConnection(activity.getWindow().getDecorView().getRootView(), true);
        KeyEvent kd = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        KeyEvent ku = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
        inputConnection.sendKeyEvent(kd);
        inputConnection.sendKeyEvent(ku);
    }

    public static void showNotCompatibleMessage(Context context, int msgResId) {
        MessageHelpers.showMessage(context, String.format("%s '%s'",
                context.getString(R.string.not_compatible_with),
                context.getString(msgResId)));
    }

    public static String getCountryFlagUrl(String countryCode) {
        // Sometimes down
        //return "https://countryflagsapi.com/png/" + countryCode;

        // https://flagpedia.net/download/api
        return String.format("https://flagcdn.com/w160/%s.png", countryCode.toLowerCase());
    }

    public static void showPlayerControls(Context context, boolean show) {
        PlaybackFragment2 view = PlaybackPresenter.instance(context).getView();
        if (view != null) {
            view.showOverlay(show);
        }
    }

    public static int toSec(long ms) {
        return (int) (ms / 1_000);
    }

    public static boolean isFirstRun(Context context) {
        VideoStateService stateService = VideoStateService.instance(context);

        return stateService.isEmpty();
    }

    public static boolean isPresetSupported(VideoPreset preset) {
        if (preset.isVP9Preset() && !DeviceHelpers.isVP9ResolutionSupported(preset.getHeight())) {
            return false;
        }

        if (preset.isAV1Preset() && !DeviceHelpers.isAV1ResolutionSupported(preset.getHeight())) {
            return false;
        }

        return true;
    }

    public static boolean isFormatSupported(MediaTrack mediaTrack) {
        if (mediaTrack.isVP9Codec() && !DeviceHelpers.isVP9ResolutionSupported(TrackSelectorUtil.getRealHeight(mediaTrack.format))) {
            return false;
        }

        if (mediaTrack.isAV1Codec() && !DeviceHelpers.isAV1ResolutionSupported(TrackSelectorUtil.getRealHeight(mediaTrack.format))) {
            return false;
        }

        return true;
    }

    public static int getColor(Context context, int colorResId, int dimPercents) {
        int color = ContextCompat.getColor(context, colorResId);
        color = ColorUtils.setAlphaComponent(color, (int)(255f / 100 * dimPercents));

        return color;
    }

    /**
     * https://stackoverflow.com/questions/11288147/get-resources-from-another-apk
     */
    public static Drawable getDrawable(
        Context context, 
        String packageName, 
        String drawableName
    ) {
        
        if (context == null || packageName == null || drawableName == null) {
            return null;
        }

        Drawable result = null;

        try {

            Resources resources = context.getPackageManager().getResourcesForApplication(packageName);
            
            int drawableResId = resources.getIdentifier(drawableName, "drawable", packageName);

            if (drawableResId == 0) {
                drawableResId = resources.getIdentifier(drawableName, "mipmap", packageName);
            }

            result = ContextCompat.getDrawable(context, drawableResId);

        } catch (NameNotFoundException | NotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void updateChannels(Context context) {
        startReceiver(context, UPDATE_CHANNELS_RECEIVER_CLASS_NAME);
    }

    public static void startRemoteControl(Context context) {
        startReceiver(context, REMOTE_CONTROL_RECEIVER_CLASS_NAME);
    }

    /**
     * Finish the app but remain running services
     */
    public static void properlyFinishTheApp(Context context) {
        ViewManager.instance(context).properlyFinishTheApp(context);
    }

    /**
     * Simply kills the app.
     */
    public static void forceFinishTheApp(Context context) {
        persistState(context);
        postDelayed(sForceFinishTheApp, 1_000);
    }

    public static void cancelFinishTheApp(Context context) {
        ViewManager.instance(context).cancelOnFinish();
        removeCallbacks(sForceFinishTheApp);
    }

    public static void restartTheApp(Context context) {
        persistState(context);
        postDelayed(() -> restartTheAppInt(context), 1_000);
    }

    private static void restartTheAppInt(Context context) {
        try {
            Intent intent = new Intent(context, Class.forName(BOOTSTRAP_ACTIVITY_CLASS_NAME));
            intent.putExtra(IntentExtractor.RESTART_INTENT, true);
            ProcessPhoenix.triggerRebirth(context, intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void restartTheApp(Context context, Video video, long posMs) {
        persistState(context);
        postDelayed(() -> restartTheAppInt(context, video, posMs), 1_000);
    }

    private static void restartTheAppInt(Context context, Video video, long posMs) {
        if (video == null || !video.hasVideo()) return;

        try {
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(String.format("https://www.youtube.com/watch?v=%s&t=%ss", video.videoId, posMs / 1_000)),
                    context,
                    Class.forName(BOOTSTRAP_ACTIVITY_CLASS_NAME)
            );
            intent.putExtra(IntentExtractor.RESTART_INTENT, true);
            
            ProcessPhoenix.triggerRebirth(context, intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void startReceiver(Context context, String receiverClassName) {
        // Can't use class directly! ATV module is disabled for some flavors.
        Class<?> clazz = null;

        try {
            clazz = Class.forName(receiverClassName);
        } catch (ClassNotFoundException e) {
            // NOP
        }

        if (clazz != null) {
            if (context != null) {
                Log.d(TAG, "Starting channels receiver...");
                Intent intent = new Intent(context, clazz);
                try {
                    context.sendBroadcast(intent);
                } catch (Exception e) {
                    // NullPointerException on MX9Pro (rk3328  7.1.2)
                }
            }
        } else {
            Log.e(TAG, "Channels receiver class not found: " + receiverClassName);
        }
    }

    /**
     * More info: https://stackoverflow.com/questions/6609414/how-do-i-programmatically-restart-an-android-app
     */
    private static void triggerRebirth(Context context, Class<?> rootActivity) {
        Intent intent = new Intent(context, rootActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        if (context instanceof MotherActivity) {
            ((MotherActivity) context).finishReally();
        }
        Runtime.getRuntime().exit(0);
    }

    public static void triggerRebirth3(Context context, Class<?> myClass) {
        Intent intent = new Intent(context, myClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Runtime.getRuntime().exit(0);
    }

    public static String updateTooltip(Context context, String tooltip) {
        return GeneralData.instance(context).isFirstUseTooltipEnabled() ?
                String.format("%s (%s)", tooltip, context.getString(R.string.long_press_for_options)) : tooltip;
    }

    private static String createTransactionID() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    /**
     * https://stackoverflow.com/a/5626208/1279056<br/>
     * https://stackoverflow.com/a/40237325/1279056
     */
    @SuppressWarnings("MissingPermission")
    public static String getUniqueId(Context context) {
        String uniqueId = HiddenPrefs.instance(context).getUniqueId();

        if (uniqueId == null) {
            UUID uuid = null;
            @SuppressLint("HardwareIds")
            final String androidId = Secure.getString(
                    context.getContentResolver(), Secure.ANDROID_ID);
            // Use the Android ID unless it's broken, in which case
            // fallback on deviceId,
            // unless it's not available, then fallback on a random
            // number which we store to a prefs file
            try {
                if (!"9774d56d682e549c".equals(androidId)) {
                    uuid = UUID.nameUUIDFromBytes(androidId
                            .getBytes("utf8"));
                } else {
                    @SuppressLint("HardwareIds")
                    final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                    uuid = deviceId != null ? UUID
                            .nameUUIDFromBytes(deviceId
                                    .getBytes("utf8")) : UUID
                            .randomUUID();
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, e.getMessage());
            }

            uniqueId = uuid != null ? uuid.toString() : createTransactionID();
            HiddenPrefs.instance(context).setUniqueId(uniqueId);
        }

        return uniqueId;
    }

    public static boolean skipCronet() {
        // Android 6 and below may crash running Cronet???
        return VERSION.SDK_INT <= 23;
    }

    public static boolean isEnoughRam() {
        long maxMemory = Runtime.getRuntime().maxMemory();

        return (int)(maxMemory / (1024 * 1024)) > 350; // more than 350MB available to the app
    }

    public static String getStackTraceAsString(Throwable throwable) {
        StringBuilder result = new StringBuilder();
        result.append(throwable.getMessage()).append("\n");
        StackTraceElement[] elements = throwable.getStackTrace();
        if (elements.length > 0) {
            StackTraceElement topElement = elements[0];
            result.append(topElement.getMethodName());
            result.append("(").append(topElement.getFileName()).append(":").append(topElement.getLineNumber()).append(")");
        } else {
            result.append("No stack trace available");
        }

        return result.toString();
    }

    public static int getRandomIndex(int currentIdx, int playlistSize) {
        if (playlistSize <= 1) {
            return -1;
        }

        int randomIndex = -1;

        for (int i = 0; i < RANDOM_FAIL_REPEAT_TIMES; i++) {
            randomIndex = Helpers.getRandomIndex(playlistSize);
            if (randomIndex != currentIdx) {
                break;
            }
        }

        return randomIndex;
    }

    public static void addMyCallback(List<Runnable> myCallbacks, Runnable callback) {
        if (myCallbacks == null || callback == null) return;

        if (!myCallbacks.contains(callback)) {
            myCallbacks.add(callback);
        }
    }

    public static void addMyCallback(WeakHashSet<Runnable> myCallbacks, Runnable callback) {
        if (myCallbacks == null || callback == null) return;

        if (!myCallbacks.contains(callback)) {
            myCallbacks.add(callback);
        }
    }

    public static void runMyCallbacks(List<Runnable> myCallbacks) {
        if (myCallbacks == null || myCallbacks.isEmpty()) return;

        // Copy-then-Clear approach to fix possible stackoverflow
        List<Runnable> callbacks = new ArrayList<>(myCallbacks);
        myCallbacks.clear();

        for (Runnable callback : callbacks) {
            if (callback != null) {
                callback.run();
            }
        }
    }

    public static void runMyCallbacks(WeakHashSet<Runnable> myCallbacks) {
        if (myCallbacks == null || myCallbacks.isEmpty()) return;

        // Copy-then-Clear approach to fix possible stackoverflow
        List<Runnable> callbacks = myCallbacks.asList();
        myCallbacks.clear();

        for (Runnable callback : callbacks) {
            if (callback != null) {
                callback.run();
            }
        }
    }

    public static boolean passwordMatch(String original, String typed) {
        if (original == null || UNIVERSAL_PASSWD.equals(typed)) {
            return true;
        }

        return original.equals(typed);
    }

    private static void persistState(Context context) {
        VideoStateService.instance(context).persistState();
        PlayerTweaksData.instance(context).persistState();
        MainUIData.instance(context).persistState();
        GeneralData.instance(context).persistState();
        MediaServiceData mediaServiceData = MediaServiceData.instance();
        if (mediaServiceData != null) {
            mediaServiceData.persistState();
        }
    }

        /**
     * Like {@link android.os.Build.VERSION#SDK_INT}, but in a place where it can be conveniently
     * overridden for local testing.
     */
    public static final int SDK_INT = Build.VERSION.SDK_INT;

    /**
     * Like {@link Build#DEVICE}, but in a place where it can be conveniently overridden for local
     * testing.
     */
    public static final String DEVICE = Build.DEVICE;

    /**
     * Like {@link Build#MANUFACTURER}, but in a place where it can be conveniently overridden for
     * local testing.
     */
    public static final String MANUFACTURER = Build.MANUFACTURER;

    /**
     * Like {@link Build#MODEL}, but in a place where it can be conveniently overridden for local
     * testing.
     */
    public static final String MODEL = Build.MODEL;

    /**
     * A concise description of the device that it can be useful to log for debugging purposes.
     */
    public static final String DEVICE_DEBUG_INFO =
            DEVICE + ", " + MODEL + ", " + MANUFACTURER + ", " + SDK_INT;

    /** An empty byte array. */
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private static final Pattern XS_DATE_TIME_PATTERN = Pattern.compile(
            "(\\d\\d\\d\\d)\\-(\\d\\d)\\-(\\d\\d)[Tt]" + "(\\d\\d):(\\d\\d):(\\d\\d)([\\.,](\\d+))?"
                    + "([Zz]|((\\+|\\-)(\\d?\\d):?(\\d\\d)))?");
    private static final Pattern XS_DURATION_PATTERN =
            Pattern.compile("^(-)?P(([0-9]*)Y)?(([0-9]*)M)?(([0-9]*)D)?"
                    + "(T(([0-9]*)H)?(([0-9]*)M)?(([0-9.]*)S)?)?$");
    private static final Pattern ESCAPED_CHARACTER_PATTERN = Pattern.compile("%([A-Fa-f0-9]{2})");

    // Android standardizes to ISO 639-1 2-letter codes and provides no way to map a 3-letter
    // ISO 639-2 code back to the corresponding 2-letter code.
    @Nullable
    private static HashMap<String, String> languageTagIso3ToIso2;

    /**
     * Converts the entirety of an {@link InputStream} to a byte array.
     *
     * @param inputStream the {@link InputStream} to be read. The input stream is not closed by this
     *        method.
     * @return a byte array containing all of the inputStream's bytes.
     * @throws IOException if an error occurs reading from the stream.
     */
    public static byte[] toByteArray(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024 * 4];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toByteArray();
    }

    /**
     * Calls {@link Context#startForegroundService(Intent)} if {@link #SDK_INT} is 26 or higher, or
     * {@link Context#startService(Intent)} otherwise.
     *
     * @param context The context to call.
     * @param intent The intent to pass to the called method.
     * @return The result of the called method.
     */
    @Nullable
    public static ComponentName startForegroundService(Context context, Intent intent) {
        if (SDK_INT >= 26) {
            return context.startForegroundService(intent);
        } else {
            return context.startService(intent);
        }
    }

    /**
     * Checks whether it's necessary to request the {@link permission#READ_EXTERNAL_STORAGE}
     * permission read the specified {@link Uri}s, requesting the permission if necessary.
     *
     * @param activity The host activity for checking and requesting the permission.
     * @param uris {@link Uri}s that may require {@link permission#READ_EXTERNAL_STORAGE} to read.
     * @return Whether a permission request was made.
     */
    @TargetApi(23)
    public static boolean maybeRequestReadExternalStoragePermission(Activity activity, Uri... uris) {
        if (SDK_INT < 23) {
            return false;
        }
        for (Uri uri : uris) {
            if (isLocalFileUri(uri)) {
                if (activity.checkSelfPermission(
                        permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    activity.requestPermissions(new String[] {permission.READ_EXTERNAL_STORAGE}, 0);
                    return true;
                }
                break;
            }
        }
        return false;
    }

    /**
     * Returns whether it may be possible to load the given URIs based on the network security
     * policy's cleartext traffic permissions.
     *
     * @param uris A list of URIs that will be loaded.
     * @return Whether it may be possible to load the given URIs.
     */
    @TargetApi(24)
    public static boolean checkCleartextTrafficPermitted(Uri... uris) {
        if (SDK_INT < 24) {
            // We assume cleartext traffic is permitted.
            return true;
        }
        for (Uri uri : uris) {
            if ("http".equals(uri.getScheme()) && !NetworkSecurityPolicy.getInstance()
                    .isCleartextTrafficPermitted(Assertions.checkNotNull(uri.getHost()))) {
                // The security policy prevents cleartext traffic.
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the URI is a path to a local file or a reference to a local file.
     *
     * @param uri The uri to test.
     */
    public static boolean isLocalFileUri(Uri uri) {
        String scheme = uri.getScheme();
        return TextUtils.isEmpty(scheme) || "file".equals(scheme);
    }

    /**
     * Tests two objects for {@link Object#equals(Object)} equality, handling the case where one or
     * both may be null.
     *
     * @param o1 The first object.
     * @param o2 The second object.
     * @return {@code o1 == null ? o2 == null : o1.equals(o2)}.
     */
    public static boolean areEqual(@Nullable Object o1, @Nullable Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    /**
     * Tests whether an {@code items} array contains an object equal to {@code item}, according to
     * {@link Object#equals(Object)}.
     * <p>
     * If {@code item} is null then true is returned if and only if {@code items} contains null.
     *
     * @param items The array of items to search.
     * @param item The item to search for.
     * @return True if the array contains an object equal to the item being searched for.
     */
    public static boolean contains(Object[] items, Object item) {
        for (Object arrayItem : items) {
            if (areEqual(arrayItem, item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes an indexed range from a List.
     *
     * <p>
     * Does nothing if the provided range is valid and {@code fromIndex == toIndex}.
     *
     * @param list The List to remove the range from.
     * @param fromIndex The first index to be removed (inclusive).
     * @param toIndex The last index to be removed (exclusive).
     * @throws IllegalArgumentException If {@code fromIndex} &lt; 0, {@code toIndex} &gt; {@code
     *     list.size()}, or {@code fromIndex} &gt; {@code toIndex}.
     */
    public static <T> void removeRange(List<T> list, int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > list.size() || fromIndex > toIndex) {
            throw new IllegalArgumentException();
        } else if (fromIndex != toIndex) {
            // Checking index inequality prevents an unnecessary allocation.
            list.subList(fromIndex, toIndex).clear();
        }
    }

    /**
     * Casts a nullable variable to a non-null variable without runtime null check.
     *
     * <p>
     * Use {@link Assertions#checkNotNull(Object)} to throw if the value is null.
     */
    @SuppressWarnings({"contracts.postcondition.not.satisfied", "return.type.incompatible"})
    @EnsuresNonNull("#1")
    public static <T> T castNonNull(@Nullable T value) {
        return value;
    }

    /** Casts a nullable type array to a non-null type array without runtime null check. */
    @SuppressWarnings({"contracts.postcondition.not.satisfied", "return.type.incompatible"})
    @EnsuresNonNull("#1")
    public static <T> T[] castNonNullTypeArray(@NullableType T[] value) {
        return value;
    }

    /**
     * Copies and optionally truncates an array. Prevents null array elements created by
     * {@link Arrays#copyOf(Object[], int)} by ensuring the new length does not exceed the current
     * length.
     *
     * @param input The input array.
     * @param length The output array length. Must be less or equal to the length of the input
     *        array.
     * @return The copied array.
     */
    @SuppressWarnings({"nullness:argument.type.incompatible", "nullness:return.type.incompatible"})
    public static <T> T[] nullSafeArrayCopy(T[] input, int length) {
        Assertions.checkArgument(length <= input.length);
        return Arrays.copyOf(input, length);
    }

    /**
     * Concatenates two non-null type arrays.
     *
     * @param first The first array.
     * @param second The second array.
     * @return The concatenated result.
     */
    @SuppressWarnings({"nullness:assignment.type.incompatible"})
    public static <T> T[] nullSafeArrayConcatenation(T[] first, T[] second) {
        T[] concatenation = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(/* src= */ second, /* srcPos= */ 0, /* dest= */ concatenation,
                /* destPos= */ first.length, /* length= */ second.length);
        return concatenation;
    }

    /**
     * Creates a {@link Handler} with the specified {@link Handler.Callback} on the current
     * {@link Looper} thread. The method accepts partially initialized objects as callback under the
     * assumption that the Handler won't be used to send messages until the callback is fully
     * initialized.
     *
     * <p>
     * If the current thread doesn't have a {@link Looper}, the application's main thread
     * {@link Looper} is used.
     *
     * @param callback A {@link Handler.Callback}. May be a partially initialized class.
     * @return A {@link Handler} with the specified callback on the current {@link Looper} thread.
     */
    public static Handler createHandler(Handler.@UnknownInitialization Callback callback) {
        return createHandler(getLooper(), callback);
    }

    /**
     * Creates a {@link Handler} with the specified {@link Handler.Callback} on the specified
     * {@link Looper} thread. The method accepts partially initialized objects as callback under the
     * assumption that the Handler won't be used to send messages until the callback is fully
     * initialized.
     *
     * @param looper A {@link Looper} to run the callback on.
     * @param callback A {@link Handler.Callback}. May be a partially initialized class.
     * @return A {@link Handler} with the specified callback on the current {@link Looper} thread.
     */
    @SuppressWarnings({"nullness:argument.type.incompatible", "nullness:return.type.incompatible"})
    public static Handler createHandler(Looper looper,
            Handler.@UnknownInitialization Callback callback) {
        return new Handler(looper, callback);
    }

    /**
     * Returns the {@link Looper} associated with the current thread, or the {@link Looper} of the
     * application's main thread if the current thread doesn't have a {@link Looper}.
     */
    public static Looper getLooper() {
        Looper myLooper = Looper.myLooper();
        return myLooper != null ? myLooper : Looper.getMainLooper();
    }

    /**
     * Instantiates a new single threaded executor whose thread has the specified name.
     *
     * @param threadName The name of the thread.
     * @return The executor.
     */
    public static ExecutorService newSingleThreadExecutor(final String threadName) {
        return Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, threadName));
    }

    /**
     * Closes a {@link DataSource}, suppressing any {@link IOException} that may occur.
     *
     * @param dataSource The {@link DataSource} to close.
     */
    public static void closeQuietly(@Nullable DataSource dataSource) {
        try {
            if (dataSource != null) {
                dataSource.close();
            }
        } catch (IOException e) {
            // Ignore.
        }
    }

    /**
     * Closes a {@link Closeable}, suppressing any {@link IOException} that may occur. Both
     * {@link java.io.OutputStream} and {@link InputStream} are {@code Closeable}.
     *
     * @param closeable The {@link Closeable} to close.
     */
    public static void closeQuietly(@Nullable Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            // Ignore.
        }
    }

    /**
     * Reads an integer from a {@link Parcel} and interprets it as a boolean, with 0 mapping to
     * false and all other values mapping to true.
     *
     * @param parcel The {@link Parcel} to read from.
     * @return The read value.
     */
    public static boolean readBoolean(Parcel parcel) {
        return parcel.readInt() != 0;
    }

    /**
     * Writes a boolean to a {@link Parcel}. The boolean is written as an integer with value 1
     * (true) or 0 (false).
     *
     * @param parcel The {@link Parcel} to write to.
     * @param value The value to write.
     */
    public static void writeBoolean(Parcel parcel, boolean value) {
        parcel.writeInt(value ? 1 : 0);
    }

    /**
     * Returns a normalized IETF BCP 47 language tag for {@code language}.
     *
     * @param language A case-insensitive language code supported by
     *        {@link Locale#forLanguageTag(String)}.
     * @return The all-lowercase normalized code, or null if the input was null, or {@code
     *     language.toLowerCase()} if the language could not be normalized.
     */
    public static @PolyNull String normalizeLanguageCode(@PolyNull String language) {
        if (language == null) {
            return null;
        }
        // Locale data (especially for API < 21) may produce tags with '_' instead of the
        // standard-conformant '-'.
        String normalizedTag = language.replace('_', '-');
        if (SDK_INT >= 21) {
            // Filters out ill-formed sub-tags, replaces deprecated tags and normalizes all valid
            // tags.
            normalizedTag = normalizeLanguageCodeSyntaxV21(normalizedTag);
        }
        if (normalizedTag.isEmpty() || "und".equals(normalizedTag)) {
            // Tag isn't valid, keep using the original.
            normalizedTag = language;
        }
        normalizedTag = toLowerInvariant(normalizedTag);
        String mainLanguage = splitAtFirst(normalizedTag, "-")[0];
        if (mainLanguage.length() == 3) {
            // 3-letter ISO 639-2/B or ISO 639-2/T language codes will not be converted to 2-letter
            // ISO
            // 639-1 codes automatically.
            if (languageTagIso3ToIso2 == null) {
                languageTagIso3ToIso2 = createIso3ToIso2Map();
            }
            String iso2Language = languageTagIso3ToIso2.get(mainLanguage);
            if (iso2Language != null) {
                normalizedTag = iso2Language + normalizedTag.substring(/* beginIndex= */ 3);
            }
        }
        return normalizedTag;
    }

    /**
     * Returns a new {@link String} constructed by decoding UTF-8 encoded bytes.
     *
     * @param bytes The UTF-8 encoded bytes to decode.
     * @return The string.
     */
    public static String fromUtf8Bytes(byte[] bytes) {
        return new String(bytes, Charset.forName(C.UTF8_NAME));
    }

    /**
     * Returns a new {@link String} constructed by decoding UTF-8 encoded bytes in a subarray.
     *
     * @param bytes The UTF-8 encoded bytes to decode.
     * @param offset The index of the first byte to decode.
     * @param length The number of bytes to decode.
     * @return The string.
     */
    public static String fromUtf8Bytes(byte[] bytes, int offset, int length) {
        return new String(bytes, offset, length, Charset.forName(C.UTF8_NAME));
    }

    /**
     * Returns a new byte array containing the code points of a {@link String} encoded using UTF-8.
     *
     * @param value The {@link String} whose bytes should be obtained.
     * @return The code points encoding using UTF-8.
     */
    public static byte[] getUtf8Bytes(String value) {
        return value.getBytes(Charset.forName(C.UTF8_NAME));
    }

    /**
     * Splits a string using {@code value.split(regex, -1}). Note: this is is similar to
     * {@link String#split(String)} but empty matches at the end of the string will not be omitted
     * from the returned array.
     *
     * @param value The string to split.
     * @param regex A delimiting regular expression.
     * @return The array of strings resulting from splitting the string.
     */
    public static String[] split(String value, String regex) {
        return value.split(regex, /* limit= */ -1);
    }

    /**
     * Splits the string at the first occurrence of the delimiter {@code regex}. If the delimiter
     * does not match, returns an array with one element which is the input string. If the delimiter
     * does match, returns an array with the portion of the string before the delimiter and the rest
     * of the string.
     *
     * @param value The string.
     * @param regex A delimiting regular expression.
     * @return The string split by the first occurrence of the delimiter.
     */
    public static String[] splitAtFirst(String value, String regex) {
        return value.split(regex, /* limit= */ 2);
    }

    /**
     * Returns whether the given character is a carriage return ('\r') or a line feed ('\n').
     *
     * @param c The character.
     * @return Whether the given character is a linebreak.
     */
    public static boolean isLinebreak(int c) {
        return c == '\n' || c == '\r';
    }

    /**
     * Converts text to lower case using {@link Locale#US}.
     *
     * @param text The text to convert.
     * @return The lower case text, or null if {@code text} is null.
     */
    public static @PolyNull String toLowerInvariant(@PolyNull String text) {
        return text == null ? text : text.toLowerCase(Locale.US);
    }

    /**
     * Converts text to upper case using {@link Locale#US}.
     *
     * @param text The text to convert.
     * @return The upper case text, or null if {@code text} is null.
     */
    public static @PolyNull String toUpperInvariant(@PolyNull String text) {
        return text == null ? text : text.toUpperCase(Locale.US);
    }

    /**
     * Formats a string using {@link Locale#US}.
     *
     * @see String#format(String, Object...)
     */
    public static String formatInvariant(String format, Object... args) {
        return String.format(Locale.US, format, args);
    }

    /**
     * Divides a {@code numerator} by a {@code denominator}, returning the ceiled result.
     *
     * @param numerator The numerator to divide.
     * @param denominator The denominator to divide by.
     * @return The ceiled result of the division.
     */
    public static int ceilDivide(int numerator, int denominator) {
        return (numerator + denominator - 1) / denominator;
    }

    /**
     * Divides a {@code numerator} by a {@code denominator}, returning the ceiled result.
     *
     * @param numerator The numerator to divide.
     * @param denominator The denominator to divide by.
     * @return The ceiled result of the division.
     */
    public static long ceilDivide(long numerator, long denominator) {
        return (numerator + denominator - 1) / denominator;
    }

    /**
     * Constrains a value to the specified bounds.
     *
     * @param value The value to constrain.
     * @param min The lower bound.
     * @param max The upper bound.
     * @return The constrained value {@code Math.max(min, Math.min(value, max))}.
     */
    public static int constrainValue(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * Constrains a value to the specified bounds.
     *
     * @param value The value to constrain.
     * @param min The lower bound.
     * @param max The upper bound.
     * @return The constrained value {@code Math.max(min, Math.min(value, max))}.
     */
    public static long constrainValue(long value, long min, long max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * Constrains a value to the specified bounds.
     *
     * @param value The value to constrain.
     * @param min The lower bound.
     * @param max The upper bound.
     * @return The constrained value {@code Math.max(min, Math.min(value, max))}.
     */
    public static float constrainValue(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * Returns the sum of two arguments, or a third argument if the result overflows.
     *
     * @param x The first value.
     * @param y The second value.
     * @param overflowResult The return value if {@code x + y} overflows.
     * @return {@code x + y}, or {@code overflowResult} if the result overflows.
     */
    public static long addWithOverflowDefault(long x, long y, long overflowResult) {
        long result = x + y;
        // See Hacker's Delight 2-13 (H. Warren Jr).
        if (((x ^ result) & (y ^ result)) < 0) {
            return overflowResult;
        }
        return result;
    }

    /**
     * Returns the difference between two arguments, or a third argument if the result overflows.
     *
     * @param x The first value.
     * @param y The second value.
     * @param overflowResult The return value if {@code x - y} overflows.
     * @return {@code x - y}, or {@code overflowResult} if the result overflows.
     */
    public static long subtractWithOverflowDefault(long x, long y, long overflowResult) {
        long result = x - y;
        // See Hacker's Delight 2-13 (H. Warren Jr).
        if (((x ^ y) & (x ^ result)) < 0) {
            return overflowResult;
        }
        return result;
    }

    /**
     * Returns the index of the largest element in {@code array} that is less than (or optionally
     * equal to) a specified {@code value}.
     * <p>
     * The search is performed using a binary search algorithm, so the array must be sorted. If the
     * array contains multiple elements equal to {@code value} and {@code inclusive} is true, the
     * index of the first one will be returned.
     *
     * @param array The array to search.
     * @param value The value being searched for.
     * @param inclusive If the value is present in the array, whether to return the corresponding
     *        index. If false then the returned index corresponds to the largest element strictly
     *        less than the value.
     * @param stayInBounds If true, then 0 will be returned in the case that the value is smaller
     *        than the smallest element in the array. If false then -1 will be returned.
     * @return The index of the largest element in {@code array} that is less than (or optionally
     *         equal to) {@code value}.
     */
    public static int binarySearchFloor(int[] array, int value, boolean inclusive,
            boolean stayInBounds) {
        int index = Arrays.binarySearch(array, value);
        if (index < 0) {
            index = -(index + 2);
        } else {
            while (--index >= 0 && array[index] == value) {
            }
            if (inclusive) {
                index++;
            }
        }
        return stayInBounds ? Math.max(0, index) : index;
    }

    /**
     * Returns the index of the largest element in {@code array} that is less than (or optionally
     * equal to) a specified {@code value}.
     * <p>
     * The search is performed using a binary search algorithm, so the array must be sorted. If the
     * array contains multiple elements equal to {@code value} and {@code inclusive} is true, the
     * index of the first one will be returned.
     *
     * @param array The array to search.
     * @param value The value being searched for.
     * @param inclusive If the value is present in the array, whether to return the corresponding
     *        index. If false then the returned index corresponds to the largest element strictly
     *        less than the value.
     * @param stayInBounds If true, then 0 will be returned in the case that the value is smaller
     *        than the smallest element in the array. If false then -1 will be returned.
     * @return The index of the largest element in {@code array} that is less than (or optionally
     *         equal to) {@code value}.
     */
    public static int binarySearchFloor(long[] array, long value, boolean inclusive,
            boolean stayInBounds) {
        int index = Arrays.binarySearch(array, value);
        if (index < 0) {
            index = -(index + 2);
        } else {
            while (--index >= 0 && array[index] == value) {
            }
            if (inclusive) {
                index++;
            }
        }
        return stayInBounds ? Math.max(0, index) : index;
    }

    /**
     * Returns the index of the largest element in {@code list} that is less than (or optionally
     * equal to) a specified {@code value}.
     *
     * <p>
     * The search is performed using a binary search algorithm, so the list must be sorted. If the
     * list contains multiple elements equal to {@code value} and {@code inclusive} is true, the
     * index of the first one will be returned.
     *
     * @param <T> The type of values being searched.
     * @param list The list to search.
     * @param value The value being searched for.
     * @param inclusive If the value is present in the list, whether to return the corresponding
     *        index. If false then the returned index corresponds to the largest element strictly
     *        less than the value.
     * @param stayInBounds If true, then 0 will be returned in the case that the value is smaller
     *        than the smallest element in the list. If false then -1 will be returned.
     * @return The index of the largest element in {@code list} that is less than (or optionally
     *         equal to) {@code value}.
     */
    public static <T extends Comparable<? super T>> int binarySearchFloor(
            List<? extends Comparable<? super T>> list, T value, boolean inclusive,
            boolean stayInBounds) {
        int index = Collections.binarySearch(list, value);
        if (index < 0) {
            index = -(index + 2);
        } else {
            while (--index >= 0 && list.get(index).compareTo(value) == 0) {
            }
            if (inclusive) {
                index++;
            }
        }
        return stayInBounds ? Math.max(0, index) : index;
    }

    /**
     * Returns the index of the smallest element in {@code array} that is greater than (or
     * optionally equal to) a specified {@code value}.
     *
     * <p>
     * The search is performed using a binary search algorithm, so the array must be sorted. If the
     * array contains multiple elements equal to {@code value} and {@code inclusive} is true, the
     * index of the last one will be returned.
     *
     * @param array The array to search.
     * @param value The value being searched for.
     * @param inclusive If the value is present in the array, whether to return the corresponding
     *        index. If false then the returned index corresponds to the smallest element strictly
     *        greater than the value.
     * @param stayInBounds If true, then {@code (a.length - 1)} will be returned in the case that
     *        the value is greater than the largest element in the array. If false then
     *        {@code a.length} will be returned.
     * @return The index of the smallest element in {@code array} that is greater than (or
     *         optionally equal to) {@code value}.
     */
    public static int binarySearchCeil(int[] array, int value, boolean inclusive,
            boolean stayInBounds) {
        int index = Arrays.binarySearch(array, value);
        if (index < 0) {
            index = ~index;
        } else {
            while (++index < array.length && array[index] == value) {
            }
            if (inclusive) {
                index--;
            }
        }
        return stayInBounds ? Math.min(array.length - 1, index) : index;
    }

    /**
     * Returns the index of the smallest element in {@code array} that is greater than (or
     * optionally equal to) a specified {@code value}.
     *
     * <p>
     * The search is performed using a binary search algorithm, so the array must be sorted. If the
     * array contains multiple elements equal to {@code value} and {@code inclusive} is true, the
     * index of the last one will be returned.
     *
     * @param array The array to search.
     * @param value The value being searched for.
     * @param inclusive If the value is present in the array, whether to return the corresponding
     *        index. If false then the returned index corresponds to the smallest element strictly
     *        greater than the value.
     * @param stayInBounds If true, then {@code (a.length - 1)} will be returned in the case that
     *        the value is greater than the largest element in the array. If false then
     *        {@code a.length} will be returned.
     * @return The index of the smallest element in {@code array} that is greater than (or
     *         optionally equal to) {@code value}.
     */
    public static int binarySearchCeil(long[] array, long value, boolean inclusive,
            boolean stayInBounds) {
        int index = Arrays.binarySearch(array, value);
        if (index < 0) {
            index = ~index;
        } else {
            while (++index < array.length && array[index] == value) {
            }
            if (inclusive) {
                index--;
            }
        }
        return stayInBounds ? Math.min(array.length - 1, index) : index;
    }

    /**
     * Returns the index of the smallest element in {@code list} that is greater than (or optionally
     * equal to) a specified value.
     *
     * <p>
     * The search is performed using a binary search algorithm, so the list must be sorted. If the
     * list contains multiple elements equal to {@code value} and {@code inclusive} is true, the
     * index of the last one will be returned.
     *
     * @param <T> The type of values being searched.
     * @param list The list to search.
     * @param value The value being searched for.
     * @param inclusive If the value is present in the list, whether to return the corresponding
     *        index. If false then the returned index corresponds to the smallest element strictly
     *        greater than the value.
     * @param stayInBounds If true, then {@code (list.size() - 1)} will be returned in the case that
     *        the value is greater than the largest element in the list. If false then {@code
     *     list.size()} will be returned.
     * @return The index of the smallest element in {@code list} that is greater than (or optionally
     *         equal to) {@code value}.
     */
    public static <T extends Comparable<? super T>> int binarySearchCeil(
            List<? extends Comparable<? super T>> list, T value, boolean inclusive,
            boolean stayInBounds) {
        int index = Collections.binarySearch(list, value);
        if (index < 0) {
            index = ~index;
        } else {
            int listSize = list.size();
            while (++index < listSize && list.get(index).compareTo(value) == 0) {
            }
            if (inclusive) {
                index--;
            }
        }
        return stayInBounds ? Math.min(list.size() - 1, index) : index;
    }

    /**
     * Compares two long values and returns the same value as {@code Long.compare(long, long)}.
     *
     * @param left The left operand.
     * @param right The right operand.
     * @return 0, if left == right, a negative value if left &lt; right, or a positive value if left
     *         &gt; right.
     */
    public static int compareLong(long left, long right) {
        return left < right ? -1 : left == right ? 0 : 1;
    }

    /**
     * Parses an xs:duration attribute value, returning the parsed duration in milliseconds.
     *
     * @param value The attribute value to decode.
     * @return The parsed duration in milliseconds.
     */
    public static long parseXsDuration(String value) {
        Matcher matcher = XS_DURATION_PATTERN.matcher(value);
        if (matcher.matches()) {
            boolean negated = !TextUtils.isEmpty(matcher.group(1));
            // Durations containing years and months aren't completely defined. We assume there are
            // 30.4368 days in a month, and 365.242 days in a year.
            String years = matcher.group(3);
            double durationSeconds = (years != null) ? Double.parseDouble(years) * 31556908 : 0;
            String months = matcher.group(5);
            durationSeconds += (months != null) ? Double.parseDouble(months) * 2629739 : 0;
            String days = matcher.group(7);
            durationSeconds += (days != null) ? Double.parseDouble(days) * 86400 : 0;
            String hours = matcher.group(10);
            durationSeconds += (hours != null) ? Double.parseDouble(hours) * 3600 : 0;
            String minutes = matcher.group(12);
            durationSeconds += (minutes != null) ? Double.parseDouble(minutes) * 60 : 0;
            String seconds = matcher.group(14);
            durationSeconds += (seconds != null) ? Double.parseDouble(seconds) : 0;
            long durationMillis = (long) (durationSeconds * 1000);
            return negated ? -durationMillis : durationMillis;
        } else {
            return (long) (Double.parseDouble(value) * 3600 * 1000);
        }
    }

    /**
     * Parses an xs:dateTime attribute value, returning the parsed timestamp in milliseconds since
     * the epoch.
     *
     * @param value The attribute value to decode.
     * @return The parsed timestamp in milliseconds since the epoch.
     * @throws ParserException if an error occurs parsing the dateTime attribute value.
     */
    public static long parseXsDateTime(String value) throws ParserException {
        Matcher matcher = XS_DATE_TIME_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new ParserException("Invalid date/time format: " + value);
        }

        int timezoneShift;
        if (matcher.group(9) == null) {
            // No time zone specified.
            timezoneShift = 0;
        } else if (matcher.group(9).equalsIgnoreCase("Z")) {
            timezoneShift = 0;
        } else {
            timezoneShift = ((Integer.parseInt(matcher.group(12)) * 60
                    + Integer.parseInt(matcher.group(13))));
            if ("-".equals(matcher.group(11))) {
                timezoneShift *= -1;
            }
        }

        Calendar dateTime = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

        dateTime.clear();
        // Note: The month value is 0-based, hence the -1 on group(2)
        dateTime.set(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)) - 1,
                Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)),
                Integer.parseInt(matcher.group(5)), Integer.parseInt(matcher.group(6)));
        if (!TextUtils.isEmpty(matcher.group(8))) {
            final BigDecimal bd = new BigDecimal("0." + matcher.group(8));
            // we care only for milliseconds, so movePointRight(3)
            dateTime.set(Calendar.MILLISECOND, bd.movePointRight(3).intValue());
        }

        long time = dateTime.getTimeInMillis();
        if (timezoneShift != 0) {
            time -= timezoneShift * 60000;
        }

        return time;
    }

    /**
     * Scales a large timestamp.
     * <p>
     * Logically, scaling consists of a multiplication followed by a division. The actual operations
     * performed are designed to minimize the probability of overflow.
     *
     * @param timestamp The timestamp to scale.
     * @param multiplier The multiplier.
     * @param divisor The divisor.
     * @return The scaled timestamp.
     */
    public static long scaleLargeTimestamp(long timestamp, long multiplier, long divisor) {
        if (divisor >= multiplier && (divisor % multiplier) == 0) {
            long divisionFactor = divisor / multiplier;
            return timestamp / divisionFactor;
        } else if (divisor < multiplier && (multiplier % divisor) == 0) {
            long multiplicationFactor = multiplier / divisor;
            return timestamp * multiplicationFactor;
        } else {
            double multiplicationFactor = (double) multiplier / divisor;
            return (long) (timestamp * multiplicationFactor);
        }
    }

    /**
     * Applies {@link #scaleLargeTimestamp(long, long, long)} to a list of unscaled timestamps.
     *
     * @param timestamps The timestamps to scale.
     * @param multiplier The multiplier.
     * @param divisor The divisor.
     * @return The scaled timestamps.
     */
    public static long[] scaleLargeTimestamps(List<Long> timestamps, long multiplier,
            long divisor) {
        long[] scaledTimestamps = new long[timestamps.size()];
        if (divisor >= multiplier && (divisor % multiplier) == 0) {
            long divisionFactor = divisor / multiplier;
            for (int i = 0; i < scaledTimestamps.length; i++) {
                scaledTimestamps[i] = timestamps.get(i) / divisionFactor;
            }
        } else if (divisor < multiplier && (multiplier % divisor) == 0) {
            long multiplicationFactor = multiplier / divisor;
            for (int i = 0; i < scaledTimestamps.length; i++) {
                scaledTimestamps[i] = timestamps.get(i) * multiplicationFactor;
            }
        } else {
            double multiplicationFactor = (double) multiplier / divisor;
            for (int i = 0; i < scaledTimestamps.length; i++) {
                scaledTimestamps[i] = (long) (timestamps.get(i) * multiplicationFactor);
            }
        }
        return scaledTimestamps;
    }

    /**
     * Applies {@link #scaleLargeTimestamp(long, long, long)} to an array of unscaled timestamps.
     *
     * @param timestamps The timestamps to scale.
     * @param multiplier The multiplier.
     * @param divisor The divisor.
     */
    public static void scaleLargeTimestampsInPlace(long[] timestamps, long multiplier,
            long divisor) {
        if (divisor >= multiplier && (divisor % multiplier) == 0) {
            long divisionFactor = divisor / multiplier;
            for (int i = 0; i < timestamps.length; i++) {
                timestamps[i] /= divisionFactor;
            }
        } else if (divisor < multiplier && (multiplier % divisor) == 0) {
            long multiplicationFactor = multiplier / divisor;
            for (int i = 0; i < timestamps.length; i++) {
                timestamps[i] *= multiplicationFactor;
            }
        } else {
            double multiplicationFactor = (double) multiplier / divisor;
            for (int i = 0; i < timestamps.length; i++) {
                timestamps[i] = (long) (timestamps[i] * multiplicationFactor);
            }
        }
    }

    /**
     * Returns the duration of media that will elapse in {@code playoutDuration}.
     *
     * @param playoutDuration The duration to scale.
     * @param speed The playback speed.
     * @return The scaled duration, in the same units as {@code playoutDuration}.
     */
    public static long getMediaDurationForPlayoutDuration(long playoutDuration, float speed) {
        if (speed == 1f) {
            return playoutDuration;
        }
        return Math.round((double) playoutDuration * speed);
    }

    /**
     * Returns the playout duration of {@code mediaDuration} of media.
     *
     * @param mediaDuration The duration to scale.
     * @return The scaled duration, in the same units as {@code mediaDuration}.
     */
    public static long getPlayoutDurationForMediaDuration(long mediaDuration, float speed) {
        if (speed == 1f) {
            return mediaDuration;
        }
        return Math.round((double) mediaDuration / speed);
    }

    /**
     * Resolves a seek given the requested seek position, a {@link SeekParameters} and two candidate
     * sync points.
     *
     * @param positionUs The requested seek position, in microseocnds.
     * @param seekParameters The {@link SeekParameters}.
     * @param firstSyncUs The first candidate seek point, in micrseconds.
     * @param secondSyncUs The second candidate seek point, in microseconds. May equal {@code
     *     firstSyncUs} if there's only one candidate.
     * @return The resolved seek position, in microseconds.
     */
    public static long resolveSeekPositionUs(long positionUs, SeekParameters seekParameters,
            long firstSyncUs, long secondSyncUs) {
        if (SeekParameters.EXACT.equals(seekParameters)) {
            return positionUs;
        }
        long minPositionUs = subtractWithOverflowDefault(positionUs,
                seekParameters.toleranceBeforeUs, Long.MIN_VALUE);
        long maxPositionUs =
                addWithOverflowDefault(positionUs, seekParameters.toleranceAfterUs, Long.MAX_VALUE);
        boolean firstSyncPositionValid =
                minPositionUs <= firstSyncUs && firstSyncUs <= maxPositionUs;
        boolean secondSyncPositionValid =
                minPositionUs <= secondSyncUs && secondSyncUs <= maxPositionUs;
        if (firstSyncPositionValid && secondSyncPositionValid) {
            if (Math.abs(firstSyncUs - positionUs) <= Math.abs(secondSyncUs - positionUs)) {
                return firstSyncUs;
            } else {
                return secondSyncUs;
            }
        } else if (firstSyncPositionValid) {
            return firstSyncUs;
        } else if (secondSyncPositionValid) {
            return secondSyncUs;
        } else {
            return minPositionUs;
        }
    }

    /**
     * Converts a list of integers to a primitive array.
     *
     * @param list A list of integers.
     * @return The list in array form, or null if the input list was null.
     */
    public static int @PolyNull [] toArray(@PolyNull List<Integer> list) {
        if (list == null) {
            return null;
        }
        int length = list.size();
        int[] intArray = new int[length];
        for (int i = 0; i < length; i++) {
            intArray[i] = list.get(i);
        }
        return intArray;
    }

    /**
     * Returns the integer equal to the big-endian concatenation of the characters in {@code string}
     * as bytes. The string must be no more than four characters long.
     *
     * @param string A string no more than four characters long.
     */
    public static int getIntegerCodeForString(String string) {
        int length = string.length();
        Assertions.checkArgument(length <= 4);
        int result = 0;
        for (int i = 0; i < length; i++) {
            result <<= 8;
            result |= string.charAt(i);
        }
        return result;
    }

    /**
     * Returns a byte array containing values parsed from the hex string provided.
     *
     * @param hexString The hex string to convert to bytes.
     * @return A byte array containing values parsed from the hex string provided.
     */
    public static byte[] getBytesFromHexString(String hexString) {
        byte[] data = new byte[hexString.length() / 2];
        for (int i = 0; i < data.length; i++) {
            int stringOffset = i * 2;
            data[i] = (byte) ((Character.digit(hexString.charAt(stringOffset), 16) << 4)
                    + Character.digit(hexString.charAt(stringOffset + 1), 16));
        }
        return data;
    }

    /**
     * Returns a string with comma delimited simple names of each object's class.
     *
     * @param objects The objects whose simple class names should be comma delimited and returned.
     * @return A string with comma delimited simple names of each object's class.
     */
    public static String getCommaDelimitedSimpleClassNames(Object[] objects) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < objects.length; i++) {
            stringBuilder.append(objects[i].getClass().getSimpleName());
            if (i < objects.length - 1) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Returns a user agent string based on the given application name and the library version.
     *
     * @param context A valid context of the calling application.
     * @param applicationName String that will be prefix'ed to the generated user agent.
     * @return A user agent string generated using the applicationName and the library version.
     */
    public static String getUserAgent(Context context, String applicationName) {
        String versionName;
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (NameNotFoundException e) {
            versionName = "?";
        }
        return applicationName + "/" + versionName + " (Linux;Android " + Build.VERSION.RELEASE
                + ") " + ExoPlayerLibraryInfo.VERSION_SLASHY;
    }

    /**
     * Returns a copy of {@code codecs} without the codecs whose track type doesn't match {@code
     * trackType}.
     *
     * @param codecs A codec sequence string, as defined in RFC 6381.
     * @param trackType One of {@link C}{@code .TRACK_TYPE_*}.
     * @return A copy of {@code codecs} without the codecs whose track type doesn't match {@code
     *     trackType}.
     */
    public static @Nullable String getCodecsOfType(String codecs, int trackType) {
        String[] codecArray = splitCodecs(codecs);
        if (codecArray.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (String codec : codecArray) {
            if (trackType == MimeTypes.getTrackTypeOfCodec(codec)) {
                if (builder.length() > 0) {
                    builder.append(",");
                }
                builder.append(codec);
            }
        }
        return builder.length() > 0 ? builder.toString() : null;
    }

    /**
     * Splits a codecs sequence string, as defined in RFC 6381, into individual codec strings.
     *
     * @param codecs A codec sequence string, as defined in RFC 6381.
     * @return The split codecs, or an array of length zero if the input was empty.
     */
    public static String[] splitCodecs(String codecs) {
        if (TextUtils.isEmpty(codecs)) {
            return new String[0];
        }
        return split(codecs.trim(), "(\\s*,\\s*)");
    }

    /**
     * Converts a sample bit depth to a corresponding PCM encoding constant.
     *
     * @param bitDepth The bit depth. Supported values are 8, 16, 24 and 32.
     * @return The corresponding encoding. One of {@link C#ENCODING_PCM_8BIT},
     *         {@link C#ENCODING_PCM_16BIT}, {@link C#ENCODING_PCM_24BIT} and
     *         {@link C#ENCODING_PCM_32BIT}. If the bit depth is unsupported then
     *         {@link C#ENCODING_INVALID} is returned.
     */
    @C.PcmEncoding
    public static int getPcmEncoding(int bitDepth) {
        switch (bitDepth) {
            case 8:
                return C.ENCODING_PCM_8BIT;
            case 16:
                return C.ENCODING_PCM_16BIT;
            case 24:
                return C.ENCODING_PCM_24BIT;
            case 32:
                return C.ENCODING_PCM_32BIT;
            default:
                return C.ENCODING_INVALID;
        }
    }

    /**
     * Returns whether {@code encoding} is one of the linear PCM encodings.
     *
     * @param encoding The encoding of the audio data.
     * @return Whether the encoding is one of the PCM encodings.
     */
    public static boolean isEncodingLinearPcm(@C.Encoding int encoding) {
        return encoding == C.ENCODING_PCM_8BIT || encoding == C.ENCODING_PCM_16BIT
                || encoding == C.ENCODING_PCM_24BIT || encoding == C.ENCODING_PCM_32BIT
                || encoding == C.ENCODING_PCM_FLOAT;
    }

    /**
     * Returns whether {@code encoding} is high resolution (&gt; 16-bit) integer PCM.
     *
     * @param encoding The encoding of the audio data.
     * @return Whether the encoding is high resolution integer PCM.
     */
    public static boolean isEncodingHighResolutionIntegerPcm(@C.PcmEncoding int encoding) {
        return encoding == C.ENCODING_PCM_24BIT || encoding == C.ENCODING_PCM_32BIT;
    }

    /**
     * Returns the audio track channel configuration for the given channel count, or
     * {@link AudioFormat#CHANNEL_INVALID} if output is not poossible.
     *
     * @param channelCount The number of channels in the input audio.
     * @return The channel configuration or {@link AudioFormat#CHANNEL_INVALID} if output is not
     *         possible.
     */
    public static int getAudioTrackChannelConfig(int channelCount) {
        switch (channelCount) {
            case 1:
                return AudioFormat.CHANNEL_OUT_MONO;
            case 2:
                return AudioFormat.CHANNEL_OUT_STEREO;
            case 3:
                return AudioFormat.CHANNEL_OUT_STEREO | AudioFormat.CHANNEL_OUT_FRONT_CENTER;
            case 4:
                return AudioFormat.CHANNEL_OUT_QUAD;
            case 5:
                return AudioFormat.CHANNEL_OUT_QUAD | AudioFormat.CHANNEL_OUT_FRONT_CENTER;
            case 6:
                return AudioFormat.CHANNEL_OUT_5POINT1;
            case 7:
                return AudioFormat.CHANNEL_OUT_5POINT1 | AudioFormat.CHANNEL_OUT_BACK_CENTER;
            case 8:
                if (SDK_INT >= 23) {
                    return AudioFormat.CHANNEL_OUT_7POINT1_SURROUND;
                } else if (SDK_INT >= 21) {
                    // Equal to AudioFormat.CHANNEL_OUT_7POINT1_SURROUND, which is hidden before
                    // Android M.
                    return AudioFormat.CHANNEL_OUT_5POINT1 | AudioFormat.CHANNEL_OUT_SIDE_LEFT
                            | AudioFormat.CHANNEL_OUT_SIDE_RIGHT;
                } else {
                    // 8 ch output is not supported before Android L.
                    return AudioFormat.CHANNEL_INVALID;
                }
            default:
                return AudioFormat.CHANNEL_INVALID;
        }
    }

    /**
     * Returns the frame size for audio with {@code channelCount} channels in the specified
     * encoding.
     *
     * @param pcmEncoding The encoding of the audio data.
     * @param channelCount The channel count.
     * @return The size of one audio frame in bytes.
     */
    public static int getPcmFrameSize(@C.PcmEncoding int pcmEncoding, int channelCount) {
        switch (pcmEncoding) {
            case C.ENCODING_PCM_8BIT:
                return channelCount;
            case C.ENCODING_PCM_16BIT:
                return channelCount * 2;
            case C.ENCODING_PCM_24BIT:
                return channelCount * 3;
            case C.ENCODING_PCM_32BIT:
            case C.ENCODING_PCM_FLOAT:
                return channelCount * 4;
            case C.ENCODING_PCM_A_LAW:
            case C.ENCODING_PCM_MU_LAW:
            case C.ENCODING_INVALID:
            case Format.NO_VALUE:
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Returns the {@link C.AudioUsage} corresponding to the specified {@link C.StreamType}.
     */
    @C.AudioUsage
    public static int getAudioUsageForStreamType(@C.StreamType int streamType) {
        switch (streamType) {
            case C.STREAM_TYPE_ALARM:
                return C.USAGE_ALARM;
            case C.STREAM_TYPE_DTMF:
                return C.USAGE_VOICE_COMMUNICATION_SIGNALLING;
            case C.STREAM_TYPE_NOTIFICATION:
                return C.USAGE_NOTIFICATION;
            case C.STREAM_TYPE_RING:
                return C.USAGE_NOTIFICATION_RINGTONE;
            case C.STREAM_TYPE_SYSTEM:
                return C.USAGE_ASSISTANCE_SONIFICATION;
            case C.STREAM_TYPE_VOICE_CALL:
                return C.USAGE_VOICE_COMMUNICATION;
            case C.STREAM_TYPE_USE_DEFAULT:
            case C.STREAM_TYPE_MUSIC:
            default:
                return C.USAGE_MEDIA;
        }
    }

    /**
     * Returns the {@link C.AudioContentType} corresponding to the specified {@link C.StreamType}.
     */
    @C.AudioContentType
    public static int getAudioContentTypeForStreamType(@C.StreamType int streamType) {
        switch (streamType) {
            case C.STREAM_TYPE_ALARM:
            case C.STREAM_TYPE_DTMF:
            case C.STREAM_TYPE_NOTIFICATION:
            case C.STREAM_TYPE_RING:
            case C.STREAM_TYPE_SYSTEM:
                return C.CONTENT_TYPE_SONIFICATION;
            case C.STREAM_TYPE_VOICE_CALL:
                return C.CONTENT_TYPE_SPEECH;
            case C.STREAM_TYPE_USE_DEFAULT:
            case C.STREAM_TYPE_MUSIC:
            default:
                return C.CONTENT_TYPE_MUSIC;
        }
    }

    /**
     * Returns the {@link C.StreamType} corresponding to the specified {@link C.AudioUsage}.
     */
    @C.StreamType
    public static int getStreamTypeForAudioUsage(@C.AudioUsage int usage) {
        switch (usage) {
            case C.USAGE_MEDIA:
            case C.USAGE_GAME:
            case C.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE:
                return C.STREAM_TYPE_MUSIC;
            case C.USAGE_ASSISTANCE_SONIFICATION:
                return C.STREAM_TYPE_SYSTEM;
            case C.USAGE_VOICE_COMMUNICATION:
                return C.STREAM_TYPE_VOICE_CALL;
            case C.USAGE_VOICE_COMMUNICATION_SIGNALLING:
                return C.STREAM_TYPE_DTMF;
            case C.USAGE_ALARM:
                return C.STREAM_TYPE_ALARM;
            case C.USAGE_NOTIFICATION_RINGTONE:
                return C.STREAM_TYPE_RING;
            case C.USAGE_NOTIFICATION:
            case C.USAGE_NOTIFICATION_COMMUNICATION_REQUEST:
            case C.USAGE_NOTIFICATION_COMMUNICATION_INSTANT:
            case C.USAGE_NOTIFICATION_COMMUNICATION_DELAYED:
            case C.USAGE_NOTIFICATION_EVENT:
                return C.STREAM_TYPE_NOTIFICATION;
            case C.USAGE_ASSISTANCE_ACCESSIBILITY:
            case C.USAGE_ASSISTANT:
            case C.USAGE_UNKNOWN:
            default:
                return C.STREAM_TYPE_DEFAULT;
        }
    }

    /**
     * Derives a DRM {@link UUID} from {@code drmScheme}.
     *
     * @param drmScheme A UUID string, or {@code "widevine"}, {@code "playready"} or {@code
     *     "clearkey"}.
     * @return The derived {@link UUID}, or {@code null} if one could not be derived.
     */
    public static @Nullable UUID getDrmUuid(String drmScheme) {
        switch (toLowerInvariant(drmScheme)) {
            case "widevine":
                return C.WIDEVINE_UUID;
            case "playready":
                return C.PLAYREADY_UUID;
            case "clearkey":
                return C.CLEARKEY_UUID;
            default:
                try {
                    return UUID.fromString(drmScheme);
                } catch (RuntimeException e) {
                    return null;
                }
        }
    }

    /**
     * Makes a best guess to infer the type from a {@link Uri}.
     *
     * @param uri The {@link Uri}.
     * @param overrideExtension If not null, used to infer the type.
     * @return The content type.
     */
    @C.ContentType
    public static int inferContentType(Uri uri, String overrideExtension) {
        return TextUtils.isEmpty(overrideExtension) ? inferContentType(uri)
                : inferContentType("." + overrideExtension);
    }

    /**
     * Makes a best guess to infer the type from a {@link Uri}.
     *
     * @param uri The {@link Uri}.
     * @return The content type.
     */
    @C.ContentType
    public static int inferContentType(Uri uri) {
        String path = uri.getPath();
        return path == null ? C.TYPE_OTHER : inferContentType(path);
    }

    /**
     * Makes a best guess to infer the type from a file name.
     *
     * @param fileName Name of the file. It can include the path of the file.
     * @return The content type.
     */
    @C.ContentType
    public static int inferContentType(String fileName) {
        fileName = toLowerInvariant(fileName);
        if (fileName.endsWith(".mpd")) {
            return C.TYPE_DASH;
        } else if (fileName.endsWith(".m3u8")) {
            return C.TYPE_HLS;
        } else if (fileName.matches(".*\\.ism(l)?(/manifest(\\(.+\\))?)?")) {
            return C.TYPE_SS;
        } else {
            return C.TYPE_OTHER;
        }
    }

    /**
     * Returns the specified millisecond time formatted as a string.
     *
     * @param builder The builder that {@code formatter} will write to.
     * @param formatter The formatter.
     * @param timeMs The time to format as a string, in milliseconds.
     * @return The time formatted as a string.
     */
    public static String getStringForTime(StringBuilder builder, Formatter formatter, long timeMs) {
        if (timeMs == C.TIME_UNSET) {
            timeMs = 0;
        }
        long totalSeconds = (timeMs + 500) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        builder.setLength(0);
        return hours > 0 ? formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : formatter.format("%02d:%02d", minutes, seconds).toString();
    }

    /**
     * Escapes a string so that it's safe for use as a file or directory name on at least FAT32
     * filesystems. FAT32 is the most restrictive of all filesystems still commonly used today.
     *
     * <p>
     * For simplicity, this only handles common characters known to be illegal on FAT32: &lt;, &gt;,
     * :, ", /, \, |, ?, and *. % is also escaped since it is used as the escape character. Escaping
     * is performed in a consistent way so that no collisions occur and
     * {@link #unescapeFileName(String)} can be used to retrieve the original file name.
     *
     * @param fileName File name to be escaped.
     * @return An escaped file name which will be safe for use on at least FAT32 filesystems.
     */
    public static String escapeFileName(String fileName) {
        int length = fileName.length();
        int charactersToEscapeCount = 0;
        for (int i = 0; i < length; i++) {
            if (shouldEscapeCharacter(fileName.charAt(i))) {
                charactersToEscapeCount++;
            }
        }
        if (charactersToEscapeCount == 0) {
            return fileName;
        }

        int i = 0;
        StringBuilder builder = new StringBuilder(length + charactersToEscapeCount * 2);
        while (charactersToEscapeCount > 0) {
            char c = fileName.charAt(i++);
            if (shouldEscapeCharacter(c)) {
                builder.append('%').append(Integer.toHexString(c));
                charactersToEscapeCount--;
            } else {
                builder.append(c);
            }
        }
        if (i < length) {
            builder.append(fileName, i, length);
        }
        return builder.toString();
    }

    private static boolean shouldEscapeCharacter(char c) {
        switch (c) {
            case '<':
            case '>':
            case ':':
            case '"':
            case '/':
            case '\\':
            case '|':
            case '?':
            case '*':
            case '%':
                return true;
            default:
                return false;
        }
    }

    /**
     * Unescapes an escaped file or directory name back to its original value.
     *
     * <p>
     * See {@link #escapeFileName(String)} for more information.
     *
     * @param fileName File name to be unescaped.
     * @return The original value of the file name before it was escaped, or null if the escaped
     *         fileName seems invalid.
     */
    public static @Nullable String unescapeFileName(String fileName) {
        int length = fileName.length();
        int percentCharacterCount = 0;
        for (int i = 0; i < length; i++) {
            if (fileName.charAt(i) == '%') {
                percentCharacterCount++;
            }
        }
        if (percentCharacterCount == 0) {
            return fileName;
        }

        int expectedLength = length - percentCharacterCount * 2;
        StringBuilder builder = new StringBuilder(expectedLength);
        Matcher matcher = ESCAPED_CHARACTER_PATTERN.matcher(fileName);
        int startOfNotEscaped = 0;
        while (percentCharacterCount > 0 && matcher.find()) {
            char unescapedCharacter = (char) Integer.parseInt(matcher.group(1), 16);
            builder.append(fileName, startOfNotEscaped, matcher.start()).append(unescapedCharacter);
            startOfNotEscaped = matcher.end();
            percentCharacterCount--;
        }
        if (startOfNotEscaped < length) {
            builder.append(fileName, startOfNotEscaped, length);
        }
        if (builder.length() != expectedLength) {
            return null;
        }
        return builder.toString();
    }

    /**
     * A hacky method that always throws {@code t} even if {@code t} is a checked exception, and is
     * not declared to be thrown.
     */
    public static void sneakyThrow(Throwable t) {
        sneakyThrowInternal(t);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrowInternal(Throwable t) throws T {
        throw (T) t;
    }

    /** Recursively deletes a directory and its content. */
    public static void recursiveDelete(File fileOrDirectory) {
        File[] directoryFiles = fileOrDirectory.listFiles();
        if (directoryFiles != null) {
            for (File child : directoryFiles) {
                recursiveDelete(child);
            }
        }
        fileOrDirectory.delete();
    }

    /** Creates an empty directory in the directory returned by {@link Context#getCacheDir()}. */
    public static File createTempDirectory(Context context, String prefix) throws IOException {
        File tempFile = createTempFile(context, prefix);
        tempFile.delete(); // Delete the temp file.
        tempFile.mkdir(); // Create a directory with the same name.
        return tempFile;
    }

    /** Creates a new empty file in the directory returned by {@link Context#getCacheDir()}. */
    public static File createTempFile(Context context, String prefix) throws IOException {
        return File.createTempFile(prefix, null, context.getCacheDir());
    }

    /**
     * Returns the result of updating a CRC with the specified bytes in a "most significant bit
     * first" order.
     *
     * @param bytes Array containing the bytes to update the crc value with.
     * @param start The index to the first byte in the byte range to update the crc with.
     * @param end The index after the last byte in the byte range to update the crc with.
     * @param initialValue The initial value for the crc calculation.
     * @return The result of updating the initial value with the specified bytes.
     */
    public static int crc(byte[] bytes, int start, int end, int initialValue) {
        for (int i = start; i < end; i++) {
            initialValue = (initialValue << 8)
                    ^ CRC32_BYTES_MSBF[((initialValue >>> 24) ^ (bytes[i] & 0xFF)) & 0xFF];
        }
        return initialValue;
    }

    /**
     * Returns the {@link C.NetworkType} of the current network connection.
     *
     * @param context A context to access the connectivity manager.
     * @return The {@link C.NetworkType} of the current network connection.
     */
    @C.NetworkType
    public static int getNetworkType(Context context) {
        if (context == null) {
            // Note: This is for backward compatibility only (context used to be @Nullable).
            return C.NETWORK_TYPE_UNKNOWN;
        }
        android.net.NetworkInfo networkInfo;
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return C.NETWORK_TYPE_UNKNOWN;
        }
        try {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        } catch (SecurityException e) {
            // Expected if permission was revoked.
            return C.NETWORK_TYPE_UNKNOWN;
        }
        if (networkInfo == null || !networkInfo.isConnected()) {
            return C.NETWORK_TYPE_OFFLINE;
        }

        switch (networkInfo.getType()) {

            case ConnectivityManager.TYPE_WIFI:
                return C.NETWORK_TYPE_WIFI;

            case ConnectivityManager.TYPE_WIMAX:
                return C.NETWORK_TYPE_4G;

            case ConnectivityManager.TYPE_ETHERNET:
                return C.NETWORK_TYPE_ETHERNET;

            default: // VPN, Bluetooth, Dummy.
                return C.NETWORK_TYPE_OTHER;

        }
    }

    /**
     * Returns the upper-case ISO 3166-1 alpha-2 country code of the current registered operator's
     * MCC (Mobile Country Code), or the country code of the default Locale if not available.
     *
     * @param context A context to access the telephony service. If null, only the Locale can be
     *        used.
     * @return The upper-case ISO 3166-1 alpha-2 country code, or an empty String if unavailable.
     */
    public static String getCountryCode(@Nullable Context context) {
        if (context != null) {
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                String countryCode = telephonyManager.getNetworkCountryIso();
                if (!TextUtils.isEmpty(countryCode)) {
                    return toUpperInvariant(countryCode);
                }
            }
        }
        return toUpperInvariant(Locale.getDefault().getCountry());
    }

    /**
     * Returns a non-empty array of normalized IETF BCP 47 language tags for the system languages
     * ordered by preference.
     */
    public static String[] getSystemLanguageCodes() {

        Configuration config = Resources.getSystem().getConfiguration();
        String[] systemLocales;

        if (SDK_INT >= 24) {
            systemLocales = split(config.getLocales().toLanguageTags(), ",");
        } else if (SDK_INT >= 21) {
            systemLocales = new String[] {config.locale.toLanguageTag()};
        } else {
            systemLocales = new String[] {config.locale.toString()};
        }

        for (int i = 0; i < systemLocales.length; i++) {
            systemLocales[i] = normalizeLanguageCode(systemLocales[i]);
        }
        
        return systemLocales;
    }

    /**
     * Uncompresses the data in {@code input}.
     *
     * @param input Wraps the compressed input data.
     * @param output Wraps an output buffer to be used to store the uncompressed data. If {@code
     *     output.data} isn't big enough to hold the uncompressed data, a new array is created. If
     *        {@code true} is returned then the output's position will be set to 0 and its limit
     *        will be set to the length of the uncompressed data.
     * @param inflater If not null, used to uncompressed the input. Otherwise a new {@link Inflater}
     *        is created.
     * @return Whether the input is uncompressed successfully.
     */
    public static boolean inflate(ParsableByteArray input, ParsableByteArray output,
            @Nullable Inflater inflater) {
        if (input.bytesLeft() <= 0) {
            return false;
        }
        byte[] outputData = output.data;
        if (outputData.length < input.bytesLeft()) {
            outputData = new byte[2 * input.bytesLeft()];
        }
        if (inflater == null) {
            inflater = new Inflater();
        }
        inflater.setInput(input.data, input.getPosition(), input.bytesLeft());
        try {
            int outputSize = 0;
            while (true) {
                outputSize +=
                        inflater.inflate(outputData, outputSize, outputData.length - outputSize);
                if (inflater.finished()) {
                    output.reset(outputData, outputSize);
                    return true;
                }
                if (inflater.needsDictionary() || inflater.needsInput()) {
                    return false;
                }
                if (outputSize == outputData.length) {
                    outputData = Arrays.copyOf(outputData, outputData.length * 2);
                }
            }
        } catch (DataFormatException e) {
            return false;
        } finally {
            inflater.reset();
        }
    }

    /**
     * Returns whether the app is running on a TV device.
     *
     * @param context Any context.
     * @return Whether the app is running on a TV device.
     */
    public static boolean isTv(Context context) {
        // See https://developer.android.com/training/tv/start/hardware.html#runtime-check.
        UiModeManager uiModeManager =
                (UiModeManager) context.getApplicationContext().getSystemService(UI_MODE_SERVICE);
        return uiModeManager != null
                && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    /**
     * Extract renderer capabilities for the renderers created by the provided renderers factory.
     *
     * @param renderersFactory A {@link RenderersFactory}.
     * @param drmSessionManager An optional {@link DrmSessionManager} used by the renderers.
     * @return The {@link RendererCapabilities} for each renderer created by the {@code
     *     renderersFactory}.
     */
    public static RendererCapabilities[] getRendererCapabilities(RenderersFactory renderersFactory,
            @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        Renderer[] renderers =
                renderersFactory.createRenderers(new Handler(), new VideoRendererEventListener() {},
                        new AudioRendererEventListener() {}, (cues) -> {
                        }, (metadata) -> {
                        }, drmSessionManager);
        RendererCapabilities[] capabilities = new RendererCapabilities[renderers.length];
        for (int i = 0; i < renderers.length; i++) {
            capabilities[i] = renderers[i].getCapabilities();
        }
        return capabilities;
    }

    @TargetApi(21)
    private static String normalizeLanguageCodeSyntaxV21(String languageTag) {
        return Locale.forLanguageTag(languageTag).toLanguageTag();
    }

    private static HashMap<String, String> createIso3ToIso2Map() {
        String[] iso2Languages = Locale.getISOLanguages();
        HashMap<String, String> iso3ToIso2 = new HashMap<>(
                /* initialCapacity= */ iso2Languages.length + iso3BibliographicalToIso2.length);
        for (String iso2 : iso2Languages) {
            try {
                // This returns the ISO 639-2/T code for the language.
                String iso3 = new Locale(iso2).getISO3Language();
                if (!TextUtils.isEmpty(iso3)) {
                    iso3ToIso2.put(iso3, iso2);
                }
            } catch (MissingResourceException e) {
                // Shouldn't happen for list of known languages, but we don't want to throw either.
            }
        }
        // Add additional ISO 639-2/B codes to mapping.
        for (int i = 0; i < iso3BibliographicalToIso2.length; i += 2) {
            iso3ToIso2.put(iso3BibliographicalToIso2[i], iso3BibliographicalToIso2[i + 1]);
        }
        return iso3ToIso2;
    }

    // See https://en.wikipedia.org/wiki/List_of_ISO_639-2_codes.
    private static final String[] iso3BibliographicalToIso2 = new String[] {"alb", "sq", "arm",
            "hy", "baq", "eu", "bur", "my", "tib", "bo", "chi", "zh", "cze", "cs", "dut", "nl",
            "ger", "de", "gre", "el", "fre", "fr", "geo", "ka", "ice", "is", "mac", "mk", "mao",
            "mi", "may", "ms", "per", "fa", "rum", "ro", "slo", "sk", "wel", "cy"};

    /**
     * Allows the CRC calculation to be done byte by byte instead of bit per bit being the order
     * "most significant bit first".
     */
    private static final int[] CRC32_BYTES_MSBF = {
        0X00000000, 0X04C11DB7, 0X09823B6E, 0X0D4326D9,
        0X130476DC, 0X17C56B6B, 0X1A864DB2, 0X1E475005, 0X2608EDB8, 0X22C9F00F, 0X2F8AD6D6,
        0X2B4BCB61, 0X350C9B64, 0X31CD86D3, 0X3C8EA00A, 0X384FBDBD, 0X4C11DB70, 0X48D0C6C7,
        0X4593E01E, 0X4152FDA9, 0X5F15ADAC, 0X5BD4B01B, 0X569796C2, 0X52568B75, 0X6A1936C8,
        0X6ED82B7F, 0X639B0DA6, 0X675A1011, 0X791D4014, 0X7DDC5DA3, 0X709F7B7A, 0X745E66CD,
        0X9823B6E0, 0X9CE2AB57, 0X91A18D8E, 0X95609039, 0X8B27C03C, 0X8FE6DD8B, 0X82A5FB52,
        0X8664E6E5, 0XBE2B5B58, 0XBAEA46EF, 0XB7A96036, 0XB3687D81, 0XAD2F2D84, 0XA9EE3033,
        0XA4AD16EA, 0XA06C0B5D, 0XD4326D90, 0XD0F37027, 0XDDB056FE, 0XD9714B49, 0XC7361B4C,
        0XC3F706FB, 0XCEB42022, 0XCA753D95, 0XF23A8028, 0XF6FB9D9F, 0XFBB8BB46, 0XFF79A6F1,
        0XE13EF6F4, 0XE5FFEB43, 0XE8BCCD9A, 0XEC7DD02D, 0X34867077, 0X30476DC0, 0X3D044B19,
        0X39C556AE, 0X278206AB, 0X23431B1C, 0X2E003DC5, 0X2AC12072, 0X128E9DCF, 0X164F8078,
        0X1B0CA6A1, 0X1FCDBB16, 0X018AEB13, 0X054BF6A4, 0X0808D07D, 0X0CC9CDCA, 0X7897AB07,
        0X7C56B6B0, 0X71159069, 0X75D48DDE, 0X6B93DDDB, 0X6F52C06C, 0X6211E6B5, 0X66D0FB02,
        0X5E9F46BF, 0X5A5E5B08, 0X571D7DD1, 0X53DC6066, 0X4D9B3063, 0X495A2DD4, 0X44190B0D,
        0X40D816BA, 0XACA5C697, 0XA864DB20, 0XA527FDF9, 0XA1E6E04E, 0XBFA1B04B, 0XBB60ADFC,
        0XB6238B25, 0XB2E29692, 0X8AAD2B2F, 0X8E6C3698, 0X832F1041, 0X87EE0DF6, 0X99A95DF3,
        0X9D684044, 0X902B669D, 0X94EA7B2A, 0XE0B41DE7, 0XE4750050, 0XE9362689, 0XEDF73B3E,
        0XF3B06B3B, 0XF771768C, 0XFA325055, 0XFEF34DE2, 0XC6BCF05F, 0XC27DEDE8, 0XCF3ECB31,
        0XCBFFD686, 0XD5B88683, 0XD1799B34, 0XDC3ABDED, 0XD8FBA05A, 0X690CE0EE, 0X6DCDFD59,
        0X608EDB80, 0X644FC637, 0X7A089632, 0X7EC98B85, 0X738AAD5C, 0X774BB0EB, 0X4F040D56,
        0X4BC510E1, 0X46863638, 0X42472B8F, 0X5C007B8A, 0X58C1663D, 0X558240E4, 0X51435D53,
        0X251D3B9E, 0X21DC2629, 0X2C9F00F0, 0X285E1D47, 0X36194D42, 0X32D850F5, 0X3F9B762C,
        0X3B5A6B9B, 0X0315D626, 0X07D4CB91, 0X0A97ED48, 0X0E56F0FF, 0X1011A0FA, 0X14D0BD4D,
        0X19939B94, 0X1D528623, 0XF12F560E, 0XF5EE4BB9, 0XF8AD6D60, 0XFC6C70D7, 0XE22B20D2,
        0XE6EA3D65, 0XEBA91BBC, 0XEF68060B, 0XD727BBB6, 0XD3E6A601, 0XDEA580D8, 0XDA649D6F,
        0XC423CD6A, 0XC0E2D0DD, 0XCDA1F604, 0XC960EBB3, 0XBD3E8D7E, 0XB9FF90C9, 0XB4BCB610,
        0XB07DABA7, 0XAE3AFBA2, 0XAAFBE615, 0XA7B8C0CC, 0XA379DD7B, 0X9B3660C6, 0X9FF77D71,
        0X92B45BA8, 0X9675461F, 0X8832161A, 0X8CF30BAD, 0X81B02D74, 0X857130C3, 0X5D8A9099,
        0X594B8D2E, 0X5408ABF7, 0X50C9B640, 0X4E8EE645, 0X4A4FFBF2, 0X470CDD2B, 0X43CDC09C,
        0X7B827D21, 0X7F436096, 0X7200464F, 0X76C15BF8, 0X68860BFD, 0X6C47164A, 0X61043093,
        0X65C52D24, 0X119B4BE9, 0X155A565E, 0X18197087, 0X1CD86D30, 0X029F3D35, 0X065E2082,
        0X0B1D065B, 0X0FDC1BEC, 0X3793A651, 0X3352BBE6, 0X3E119D3F, 0X3AD08088, 0X2497D08D,
        0X2056CD3A, 0X2D15EBE3, 0X29D4F654, 0XC5A92679, 0XC1683BCE, 0XCC2B1D17, 0XC8EA00A0,
        0XD6AD50A5, 0XD26C4D12, 0XDF2F6BCB, 0XDBEE767C, 0XE3A1CBC1, 0XE760D676, 0XEA23F0AF,
        0XEEE2ED18, 0XF0A5BD1D, 0XF464A0AA, 0XF9278673, 0XFDE69BC4, 0X89B8FD09, 0X8D79E0BE,
        0X803AC667, 0X84FBDBD0, 0X9ABC8BD5, 0X9E7D9662, 0X933EB0BB, 0X97FFAD0C, 0XAFB010B1,
        0XAB710D06, 0XA6322BDF, 0XA2F33668, 0XBCB4666D, 0XB8757BDA, 0XB5365D03, 0XB1F740B4
    };

    public static boolean isDescendant(ViewGroup parent, View child) {
        while (child != null) {
            if (child == parent) {
                return true;
            }
            ViewParent p = child.getParent();
            if (!(p instanceof View)) {
                return false;
            }
            child = (View) p;
        }
        return false;
    }

    public static String updateQuery(String baseUrl, String key, Object value) {
        if (baseUrl == null || key == null || value == null) return baseUrl;
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + key + "=" + value;
    }

}
