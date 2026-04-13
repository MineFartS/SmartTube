
package androidx.leanback.widget;

import android.animation.Animator;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * FragmentAnimationProvider supplies animations for use during a fragment's onCreateAnimator
 * callback. Animators added here will be added to an animation set and played together. This
 * allows presenters used by a fragment to control their own fragment lifecycle animations.
 */
public interface FragmentAnimationProvider {

    /**
     * Animates the fragment in response to the IME appearing.
     * @param animators A list of animations to which this provider's animations should be added.
     */
    public abstract void onImeAppearing(@NonNull List<Animator> animators);

    /**
     * Animates the fragment in response to the IME disappearing.
     * @param animators A list of animations to which this provider's animations should be added.
     */
    public abstract void onImeDisappearing(@NonNull List<Animator> animators);

}
