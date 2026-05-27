package androidx.leanback.app;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.os.Build;

import androidx.annotation.RestrictTo;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class PermissionHelper {

    public static void requestPermissions(android.app.Fragment fragment, String[] permissions,
            int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            fragment.requestPermissions(permissions, requestCode);
        }
    }

    private PermissionHelper() {
    }
}
