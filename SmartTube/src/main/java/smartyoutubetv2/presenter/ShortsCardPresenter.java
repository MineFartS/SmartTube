package SmartTubeApp.presenter;

import android.content.Context;
import android.util.Pair;
import SmartTubeApp.prefs.MainUIData;
import SmartTubeApp.R;
import SmartTubeApp.ui.browse.video.GridFragmentHelper;

public class ShortsCardPresenter extends VideoCardPresenter {
    @Override
    protected Pair<Integer, Integer> getCardDimensPx(Context context) {
        return GridFragmentHelper.getCardDimensPx(
            context, 
            R.dimen.shorts_card_width, 
            R.dimen.shorts_card_height, 
            1.0f // Scale
        );
    }
}
