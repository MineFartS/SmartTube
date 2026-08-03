package minefarts.smarttube.leanback.widget;

import android.graphics.Outline;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.RequiresApi;

@RequiresApi(21)
class RoundedRectHelperApi21 {

    private static SparseArray<ViewOutlineProvider> sRoundedRectProvider;
    private static final int MAX_CACHED_PROVIDER = 32;

    static final class RoundedRectOutlineProvider extends ViewOutlineProvider {

        private int mRadius;

        RoundedRectOutlineProvider(int radius) {
            mRadius = radius;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mRadius);
            outline.setAlpha(1f);
        }
    };

    public static void setClipToRoundedOutline(View view, boolean clip, int roundedCornerRadius) {
        if (clip) {
            if (sRoundedRectProvider == null) {
                sRoundedRectProvider = new SparseArray<ViewOutlineProvider>();
            }
            ViewOutlineProvider provider = sRoundedRectProvider.get(roundedCornerRadius);
            if (provider == null) {
                provider = new RoundedRectOutlineProvider(roundedCornerRadius);
                if (sRoundedRectProvider.size() < MAX_CACHED_PROVIDER) {
                    sRoundedRectProvider.put(roundedCornerRadius, provider);
                }
            }
            view.setOutlineProvider(provider);
        } else {
            view.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        }
        view.setClipToOutline(clip);
    }

    private RoundedRectHelperApi21() {
    }
}
