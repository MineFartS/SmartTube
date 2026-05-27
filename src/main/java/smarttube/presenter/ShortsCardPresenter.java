package minefarts.smarttube.presenter;

import android.content.Context;
import android.util.Pair;
import minefarts.smarttube.prefs.MainUIData;
import minefarts.smarttube.R;
import minefarts.smarttube.ui.browse.video.GridFragmentHelper;

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
