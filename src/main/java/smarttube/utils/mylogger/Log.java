package minefarts.smarttube.utils.mylogger;

import com.google.gson.Gson;

import androidx.annotation.Nullable;

import java.util.Arrays;

/** Wrapper around {@link android.util.Log} which allows to set the log level. */
public final class Log {

    private static final Gson sGson = new Gson();

    public static void d(
        String tag, 
        @Nullable Object message,
        Object... formatArgs
    ) {
        d(tag, message, null, formatArgs);
    }

    public static void d(
        String tag, 
        @Nullable Object message,
        @Nullable Throwable throwable,
        Object... formatArgs
    ) {
        String msg = formatMsg(message, formatArgs);
        android.util.Log.d(tag, msg, throwable);
    }

    public static void i(
        String tag, 
        @Nullable Object message,
        Object... formatArgs
    ) {
        i(tag, message, null, formatArgs);
    }

    public static void i(
        String tag, 
        @Nullable Object message,
        @Nullable Throwable throwable,
        Object... formatArgs
    ) {
        String msg = formatMsg(message, formatArgs);
        android.util.Log.i(tag, msg, throwable);
    }

    public static void w(
        String tag, 
        @Nullable Object message,
        Object... formatArgs
    ) {
        w(tag, message, Traceback(), formatArgs);
    }

    public static void w(
        String tag, 
        @Nullable Object message,
        @Nullable Throwable throwable,
        Object... formatArgs
    ) {
        String msg = formatMsg(message, formatArgs);
        android.util.Log.w(tag, msg, throwable);
    }

    public static void e(
        String tag, 
        @Nullable Object message,
        Object... formatArgs
    ) {
        e(tag, message, Traceback(), formatArgs);
    }

    public static void e(
        String tag, 
        @Nullable Object message,
        @Nullable Throwable throwable,
        Object... formatArgs
    ) {
        String msg = formatMsg(message, formatArgs);
        android.util.Log.e(tag, msg, throwable);
    }

    private static String formatMsg(Object msg, Object... formatArgs) {

        if (msg == null) return "";

        String fmsg = "\n";

        if (formatArgs != null && formatArgs.length > 0) {

            fmsg += String.format(msg.toString(), formatArgs);

        } else {
            try {
                fmsg += msg.toString();
            } catch (Exception e) {
                fmsg += sGson.toJson(msg);
            }
        }

        return fmsg + "\n";
    }

    private static Throwable Traceback() {

        StackTraceElement[] traceback = Arrays.stream(Thread.currentThread().getStackTrace())
            .filter(ste -> !ste.getClassName().contains("utils.mylogger.Log"))
            .toArray(StackTraceElement[]::new);

        Throwable ex = new Throwable();

        ex.setStackTrace(traceback);
    
        return ex;
    }

}
