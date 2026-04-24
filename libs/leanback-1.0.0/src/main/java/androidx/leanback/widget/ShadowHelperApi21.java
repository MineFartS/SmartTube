
package androidx.leanback.widget;

import android.graphics.Outline;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.RequiresApi;

@RequiresApi(21)
class ShadowHelperApi21 {

    static class ShadowImpl {
        View mShadowContainer;
        float mNormalZ;
        float mFocusedZ;
    }

    static final ViewOutlineProvider sOutlineProvider = new ViewOutlineProvider() {
        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRect(0, 0, view.getWidth(), view.getHeight());
            outline.setAlpha(1.0f);
        }
    };

    /* add shadows and return a implementation detail object */
    public static Object addDynamicShadow(
            View shadowContainer, float unfocusedZ, float focusedZ, int roundedCornerRadius) {
        if (roundedCornerRadius > 0) {
            RoundedRectHelperApi21.setClipToRoundedOutline(shadowContainer, true,
                    roundedCornerRadius);
        } else {
            shadowContainer.setOutlineProvider(sOutlineProvider);
        }
        ShadowImpl impl = new ShadowImpl();
        impl.mShadowContainer = shadowContainer;
        impl.mNormalZ = unfocusedZ;
        impl.mFocusedZ = focusedZ;
        shadowContainer.setZ(impl.mNormalZ);
        return impl;
    }

    /* set shadow focus level 0 for unfocused 1 for fully focused */
    public static void setShadowFocusLevel(Object object, float level) {
        ShadowImpl impl = (ShadowImpl) object;
        impl.mShadowContainer.setZ(impl.mNormalZ + level * (impl.mFocusedZ - impl.mNormalZ));
    }

    private ShadowHelperApi21() {
    }
}
