package smartyoutubetv2.presenter;

import android.content.Context;
import android.util.Pair;
import smartyoutubetv1.prefs.MainUIData;
import smartyoutubetv2.R;
import smartyoutubetv2.ui.browse.video.GridFragmentHelper;

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
