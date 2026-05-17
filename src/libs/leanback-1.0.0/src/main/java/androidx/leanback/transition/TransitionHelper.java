
package androidx.leanback.transition;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.transition.AutoTransition;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;

import androidx.annotation.RestrictTo;

import java.util.ArrayList;

/**
 * Helper for view transitions.
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class TransitionHelper {

    public static final int FADE_IN = 0x1;
    public static final int FADE_OUT = 0x2;

    public static Object getSharedElementEnterTransition(Window window) {
        return window.getSharedElementEnterTransition();
    }

    public static void setSharedElementEnterTransition(Window window, Object transition) {
        window.setSharedElementEnterTransition((Transition) transition);
    }

    public static Object getSharedElementReturnTransition(Window window) {
        return window.getSharedElementReturnTransition();
    }

    public static void setSharedElementReturnTransition(Window window, Object transition) {
        window.setSharedElementReturnTransition((Transition) transition);
    }

    public static Object getSharedElementExitTransition(Window window) {
        return window.getSharedElementExitTransition();
    }

    public static Object getSharedElementReenterTransition(Window window) {
        return window.getSharedElementReenterTransition();
    }

    public static Object getEnterTransition(Window window) {
        return window.getEnterTransition();
    }

    public static void setEnterTransition(Window window, Object transition) {
        window.setEnterTransition((Transition) transition);
    }

    public static Object getReturnTransition(Window window) {
        return window.getReturnTransition();
    }

    public static void setReturnTransition(Window window, Object transition) {
        window.setReturnTransition((Transition) transition);
    }

    public static Object getExitTransition(Window window) {
        return window.getExitTransition();
    }

    public static Object getReenterTransition(Window window) {
        return window.getReenterTransition();
    }

    public static Object createScene(ViewGroup sceneRoot, Runnable r) {
        Scene scene = new Scene(sceneRoot);
        scene.setEnterAction(r);
        return scene;
    }

    public static Object createChangeBounds(boolean reparent) {
        CustomChangeBounds changeBounds = new CustomChangeBounds();
        changeBounds.setReparent(reparent);
        return changeBounds;
    }

    public static Object createChangeTransform() {
        return new ChangeTransform();
    }

    public static void setChangeBoundsStartDelay(Object changeBounds, View view, int startDelay) {
        ((CustomChangeBounds) changeBounds).setStartDelay(view, startDelay);
    }

    public static void setChangeBoundsStartDelay(Object changeBounds, int viewId, int startDelay) {
        ((CustomChangeBounds) changeBounds).setStartDelay(viewId, startDelay);
    }

    public static void setChangeBoundsStartDelay(Object changeBounds, String className, int startDelay) {
        ((CustomChangeBounds) changeBounds).setStartDelay(className, startDelay);
    }

    public static void setChangeBoundsDefaultStartDelay(Object changeBounds, int startDelay) {
        ((CustomChangeBounds) changeBounds).setDefaultStartDelay(startDelay);
    }

    public static Object createTransitionSet(boolean sequential) {
        TransitionSet set = new TransitionSet();
        set.setOrdering(sequential ? TransitionSet.ORDERING_SEQUENTIAL
                : TransitionSet.ORDERING_TOGETHER);
        return set;
    }

    public static Object createSlide(int slideEdge) {
        SlideKitkat slide = new SlideKitkat();
        slide.setSlideEdge(slideEdge);
        return slide;
    }

    public static Object createScale() {
        return new ChangeTransform();
    }

    public static void addTransition(Object transitionSet, Object transition) {
        ((TransitionSet) transitionSet).addTransition((Transition) transition);
    }

    public static void exclude(Object transition, int targetId, boolean exclude) {
        ((Transition) transition).excludeTarget(targetId, exclude);
    }

    public static void exclude(Object transition, View targetView, boolean exclude) {
        ((Transition) transition).excludeTarget(targetView, exclude);
    }

    public static void excludeChildren(Object transition, int targetId, boolean exclude) {
        ((Transition) transition).excludeChildren(targetId, exclude);
    }

    public static void excludeChildren(Object transition, View targetView, boolean exclude) {
        ((Transition) transition).excludeChildren(targetView, exclude);
    }

    public static void include(Object transition, int targetId) {
        ((Transition) transition).addTarget(targetId);
    }

    public static void include(Object transition, View targetView) {
        ((Transition) transition).addTarget(targetView);
    }

    public static void setStartDelay(Object transition, long startDelay) {
        ((Transition) transition).setStartDelay(startDelay);
    }

    public static void setDuration(Object transition, long duration) {
        ((Transition) transition).setDuration(duration);
    }

    public static Object createAutoTransition() {
        return new AutoTransition();
    }

    public static Object createFadeTransition(int fadeMode) {
        return new Fade(fadeMode);
    }

    public static void addTransitionListener(Object transition, final TransitionListener listener) {
        if (listener == null) {
            return;
        }

        Transition t = (Transition) transition;
        listener.mImpl = new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition11) {
                listener.onTransitionStart(transition11);
            }

            @Override
            public void onTransitionResume(Transition transition11) {
                listener.onTransitionResume(transition11);
            }

            @Override
            public void onTransitionPause(Transition transition11) {
                listener.onTransitionPause(transition11);
            }

            @Override
            public void onTransitionEnd(Transition transition11) {
                listener.onTransitionEnd(transition11);
            }

            @Override
            public void onTransitionCancel(Transition transition11) {
                listener.onTransitionCancel(transition11);
            }
        };
        t.addListener((Transition.TransitionListener) listener.mImpl);

    }

    public static void removeTransitionListener(Object transition, TransitionListener listener) {
        
        if (listener == null || listener.mImpl == null) return;

        Transition t = (Transition) transition;
        t.removeListener((Transition.TransitionListener) listener.mImpl);
        listener.mImpl = null;
    }

    public static void runTransition(Object scene, Object transition) {
        TransitionManager.go((Scene) scene, (Transition) transition);
    }

    public static void setInterpolator(Object transition, Object timeInterpolator) {
        ((Transition) transition).setInterpolator((TimeInterpolator) timeInterpolator);
    }

    public static void addTarget(Object transition, View view) {
        ((Transition) transition).addTarget(view);
    }

    public static Object createDefaultInterpolator(Context context) {
        return AnimationUtils.loadInterpolator(
            context,
            android.R.interpolator.fast_out_linear_in
        );
    }

    public static Object loadTransition(Context context, int resId) {
        return TransitionInflater.from(context).inflateTransition(resId);
    }

    public static void setEnterTransition(android.app.Fragment fragment, Object transition) {
        fragment.setEnterTransition((Transition) transition);
    }

    public static void setExitTransition(android.app.Fragment fragment, Object transition) {
        fragment.setExitTransition((Transition) transition);
    }

    public static void setSharedElementEnterTransition(android.app.Fragment fragment, Object transition) {
        fragment.setSharedElementEnterTransition((Transition) transition);
    }

    public static void addSharedElement(android.app.FragmentTransaction ft, View view, String transitionName) {
        ft.addSharedElement(view, transitionName);
    }

    public static Object createFadeAndShortSlide(int edge) {
        return new FadeAndShortSlide(edge);
    }

    public static Object createFadeAndShortSlide(int edge, float distance) {
        FadeAndShortSlide slide = new FadeAndShortSlide(edge);
        slide.setDistance(distance);
        return slide;
    }

    public static void beginDelayedTransition(ViewGroup sceneRoot, Object transitionObject) {
        Transition transition = (Transition) transitionObject;
        TransitionManager.beginDelayedTransition(sceneRoot, transition);
    }

    public static void setTransitionGroup(ViewGroup viewGroup, boolean transitionGroup) {
        viewGroup.setTransitionGroup(transitionGroup);

    }

    public static void setEpicenterCallback(
        Object transition,
        final TransitionEpicenterCallback callback
    ) {

            if (callback == null) {
                ((Transition) transition).setEpicenterCallback(null);
            } else {
                ((Transition) transition).setEpicenterCallback(new Transition.EpicenterCallback() {
                    @Override
                    public Rect onGetEpicenter(Transition transition11) {
                        return callback.onGetEpicenter(transition11);
                    }
                });
            }
        
    }

    private TransitionHelper() {
    }
}
