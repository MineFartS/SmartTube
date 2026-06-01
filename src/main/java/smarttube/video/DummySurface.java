package minefarts.smarttube.video;

import static minefarts.smarttube.utils.EGLSurfaceTexture.SECURE_MODE_NONE;
import static minefarts.smarttube.utils.EGLSurfaceTexture.SECURE_MODE_PROTECTED_PBUFFER;
import static minefarts.smarttube.utils.EGLSurfaceTexture.SECURE_MODE_SURFACELESS_CONTEXT;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLDisplay;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import androidx.annotation.Nullable;
import android.view.Surface;
import minefarts.smarttube.utils.Assertions;
import minefarts.smarttube.utils.EGLSurfaceTexture;
import minefarts.smarttube.utils.EGLSurfaceTexture.SecureMode;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.Utils;
import javax.microedition.khronos.egl.EGL10;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/**
 * A dummy {@link Surface}.
 */
@TargetApi(17)
public final class DummySurface extends Surface {

  private static final String TAG = "DummySurface";

  private static final String EXTENSION_PROTECTED_CONTENT = "EGL_EXT_protected_content";
  private static final String EXTENSION_SURFACELESS_CONTEXT = "EGL_KHR_surfaceless_context";

  /**
   * Whether the surface is secure.
   */
  public final boolean secure;

  private static @SecureMode int secureMode;
  private static boolean secureModeInitialized;

  private final DummySurfaceThread thread;
  private boolean threadReleased;

  /**
   * Returns whether the device supports secure dummy surfaces.
   *
   * @param context Any {@link Context}.
   * @return Whether the device supports secure dummy surfaces.
   */
  public static synchronized boolean isSecureSupported(Context context) {
    if (!secureModeInitialized) {
      secureMode = Utils.SDK_INT < 24 ? SECURE_MODE_NONE : getSecureModeV24(context);
      secureModeInitialized = true;
    }
    return secureMode != SECURE_MODE_NONE;
  }

  /**
   * Returns a newly created dummy surface. The surface must be released by calling {@link #release}
   * when it's no longer required.
   * <p>
   * Must only be called if {@link Util#SDK_INT} is 17 or higher.
   *
   * @param context Any {@link Context}.
   * @param secure Whether a secure surface is required. Must only be requested if
   *     {@link #isSecureSupported(Context)} returns {@code true}.
   * @throws IllegalStateException If a secure surface is requested on a device for which
   *     {@link #isSecureSupported(Context)} returns {@code false}.
   */
  public static DummySurface newInstanceV17(Context context, boolean secure) {
    assertApiLevel17OrHigher();
    Assertions.checkState(!secure || isSecureSupported(context));
    DummySurfaceThread thread = new DummySurfaceThread();
    return thread.init(secure ? secureMode : SECURE_MODE_NONE);
  }

  private DummySurface(DummySurfaceThread thread, SurfaceTexture surfaceTexture, boolean secure) {
    super(surfaceTexture);
    this.thread = thread;
    this.secure = secure;
  }

  @Override
  public void release() {
    super.release();
    // The Surface may be released multiple times (explicitly and by Surface.finalize()). The
    // implementation of super.release() has its own deduplication logic. Below we need to
    // deduplicate ourselves. Synchronization is required as we don't control the thread on which
    // Surface.finalize() is called.
    synchronized (thread) {
      if (!threadReleased) {
        thread.release();
        threadReleased = true;
      }
    }
  }

  private static void assertApiLevel17OrHigher() {
    if (Utils.SDK_INT < 17) {
      throw new UnsupportedOperationException("Unsupported prior to API level 17");
    }
  }

  @TargetApi(24)
  private static @SecureMode int getSecureModeV24(Context context) {
    if (Utils.SDK_INT < 26 && ("samsung".equals(Utils.MANUFACTURER) || "XT1650".equals(Utils.MODEL))) {
      // Samsung devices running Nougat are known to be broken. See
      // https://github.com/google/ExoPlayer/issues/3373 and [Internal: b/37197802].
      // Moto Z XT1650 is also affected. See
      // https://github.com/google/ExoPlayer/issues/3215.
      return SECURE_MODE_NONE;
    }
    if (Utils.SDK_INT < 26 && !context.getPackageManager().hasSystemFeature(
        PackageManager.FEATURE_VR_MODE_HIGH_PERFORMANCE)) {
      // Pre API level 26 devices were not well tested unless they supported VR mode.
      return SECURE_MODE_NONE;
    }
    EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
    String eglExtensions = EGL14.eglQueryString(display, EGL10.EGL_EXTENSIONS);
    if (eglExtensions == null) {
      return SECURE_MODE_NONE;
    }
    if (!eglExtensions.contains(EXTENSION_PROTECTED_CONTENT)) {
      return SECURE_MODE_NONE;
    }
    // If we can't use surfaceless contexts, we use a protected 1 * 1 pixel buffer surface. This may
    // require support for EXT_protected_surface, but in practice it works on some devices that
    // don't have that extension. See also https://github.com/google/ExoPlayer/issues/3558.
    return eglExtensions.contains(EXTENSION_SURFACELESS_CONTEXT)
        ? SECURE_MODE_SURFACELESS_CONTEXT
        : SECURE_MODE_PROTECTED_PBUFFER;
  }

  private static class DummySurfaceThread extends HandlerThread implements Callback {

    private static final int MSG_INIT = 1;
    private static final int MSG_RELEASE = 2;

    private @MonotonicNonNull EGLSurfaceTexture eglSurfaceTexture;
    private @MonotonicNonNull Handler handler;
    private @Nullable Error initError;
    private @Nullable RuntimeException initException;
    private @Nullable DummySurface surface;

    public DummySurfaceThread() {
      super("dummySurface");
    }

    public DummySurface init(@SecureMode int secureMode) {
      start();
      handler = new Handler(getLooper(), /* callback= */ this);
      eglSurfaceTexture = new EGLSurfaceTexture(handler);
      boolean wasInterrupted = false;
      synchronized (this) {
        handler.obtainMessage(MSG_INIT, secureMode, 0).sendToTarget();
        while (surface == null && initException == null && initError == null) {
          try {
            wait();
          } catch (InterruptedException e) {
            wasInterrupted = true;
          }
        }
      }
      if (wasInterrupted) {
        // Restore the interrupted status.
        Thread.currentThread().interrupt();
      }
      if (initException != null) {
        throw initException;
      } else if (initError != null) {
        throw initError;
      } else {
        return Assertions.checkNotNull(surface);
      }
    }

    public void release() {
      Assertions.checkNotNull(handler);
      handler.sendEmptyMessage(MSG_RELEASE);
    }

    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case MSG_INIT:
          try {
            initInternal(/* secureMode= */ msg.arg1);
          } catch (RuntimeException e) {
            Log.e(TAG, "Failed to initialize dummy surface", e);
            initException = e;
          } catch (Error e) {
            Log.e(TAG, "Failed to initialize dummy surface", e);
            initError = e;
          } finally {
            synchronized (this) {
              notify();
            }
          }
          return true;
        case MSG_RELEASE:
          try {
            releaseInternal();
          } catch (Throwable e) {
            Log.e(TAG, "Failed to release dummy surface", e);
          } finally {
            quit();
          }
          return true;
        default:
          return true;
      }
    }

    private void initInternal(@SecureMode int secureMode) {
      Assertions.checkNotNull(eglSurfaceTexture);
      eglSurfaceTexture.init(secureMode);
      this.surface =
          new DummySurface(
              this, eglSurfaceTexture.getSurfaceTexture(), secureMode != SECURE_MODE_NONE);
    }

    private void releaseInternal() {
      Assertions.checkNotNull(eglSurfaceTexture);
      eglSurfaceTexture.release();
    }

  }

}
