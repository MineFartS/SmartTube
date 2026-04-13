

package smartyoutubetv2.ui.mod.leanback.preference;

import android.app.Fragment;
import android.transition.Transition;
import android.view.Gravity;
import androidx.annotation.RequiresApi;
import smartyoutubetv2.ui.mod.leanback.transition.FadeAndShortSlide;

/**
 * @hide
 */
@RequiresApi(21)
public class LeanbackPreferenceFragmentTransitionHelperApi21 {

    public static void addTransitions(Fragment f) {
        final Transition transitionStartEdge = new FadeAndShortSlide(Gravity.START);
        final Transition transitionEndEdge = new FadeAndShortSlide(Gravity.END);

        f.setEnterTransition(transitionEndEdge);
        f.setExitTransition(transitionStartEdge);
        f.setReenterTransition(transitionStartEdge);
        f.setReturnTransition(transitionEndEdge);
    }


    private LeanbackPreferenceFragmentTransitionHelperApi21() {
    }
}
