package SmartTubeApp.presenter;

import android.content.Context;
import android.util.Pair;
import SmartTubeApp.prefs.MainUIData;
import SmartTubeApp.R;
import SmartTubeApp.ui.browse.video.GridFragmentHelper;

public class TinyCardPresenter extends VideoCardPresenter {
    @Override
    protected Pair<Integer, Integer> getCardDimensPx(Context context) {
        return GridFragmentHelper.getCardDimensPx(
            context, 
            R.dimen.tiny_card_width, 
            R.dimen.tiny_card_height, 
            1.0f //Scale
        );
    }

    @Override
    protected boolean isContentEnabled() {
        return false;
    }
}
