package androidx.leanback.widget;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.leanback.R;

/**
 * Helper for static (nine patch) shadows.
 */
final class StaticShadowHelper {
    private StaticShadowHelper() {
    }

    static boolean supportsShadow() {
        return Build.VERSION.SDK_INT >= 21;
    }

    static void prepareParent(ViewGroup parent) {
        if (Build.VERSION.SDK_INT >= 21) {
            parent.setLayoutMode(ViewGroup.LAYOUT_MODE_OPTICAL_BOUNDS);
        }
    }

    static Object addStaticShadow(ViewGroup shadowContainer) {
        if (Build.VERSION.SDK_INT >= 21) {
            shadowContainer.setLayoutMode(ViewGroup.LAYOUT_MODE_OPTICAL_BOUNDS);
            LayoutInflater inflater = LayoutInflater.from(shadowContainer.getContext());
            inflater.inflate(R.layout.lb_shadow, shadowContainer, true);
            ShadowImpl impl = new ShadowImpl();
            impl.mNormalShadow = shadowContainer.findViewById(R.id.lb_shadow_normal);
            impl.mFocusShadow = shadowContainer.findViewById(R.id.lb_shadow_focused);
            return impl;
        }
        return null;
    }

    static void setShadowFocusLevel(Object impl, float level) {
        if (Build.VERSION.SDK_INT >= 21) {
            ShadowImpl shadowImpl = (ShadowImpl) impl;
            shadowImpl.mNormalShadow.setAlpha(1 - level);
            shadowImpl.mFocusShadow.setAlpha(level);
        }
    }

    static class ShadowImpl {
        View mNormalShadow;
        View mFocusShadow;
    }
}
