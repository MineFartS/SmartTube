package androidx.leanback.transition;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.transition.Slide;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;

/**
 * @hide
 */
@RequiresApi(21)
@RestrictTo(LIBRARY_GROUP)
public class SlideNoPropagation extends Slide {

    public SlideNoPropagation() {
    }

    public SlideNoPropagation(int slideEdge) {
        super(slideEdge);
    }

    public SlideNoPropagation(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setSlideEdge(int slideEdge) {
        super.setSlideEdge(slideEdge);
        setPropagation(null);
    }
}
