package minefarts.smarttube.leanback.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

class NonOverlappingView extends View {
    public NonOverlappingView(Context context) {
        this(context, null);
    }

    public NonOverlappingView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public NonOverlappingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Avoids creating a hardware layer when Transition is animating alpha.
     */
    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }
}
