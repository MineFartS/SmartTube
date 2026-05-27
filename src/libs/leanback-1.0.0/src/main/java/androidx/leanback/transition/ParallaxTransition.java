package androidx.leanback.transition;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.leanback.R;
import androidx.leanback.widget.Parallax;

/**
 * A slide transition changes TRANSLATION attribute of view which is not exposed by any event.
 * In order to run a parallax effect with Slide transition, ParallaxTransition is running on the
 * side, calling ParallaxSource.updateValues() on every frame. User should make sure slide
 * and ParallaxTransition are using same duration and startDelay.
 *
 * @hide
 */
@RequiresApi(21)
@RestrictTo(LIBRARY_GROUP)
public class ParallaxTransition extends Visibility {

    static Interpolator sInterpolator = new LinearInterpolator();

    public ParallaxTransition() {
    }

    public ParallaxTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    Animator createAnimator(View view) {
        final Parallax source = (Parallax) view.getTag(R.id.lb_parallax_source);
        if (source == null) {
            return null;
        }
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setInterpolator(sInterpolator);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                source.updateValues();
            }
        });
        return animator;
    }

    @Override
    public Animator onAppear(ViewGroup sceneRoot, View view,
                             TransitionValues startValues, TransitionValues endValues) {
        if (endValues == null) {
            return null;
        }
        return createAnimator(view);
    }

    @Override
    public Animator onDisappear(ViewGroup sceneRoot, View view,
                                TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null) {
            return null;
        }
        return createAnimator(view);
    }
}
